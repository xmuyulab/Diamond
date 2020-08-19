#! /bin/bash

#install DIA-Umpire
#[packages needed: wget unzip]
mkdir /mnt/software/DIA-Umpire
cd /mnt/software/DIA-Umpire

wget -q -c https://zeroli.oss-cn-hangzhou.aliyuncs.com/Build-images/software/2020-07/DIA-Umpire_v2_0.zip
unzip ./DIA-Umpire_v2_0.zip
#remove the installation packages 
rm ./DIA-Umpire_v2_0.zip