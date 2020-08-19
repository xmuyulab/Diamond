FROM ubuntu:16.04 

#install packages and mkdir
ADD ./shell-scripts/*.sh /mnt/shell-scripts/
RUN bash /mnt/shell-scripts/packages-install.sh 

#install java 
RUN bash /mnt/shell-scripts/java-install.sh
ENV JAVA_HOME=/usr/local/jdk/jdk1.8.0_251 \
    JRE_HOME=/usr/local/jdk/jdk1.8.0_251/jre \
    PATH=$PATH:$JAVA_HOME/bin

#install nextflow and DIA-Umpire
RUN bash /mnt/shell-scripts/nextflow-install.sh && \
    bash /mnt/shell-scripts/diau-install.sh

#install cmake-3.17
RUN bash /mnt/shell-scripts/cmake-install.sh

#install TPP-5.2.0
RUN bash /mnt/shell-scripts/TPP-install.sh 

#install OpenMS-2.6.0
RUN bash /mnt/shell-scripts/OpenMS-install.sh && \
    chown -R root:root /mnt/software/OpenMS-2.6.0/contrib-build/src/CoinMP-1.8.3

#install anaconda3, packages 
RUN bash /mnt/shell-scripts/anaconda3-install.sh && \
    bash /mnt/shell-scripts/spectrast_selfrt.sh  && \
    bash /mnt/shell-scripts/pyprophet-install.sh

RUN rm -r /mnt/shell-scripts && cd /

                
