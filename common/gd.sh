
mkdir /tmp/lyy

rm -rf /tmp/lyy/*

win_num=`printf "%03d" $1`

~/software/liswath/bin/analysis_swath -in gd.params -threads 24 -max 1000 -win ${win_num}

rm -rf /tmp/lyy/*


