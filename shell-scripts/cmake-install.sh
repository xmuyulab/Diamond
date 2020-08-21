#! /bin/bash 

#install cmake-3.17.3
#[packages needed: gcc g++ build-essential libssl-dev]
mkdir /mnt/software/cmake-3.17
cd /mnt/software/cmake-3.17

wget -q -c https://zeroli.oss-cn-hangzhou.aliyuncs.com/Build-images/software/2020-07/cmake-3.17.3.tar.gz
tar -zxf ./cmake-3.17.3.tar.gz

cd /mnt/software/cmake-3.17/cmake-3.17.3

./bootstrap 
make 
make install