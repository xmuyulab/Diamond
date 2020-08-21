#!/bin/bash

if [ ! -d ./work/ ] ; then
        mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../../common/TricParser.java .

N=`sed -n "1p" ./flag.txt|awk -F'=' '{print $2}'`

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`



find . -name '*.mzXML_pp.tsv' |xargs \
bsub -R span[hosts=1] -q ${Q} -o out_09 -n 1 -K \
feature_alignment.py --out aligned.tsv --method LocalMST --realign_method lowess_cython --max_rt_diff 60 --mst:useRTCorrection True --mst:Stdev_multiplier 3.0 --target_fdr 0.01 --max_fdr_quality 0.05 --verbosity 1 --alignment_score 0.0001 --in 


javac -d . TricParser.java

bsub -R span[hosts=1] -q ${Q} -o out_09 -n 1 -K \
java TricParser aligned.tsv 


cd ..

