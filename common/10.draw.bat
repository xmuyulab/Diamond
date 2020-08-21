
#!/bin/bash

if [ ! -d ./work/ ] ; then
        mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../../common/PrepareDraw.java .

ln -s ../../common/UpdateReport.java .

ln -s ../../common/xic2jpg.R .

N=`sed -n "1p" ./flag.txt|awk -F'=' '{print $2}'`

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`

rm -rf ../draw

mkdir ../draw


javac -d . PrepareDraw.java

bsub -R span[hosts=1] -q ${Q} -o out_10 -n 24 -K \
java -Xmx100G PrepareDraw ../draw/ aligned.tsv lib.os.tsv 300

NR=`ls ../draw/*.xic|wc -l`

find ../draw -name '*.xic'|xargs -P ${N} -i \
bsub -R span[hosts=1] -q ${Q} -o out_10 -n 24 -K \
java -Xmx100G PrepareDraw ../draw/ {} win.os.tsv 

wait

javac -d . UpdateReport.java

bsub -R span[hosts=1] -q ${Q} -o out_10 -n 1 -K \
java -Xmx64G UpdateReport ../draw/ aligned.tsv 

#"remove # in next line skip draw"
#exit

bsub -R span[hosts=1] -q ${Q} -o out_10 -n 1 -K \
java -Xmx100G PrepareDraw ../draw/ 

cat ./.xic2jpg.R |sed "s/TOTAL_RUN_COUNT/${NR}/" > xic2jpg.R

bsub -R span[hosts=1] -q ${Q} -o out_10 -n 24 -K \
Rscript ./xic2jpg.R ../draw/


