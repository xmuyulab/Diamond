#!/bin/bash 

if [ ! -d ./work/ ] ; then
	mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../../common/tandem.xml .
ln -s ../../common/kscore.xml .
ln -s ../../common/taxonomy.xml .
ln -s ../../common/db.fasta .
ln -s ../../common/MergePep.java .

patch -o comet.params ../comet.params ../../common/comet.patch

N=`sed -n "1p" ./flag.txt|awk -F'=' '{print $2}'`

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`


find ../../mzML/split/ -name '*.mzML'|while read i ;do

	ln -s ${i} .

done



for i in ./*.mzML ; do

	j=`echo ${i}|awk -F'/' '{print $NF;}'`


	cat ../tandem.params |sed "s/PATH_TO_SPECTRUM/$j/"|sed "s/PATH_TO_OUTPUT/$j.tandem/" >./${j}.tandem.params

	cat ../kscore.params |sed "s/PATH_TO_SPECTRUM/$j/"|sed "s/PATH_TO_OUTPUT/$j.kscore/" >./${j}.kscore.params

done

rm search.kscore.txt search.tandem.txt search.comet.txt

for i in ./*.mzML ; do
	
echo "~/software/TPP/bin/tandem ${i}.tandem.params" >> search.tandem.txt
echo "~/software/TPP/bin/tandem ${i}.kscore.params" >> search.kscore.txt
echo "~/software/TPP/bin/comet ${i}" >> search.comet.txt

done

cat search.kscore.txt search.tandem.txt search.comet.txt > search.txt

cat search.txt|while read line; do
	echo ${line}
done|xargs -P ${N} -i \
bsub -R span[hosts=1] -q charge_normal -o out_02 -n 24 -K {}


for i in ./*.tandem ; do

	bsub -R span[hosts=1] -q charge_normal -o out_02 -n 1 -K \
~/software/TPP/bin/Tandem2XML $i ${i}.pep.xml &

sleep 0.1

done


for i in ./*.kscore ; do

	bsub -R span[hosts=1] -q charge_normal -o out_02 -n 1 -K \
~/software/TPP/bin/Tandem2XML $i ${i}.pep.xml &

sleep 0.1

done

wait

javac -d . MergePep.java

for i in ../../mzML/split/*.log ; do

        j=`echo ${i}|awk -F'/' '{print $NF;}'`
	k=${j%.mzML.log}
	cat ${i} |awk -F' ' '{if (NR>3){print $6}}' > ${k}.split
done

NP=`ls |grep "[.]split$"|wc -l`

ls |grep "[.]split$" |xargs -P 24 -i \
bsub -R span[hosts=1] -q charge_normal -o out_02 -n 1 -K \
java MergePep ./{}

for i in ../../mzML/split/*.mzML ; do
	j=`echo ${i}|awk -F'/' '{print $NF;}'`
	rm ./${j}

done			

for i in ../../mzML/*.mzML; do
	ln -s ${i} .
done


cd ..


