#!/bin/bash 


if [ ! -d ./work/ ] ; then
	mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../../common/PTM2CSV.java .
ln -s ../../common/LibFilter.java .
ln -s ../../common/PROT2CSV.java .

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`

MOD=`sed -n "3p" ./flag.txt|awk -F'=' '{print $2}'`

fdr_psm=1
fdr_pep=2
fdr_pro=3

FDR=`sed -n "${fdr_pep}p" fdr.txt`

PTM_SCORE=`sed -n "4p" ./flag.txt|awk -F'=' '{print $2}'`

javac -d . PTM2CSV.java

javac -d . LibFilter.java

if [ -z ${MOD} ] ; then


bsub -R span[hosts=1] -q ${Q} -o out_05 -n 2 -K \
java PTM2CSV db.fasta ipro.pep.xml ipro.csv ${FDR}

bsub -R span[hosts=1] -q ${Q} -o out_05 -n 24 -K \
java LibFilter ipro.pep.xml lib.pep.xml ${FDR} 

else

bsub -R span[hosts=1] -q ${Q} -o out_05 -n 2 -K \
java PTM2CSV db.fasta ptm.pep.xml ptm.csv ${FDR} ${MOD}


bsub -R span[hosts=1] -q ${Q} -o out_05 -n 24 -K \
java LibFilter ptm.pep.xml lib.pep.xml ${FDR} ${MOD} ${PTM_SCORE}


fi

bsub -R span[hosts=1] -q ${Q} -o out_05 -n 1 -K \
~/software/TPP/bin/ProteinProphet lib.pep.xml lib.xml IPROPHET

javac -d . PROT2CSV.java 

bsub -R span[hosts=1] -q ${Q} -o out_05 -n 2 -K \
java PROT2CSV lib.prot.xml prot.csv DECOY_




