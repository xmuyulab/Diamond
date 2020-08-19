#!/bin/bash

if [ ! -d ./work/ ] ; then
        mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../../common/mscore.sh .

N=`sed -n "1p" ./flag.txt|awk -F'=' '{print $2}'`

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`

dataFolder="./"
workFolder="./tmp_pp/"
resultFolder="./pp/"
dataFilenamePattern="*.mzXML.tsv"

jobCount=`ls ../../../profile|grep -E '[.]mzXML$'|wc -l`
sampleFactor=`echo "scale=6;1.0/${jobCount}"|bc`

rm -rf ${workFolder} ${resultFolder}
mkdir ${workFolder}
mkdir ${resultFolder}


for ((jobNumber=1;jobNumber<=${jobCount};++jobNumber)); do
mkdir ${resultFolder}${jobNumber}
done


bsub -R span[hosts=1] -q ${Q} -o out_08 -n 1 -K \
pyprophet-cli prepare --data-folder=${dataFolder} --data-filename-pattern=${dataFilenamePattern} --work-folder=${workFolder} --separator="tab" --extra-group-column="ProteinName"

for ((jobNumber=1;jobNumber<=${jobCount};++jobNumber)); do
bsub -R span[hosts=1] -q ${Q} -o out_08 -n 1 -K \
pyprophet-cli subsample --data-folder=${dataFolder} --data-filename-pattern=${dataFilenamePattern} --work-folder=${workFolder} --separator="tab" --job-number ${jobNumber} --job-count ${jobCount} --sample-factor=${sampleFactor} &
done

wait

bsub -R span[hosts=1] -q ${Q} -o out_08 -n 1 -K \
pyprophet-cli learn --work-folder=${workFolder} --separator="tab" --ignore-invalid-scores

for ((jobNumber=1;jobNumber<=${jobCount};++jobNumber)); do
bsub -R span[hosts=1] -q ${Q} -o out_08 -n 1 -K \
pyprophet-cli apply_weights --data-folder=${dataFolder} --data-filename-pattern=${dataFilenamePattern} --work-folder=${workFolder} --separator="tab" --job-number ${jobNumber} --job-count ${jobCount} &
done

wait

for ((jobNumber=1;jobNumber<=${jobCount};++jobNumber)); do
bsub -R span[hosts=1] -q ${Q} -o out_08 -n 1 -K \
pyprophet-cli score --data-folder=${dataFolder} --data-filename-pattern=${dataFilenamePattern} --work-folder=${workFolder} --result-folder=${resultFolder}${jobNumber}/ --separator="tab" --job-number ${jobNumber} --job-count ${jobCount} --lambda=0.4 --statistics-mode=global &
done

wait

find ${resultFolder} -name '*.mzXML_scored.txt'|xargs -P 24 -i \
bsub -R span[hosts=1] -q ${Q} -o out_08 -n 1 -K \
bash mscore.sh `pwd` {}



cd ..


