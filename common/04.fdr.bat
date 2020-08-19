#!/bin/bash 

if [ ! -d ./work/ ] ; then
	mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../../common/db.fasta 

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`

bsub -R span[hosts=1] -q ${Q} -o out_04 -n 1 -K \
perl ~/software/TPP/bin/Mayu.pl -A ipro.pep.xml -C db.fasta -E DECOY_ -G 0.01 -H 51 -I 2 -P mFDR -M fdr

f=`ls|grep '^fdr_main_'|grep '[.]csv$'`

rm fdr.txt
 
line=`cat ${f} |wc -l `

for c in {3,14,20} ; do

	awk -F',' -v i=${c} -v nl=${line} 'BEGIN{ret=0;}{if (NR>1){ if($i>=0.01&&ret==0){ret=$4;}}if (NR==nl&&ret==0){ret=$4;}}END{print ret;}' ${f} >>fdr.txt

done

cd ..









