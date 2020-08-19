#! /bin/bash 

#install anaconda3

cd /mnt/software
#wget -q参数不显示下载或安装进度条
wget -q -c https://repo.anaconda.com/archive/Anaconda3-2020.02-Linux-x86_64.sh
sh -c '/bin/echo -e "\nyes\n\/mnt\/software\/anaconda3\nyes\n" | bash Anaconda3-2020.02-Linux-x86_64.sh' 

echo export PATH="/mnt/software/anaconda3/bin:\$PATH" >> /root/.bashrc 
source /root/.bashrc
#remove the installation packages 
rm ./Anaconda3-2020.02-Linux-x86_64.sh

apt-get install --allow-unauthenticated -y python-dev build-essential \
libssl-dev libffi-dev libxml2-dev libxslt1-dev zlib1g-dev 

/mnt/software/anaconda3/bin/pip install numpy pandas scipy pymzml==0.7.8 Biopython Cython matplotlib msproteomicstools cylowess
