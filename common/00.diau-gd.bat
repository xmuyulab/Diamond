#!/bin/bash 

if [ ! -d ./work/ ] ; then
	mkdir -p ./work/
fi


cd ./work

wdir=`pwd`

cat ../../common/flag.txt ../flag.txt > flag.txt

ln -s ../../common/diau.params .

if [ $1 = "true" ] ; then 

ln -s ../../common/gd.txt .

ln -s ../../common/align.sh .

ln -s ../../common/gd.sh .

ln -s ../../common/parse_mgf.pl .

ln -s ../../common/FeatureXMLGenerator.py .

ln -s ../../common/win.tsv .

fi

N=`sed -n "1p" ./flag.txt|awk -F'=' '{print $2}'`

Q=`sed -n "2p" ./flag.txt|awk -F'=' '{print $2}'`

rm -rf ../../diau-mgf
rm -rf ../../diau-temp

mkdir -p ../../diau-mgf/
mkdir -p ../../diau-temp/

rm -rf ../../gd-temp
rm -rf ../../gd-mgf

if [ $1 = "true" ] ; then

mkdir -p ../../gd-temp/
mkdir -p ../../gd-mgf/raw

fi

cd ../../diau-temp

if [ -d ../../centroid/ ]; then

	for i in ../../centroid/*.mzXML ; do

		j=`echo ${i}|awk -F'/' '{print $NF;}'`

		k=`echo ${j}|sed "s/-Sample[0-9]*//"`

		ln -s ${i} ./${k}

	done
fi

xml=`ls |grep "[.]mzXML$"`

for i in ${xml}; do
echo ${i}
done |xargs -P ${N} -i \
bsub -R span[hosts=1] -q ${Q} -o out_00 -n 24 -K \
java -Xmx100G -jar ~/software/DIA-Umpire/DIA_Umpire_SE.jar {} ${wdir}/diau.params


for j in ${xml}; do

cat ${j%.mzXML}_Q1.mgf >  ${j%.mzXML}_diau.mgf
cat ${j%.mzXML}_Q2.mgf >> ${j%.mzXML}_diau.mgf
cat ${j%.mzXML}_Q3.mgf >> ${j%.mzXML}_diau.mgf

mv ${j%.mzXML}_diau.mgf ${wdir}/../../diau-mgf/

 
if [ $1 = "true" ] ; then

python ${wdir}/FeatureXMLGenerator.py ${j%.mzXML}_PeakCluster.csv ${j%.mzXML}_ms1scan.featureXML mz1 PeakHeightRT1 Charge -m

mv ${j%.mzXML}_ms1scan.featureXML ${wdir}/../../gd-temp/

fi

done


if [ $1 = "true" ] ; then

cd ${wdir}

cd ../../gd-temp

ls |grep -E "[.]mzXML$" | xargs rm

if [ -d ../../profile/ ]; then

	for i in ../../profile/*.mzXML ; do

		j=`echo ${i}|awk -F'/' '{print $NF;}'`

		k=`echo ${j}|sed "s/-Sample[0-9]*//"`		

		ln -s ${i} ./${k}		
	done

fi

xml=`ls |grep "[.]mzXML$"`

for i in ${xml}; do
echo ${i}
done |xargs -P 24 -i \
bsub -R span[hosts=1] -q ${Q} -o out_00 -n 1 -K \
java -jar ~/software/liswath/bin/SplitXml.jar {} ./

for i in ${xml}; do
rm ${i}
done

cd ${wdir}

cp gd.txt gd.params

find ../../gd-temp -name '*_ms1scan.mzXML' >> gd.params

bsub -R span[hosts=1] -q ${Q} -o out_00 -n 24 -K \
bash align.sh		  

win_cnt=`cat win.tsv|wc -l`
for ((i=0;i<${win_cnt};++i)); do 
echo ${i}
done |xargs -P ${N} -i \
bsub -R span[hosts=1] -q ${Q} -o out_00 -n 24 -K \
bash gd.sh {}

bsub -R span[hosts=1] -q ${Q} -o out_00 -n 1 -K \
perl parse_mgf.pl

fi

cd ${wdir}/..


