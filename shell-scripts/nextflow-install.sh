#! /bin/bash

#install nextflow
mkdir /mnt/software
mkdir /mnt/software/nextflow

cd /mnt/software/nextflow

wget -qO- https://get.nextflow.io | bash 

#将nextflow添加到环境变量
echo "PATH=\$PATH:/mnt/software/nextflow" >> /root/.bashrc
source /root/.bashrc