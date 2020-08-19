#! /bin/bash 

#install TPP-5.2.0
mkdir /mnt/software/TPP-5.2.0
mkdir /mnt/software/TPP-5.2.0/tpp
mkdir /mnt/software/TPP-5.2.0/data
mkdir /mnt/software/TPP-5.2.0/svn

cd /mnt/software/TPP-5.2.0/svn/
wget -q -c https://zeroli.oss-cn-hangzhou.aliyuncs.com/Build-images/software/2020-07/TPP_5.2.0-src.tgz
tar -zxf ./TPP_5.2.0-src.tgz

cd /mnt/software/TPP-5.2.0/svn/release_5-2-0

echo "INSTALL_DIR = /mnt/software/TPP-5.2.0/tpp" > site.mk
echo "TPP_BASEURL = /home/lcx" >> site.mk
echo "TPP_DATADIR = /mnt/software/TPP-5.2.0/data" >> site.mk

make libgd
make all 
make install 

sh -c '/bin/echo "yes" | cpan make install' 
sh -c '/bin/echo -e "\n" | cpan install Bundle::CPAN'
cpan install CGI 
cpan install XML::Parser
cpan install FindBin::libs
sh -c '/bin/echo "y" | cpan install JSON'

#add env variable for easy use of TPP
echo export PATH="\$PATH:/mnt/software/TPP-5.2.0/tpp/bin" >> /root/.bashrc 
source /root/.bashrc
