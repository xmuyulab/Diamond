#! /bin/bash

# install java 
mkdir /usr/local/jdk
cd /usr/local/jdk
wget -q -c https://zeroli.oss-cn-hangzhou.aliyuncs.com/Build-images/software/2020-07/jdk-8u251-linux-x64.tar.gz
tar -zxf ./jdk-8u251-linux-x64.tar.gz

echo "JAVA_HOME=/usr/local/jdk/jdk1.8.0_251" >> /root/.bashrc
echo "JRE_HOME=/usr/local/jdk/jdk1.8.0_251/jre" >> /root/.bashrc
echo "PATH=\$PATH:\$JAVA_HOME/bin" >> /root/.bashrc

source /root/.bashrc

rm ./jdk-8u251-linux-x64.tar.gz