#!/bin/bash 

if [ ! -d ./work/ ] ; then
	mkdir -p ./work/
fi

cd ./work

cat ../../common/flag.txt ../flag.txt > flag.txt

N=`sed -n "1p" ./flag.txt|awk -F'=' '{print $2}'`

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`


if [ ! -d ../../mzML/ ] ; then
	mkdir -p ../../mzML/ 
fi


if [ -d ../../diau-mgf/ ]; then

	for i in ../../diau-mgf/*.mgf ; do
	
		j=`echo ${i}|awk -F'/' '{print $NF;}'`
	
				
		bsub -R span[hosts=1] -q ${Q} -o out_01 -n 1 -K \
~/software/TPP/bin/msconvert --mzML --32 -o ../../mzML/ --outfile ${j%mgf}mzML $i &
		sleep 0.1
			
		
	done	
fi

if [ -d ../../gd-mgf/ ]; then
	
	for i in ../../gd-mgf/*.mgf ;do 
		
		j=`echo ${i}|awk -F'/' '{print $NF;}'`

		bsub -R span[hosts=1] -q ${Q} -o out_01 -n 1 -K \
~/software/TPP/bin/msconvert --mzML --32 -o ../../mzML/ --outfile ${j%mgf}mzML $i &
	        sleep 0.1

	done

fi

wait

rm -rf ../../mzML/split

mkdir -p ../../mzML/split

for i in ../../mzML/*.mzML ; do

	j=`echo ${i}|awk -F'/' '{print $NF;}'`

	bsub -R span[hosts=1] -q ${Q} -o out_01 -n 1 -K \
~/software/OpenMS/bin/MzMLSplitter -no_chrom -log ../../mzML/split/${j}.log  -in ${i} -out ../../mzML/split/${j%.mzML} -parts ${N} &

done


wait



cd ..
