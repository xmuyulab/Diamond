#!/bin/bash 

if [ ! -d ./work/ ] ; then
	mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../lib.update.sh .
ln -s ../../common/irt.txt .
ln -s ../../common/spectrast2openswath.sh .
ln -s ../../common/spectrast2peakview.sh .
ln -s ../../common/spectrast_selfrt.py .
ln -s ../../common/modifications.tsv .
ln -s ../../common/win.tsv .
ln -s ../../common/spectrast.usermods .

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`


for chg in {2..5}; do

list=`ls |grep -E "^clean-${chg}-interact-"|grep -E "[.]pep[.]xml$"`

for i in ${list}; do
	j=`echo ${i}|sed "s/^clean-${chg}-interact-//"|sed "s/[.]pep[.]xml$//"|sed "s/[.]/_/g"`
	bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
~/software/TPP/bin/spectrast -M ./spectrast.usermods -c_BIN! -cNorig-${chg}-${j} -cICID-QTOF -cP0.0 ./${i} &

done

done

wait

ls |grep -E "^orig-"|grep -E "[.]splib$"|xargs \
bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
~/software/TPP/bin/spectrast -M ./spectrast.usermods -c_BIN! -cNorig -cJU  

bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
python ./spectrast_selfrt.py -i orig.splib -o irt.splib -k ./irt.txt




if [ ! -f irt.splib ]; then

ls |grep -E "^orig-[2-5]-"|grep -E "[.]splib$" > pep.lib
# pep.lib = list<run-psm.splib>

for chg in {2..5};do
	cat pep.lib|grep -E "^orig-${chg}-"|sed "s/^orig-${chg}-//"|grep -E -o "^[^ ]+_mzML"
done|sort|uniq > pep.run
# pep.run = list<run_mzML>

run2irt(){

	j="/-${1}_/"

	k=`cat pep.lib |awk ${j} |xargs echo `
	
	bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
	~/software/TPP/bin/spectrast -M ./spectrast.usermods -c_BIN! -cNorig-${1} -cJU ${k} 
	
	bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
	~/software/TPP/bin/spectrast -M ./spectrast.usermods -c_BIN! -cICID-QTOF -cAC -cNorig-cons-${1} orig-${1}.splib 
	
	bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
	python ./spectrast_selfrt.py -i orig-cons-${1}.splib -o irt-${1}.splib -k ./irt.txt

}

export Q
export -f run2irt

cat pep.run|xargs -P 24 -i bash -c "run2irt {}"

wait

cat pep.run|while read i; do
if [ -f irt-${i}.splib ]; then
echo "irt-${i}.splib"
fi
done |xargs bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
~/software/TPP/bin/spectrast -M ./spectrast.usermods -c_BIN! -cICID-QTOF -cAC -cJU -cNcons-irt 


rm pep.name
# pep.name = list<peptide/chg>
cat cons-irt.splib|grep -E "^Name: "|awk -F' ' '{print $2}'  >> pep.name


rm irt.tmp
# irt.tmp = list<irt>
cat cons-irt.splib|grep -A 7 -E "^Name: "|grep -E "^Comment: "|grep -E -o " RetentionTime=[-0-9.]+"|awk -F'=' '{print $2}' >> irt.tmp


exec 3<"pep.name"
exec 4<"irt.tmp"


rm pep.irt
# pep.irt = list<peptide	irt>

while read name<&3 && read irt<&4; do

p=`echo ${name}|awk -F'/' '{print $1}'`

echo -e "${p}\t${irt}" >> pep.irt

done


bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
python ./spectrast_selfrt.py -i orig.splib -o irt.splib -k ./pep.irt 



fi


bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
~/software/TPP/bin/spectrast -M ./spectrast.usermods -c_BIN! -cICID-QTOF -cAC -cNbuild irt.splib


bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
bash lib.update.sh build.splib lib.splib

bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
bash spectrast2openswath.sh

bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
bash spectrast2peakview.sh

cat prot.csv |grep -v  "DECOY_"|grep -v "cont_"|awk -F',' '{printf("%09d\t%s\n",$3,$1);}' |sort -r|head -n 3|awk -F'\t' '{print $2;}' > irt.tmp

cat lib.os.tsv|head -n 1 > irt.os.tsv
cat lib.os.tsv |grep -f ./irt.tmp|grep -v "DECOY_" | sort >> irt.os.tsv

bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
~/software/OpenMS/bin/TargetedFileConverter -in irt.os.tsv -out irt.TraML -algorithm:force_invalid_mods


bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
~/software/OpenMS/bin/TargetedFileConverter -in lib.os.tsv -out lib.TraML -algorithm:force_invalid_mods

bsub -R span[hosts=1] -q ${Q} -o out_06 -n 1 -K \
~/software/OpenMS/bin/OpenSwathDecoyGenerator -in  lib.TraML -out lib.os.TraML -method shuffle -append -exclude_similar -remove_unannotated

cd ..

