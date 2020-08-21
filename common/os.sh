
mkdir  /tmp/openswath/

rm -rf /tmp/openswath/*

#~/software/OpenMS-2.2.0/bin/OpenSwathWorkflow -in $1/../profile/$2 -tr $1/lib.os.TraML -threads 24 -sort_swath_maps -readOptions cacheWorkingInMemory -tempDirectory /tmp/openswath/  -rt_extraction_window 1200 -batchSize 0  -tr_irt $1/irt.TraML -swath_windows_file $1/win.os.tsv -out_tsv $1/$2.tsv
~/software/OpenMS/bin/OpenSwathWorkflow -in $3/$2 -tr $1/lib.os.TraML -threads 24 -sort_swath_maps -readOptions workingInMemory -rt_extraction_window 1200 -batchSize 0 -tr_irt $1/irt.TraML -swath_windows_file $1/win.os.tsv -out_tsv $1/$2.tsv


#-out_osw $1/$2.osw 
#-out_tsv $1/$2.tsv

rm -rf /tmp/openswath/*

