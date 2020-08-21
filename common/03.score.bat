#!/bin/bash 

if [ ! -d ./work/ ] ; then
	mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../../common/db.fasta .
ln -s ../../common/modifications.tsv .
ln -s ../../common/unimod.xml .
ln -s ../../common/ModFilter.java .
ln -s ../../common/PROT2CSV.java .


Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`

MOD=`sed -n "3p" ./flag.txt|awk -F'=' '{print $2}'`


for i in ../../mzML/*.mzML ; do

	j=`echo ${i}|awk -F'/' '{print $NF;}'`
	
	for e in {comet,tandem,kscore}; do
		
		f="${j}.${e}.pep.xml"
		
		bsub -R span[hosts=1] -q ${Q} -o out_03 -n 2 -K \
~/software/TPP/bin/xinteract -Ddb.fasta -OARPd -THREADS=2 -dDECOY_ -Ninteract-${j}.${e}.pep.xml ${f} &

		sleep 0.1

	done
done

wait

javac -d . ModFilter.java

plist=`ls |grep  '^interact-'|grep '[.]pep[.]xml$'`

for i in ${plist}; do
bsub -R span[hosts=1] -q ${Q} -o out_03 -n 1 -K \
java ModFilter ${i} &
done

wait

for i in ${plist}; do
bsub -R span[hosts=1] -q ${Q} -o out_03 -n 1 -K \
sed -i "/^\s*$/d" ${i} &
done

wait

for i in ${plist}; do
bsub -R span[hosts=1] -q ${Q} -o out_03 -n 1 -K \
sed -i "s/^[ \t]*//" ${i} &
done

wait


bsub -R span[hosts=1] -q ${Q} -o out_03 -n 24 -K \
~/software/TPP/bin/InterProphetParser THREADS=24 DECOY=DECOY_  interact-*.pep.xml ipro.pep.xml 


if [ ! -z ${MOD} ] ; then

PTM=`sed -n "5p" ./flag.txt`

bsub -R span[hosts=1] -q ${Q} -o out_03 -n 1 -K \
~/software/TPP/bin/PTMProphetParser ${PTM} ipro.pep.xml ptm.pep.xml 

fi






cd ..



