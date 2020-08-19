#!/bin/bash

if [ ! -d ./work/ ] ; then
        mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../../common/win.tsv .

ln -s ../../common/os.sh .

N=`sed -n "1p" ./flag.txt|awk -F'=' '{print $2}'`

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`


echo -e "start\tend" > win.os.tsv
cat win.tsv >> win.os.tsv


curr_dir=`pwd`

find ../../../profile/ -name '*.mzXML'|while read i;do

j=`echo ${i}|awk -F'/' '{print $NF;}'`
echo ${j}

done |xargs -P ${N} -i \
bsub -q charge_large -R span[hosts=1] -o out_07 -n 24 -K \
bash os.sh ${curr_dir} {} ${curr_dir}/../../../profile

wait

cd ..

