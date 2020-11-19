#! /bin/bash 

#install OpenMS-2.6.0
#[packages needed: build-essential cmake autoconf patch libtool git automake
#                  qtbase5-dev libqt5svg5-dev libqt5opengl5-dev libboost-regex-dev 
#                  libboost-iostreams-dev libboost-date-time-dev libboost-math-dev 
#                  libboost-random-dev libsvm-dev libglpk-dev zlib1g-dev libxerces-c-dev 
#                  seqan-dev libbz2-dev coinor-libcoinmp-dev libhdf5-dev ]

#build remaining dependencies
mkdir /mnt/software/OpenMS-2.6.0
cd /mnt/software/OpenMS-2.6.0

wget -q -c https://zeroli.oss-cn-hangzhou.aliyuncs.com/Build-images/software/2020-07/OpenMS-2.6.0.tar.gz
tar -zxf ./OpenMS-2.6.0.tar.gz

cd /mnt/software/OpenMS-2.6.0/OpenMS

git submodule update --init contrib

cd ..
mkdir contrib-build
cd ./contrib-build
cmake -DBUILD_TYPE=LIST ../OpenMS/contrib
cmake -DBUILD_TYPE=SEQAN ../OpenMS/contrib
cmake -DBUILD_TYPE=WILDMAGIC ../OpenMS/contrib
cmake -DBUILD_TYPE=EIGEN ../OpenMS/contrib 
(cmake -DBUILD_TYPE=COINOR ../OpenMS/contrib && chown -R root:root /mnt/software/OpenMS-2.6.0/contrib-build/src/CoinMP-1.8.3) || \
(chown -R root:root /mnt/software/OpenMS-2.6.0/contrib-build/src/CoinMP-1.8.3 && cmake -DBUILD_TYPE=COINOR ../OpenMS/contrib)
cmake -DBUILD_TYPE=ALL -DNUMBER_OF_JOBS=4 ../OpenMS/contrib

#Configuring and building OpenMS/TOPP
cd ..
mkdir OpenMS-build
cd ./OpenMS-build
cmake -DOPENMS_CONTRIB_LIBS="/mnt/software/OpenMS-2.6.0/contrib-build" -DBOOST_USE_STATIC=OFF \
-DCMAKE_PREFIX_PATH="/usr;/usr/local" -DWITH_GUI=Off ../OpenMS

make 

echo export LD_LIBRARY_PATH="/mnt/software/OpenMS-2.6.0/OpenMS-build/lib:\$LD_LIBRARY_PATH" >> /root/.bashrc
echo export PATH="\$PATH:/mnt/software/OpenMS-2.6.0/OpenMS-build/bin" >> /root/.bashrc
source /root/.bashrc

make test 

rm /mnt/software/OpenMS-2.6.0/OpenMS-2.6.0.tar.gz
