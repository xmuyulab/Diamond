#! /bin/bash

#install nextflow
mkdir /mnt/software
mkdir /mnt/software/nextflow

cd /mnt/software/nextflow

wget -qO- https://get.nextflow.io | bash 

echo "PATH=\$PATH:/mnt/software/nextflow" >> /root/.bashrc
source /root/.bashrc
