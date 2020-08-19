#! /bin/bash 
#install packages

apt-get update --fix-missing
apt-get upgrade --allow-unauthenticated -y
apt-get install --allow-unauthenticated -y less vim wget unzip git tree gcc g++ \
build-essential libssl-dev make subversion zlib1g-dev libghc-bzlib-dev gnuplot \
expat libexpat1-dev autoconf patch libtool automake qtbase5-dev libqt5svg5-dev \
libqt5opengl5-dev libboost-regex-dev libboost-iostreams-dev libboost-date-time-dev \
libboost-math-dev libboost-random-dev libsvm-dev libglpk-dev libxerces-c-dev seqan-dev \
libbz2-dev coinor-libcoinmp-dev libhdf5-dev perl cmake-curses-gui bc 

if [ ! -d /home/lcx ] ; then 
    mkdir /home/lcx
fi 

