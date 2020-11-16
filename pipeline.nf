#! /usr/bin/env nextflow

/*
=====================================================================================
                                     pipeline.nf                                      
=====================================================================================
            A Multi-Modal DIA Mass Spectrometry Data Processing Pipeline.             
             https://github.com/xmuyulab/Diamond/blob/master/pipeline.nf                        
=====================================================================================
*/

//please run the command: [ nextflow run pipeline.nf --help ] to print a help message

def helpMessage() {
    log.info """
    
    Usage: \n
    The typical command for running the pipeline is as follows:

        Start the pipeline with the aim to build an assay library:
           \$ nextflow run pipeline.nf --workdir "" --centroid "" --profile "" --fasta "" --windows "" --windowsNumber ""  <Options_library_free> <Functions>
        
        Start the pipeline with providing a ready-made assay library:
           \$ nextflow run pipeline.nf --skipLibGeneration --workdir "" --profile "" --irt "" --lib "" --windows "" <Options_library_based> <Functions>

    ====================
    Mandatory arguments: 
    ====================

        --workdir                   Specify the location of the Diamond folder. For example: --workdir "/path/to/Diamond" (Do not contain a slash at the end!)

        --centroid                  Deliver centroided data. For example: --centroid "/path/to/Diamond/data/centroid/*.mzXML"

        --profile                   Deliver profile data. For example: --profile "/path/to/Diamond/data/profile/*.mzXML"

        --fasta                     Deliver the database file. For example: --fasta "/path/to/Diamond/data/sgs_yeast_decoy.fasta"
    
        --windows                   Deliver the winodws file. For example: --windows "/path/to/Diamond/data/win.tsv.32"

        --windowsNumber             Deliver the number of the windows to select a suitable parameter file for DIA-Umpire. For example: --windowsNumber "32"

        --irt                       Deliver a transition file containing RT normalization coordinates. For example: --irt "/path/to/Diamond/data/irt.TraML"

        --lib                       Deliver a ready-made assay library. For example: --lib "/path/to/Diamond/data/lib.os.TraML"

        --skipLibGeneration         The parameter means the step of building an assay library will be skipped and Diamond's library-based mode will be choosed. No need to give a specific parameter.

    ===============================
    Options_library_free arguments:
    ===============================

        --outdir                    Specify a results folder. For example: --outdir "/path/to/Diamond/outputs" (Do not contain a slash at the end!)  
                                    (Default: the folder named results under the workdir)
        
        --diau_paraNumber           Specify the maximum number of parallel data processing of DIA-Umpire (Default: "4").

        --mgf_mzML_paraNumber       Specify the maximum number of parallel data processing for file format conversion (Default: "4").

        --mzML_part_paraNumber      Specify the maximum number of parallel data processing for dividing mzML files (Default: "4").

        --comet_paraNumber          Specify the maximum number of parallel data processing of Comet searching (Default: "4").

        --tandem_paraNumber         Specify the maximum number of parallel data processing of X!Tandem searching (Default: "20").

        --merge_paraNumber          Specify the maximum number of parallel data processing for merging searching results (Default: "9").

        --xinteract_paraNumber      Specify the maximum number of parallel data processing for xinteract (Default: "30").

        --openSWATH_paraNumber      Specify the maximum number of parallel data processing for openSWATH (Default: "4").

        --pp_paraNumber             Specify the maximum number of parallel data processing for PyProphet (Default: "9").

        --fdr                       The threshold of FDR control (Default: "0.01").

        --pp_score_lambda           The lambda value for storeys method (Default: "0.4").

        --pp_score_statistics_mode  The parameter option of PyProphet (Default: "global"). You can modify it to "local" or "local-global".

    ================================
    Options_library_based arguments:
    ================================

        --outdir                    Specify a results folder. For example: --outdir "/path/to/Diamond/outputs" (Do not contain a slash at the end!) 
                                    (Default: the folder named results under the workdir)
        
        --openSWATH_paraNumber      Specify the maximum number of parallel data processing for openSWATH (Default: "4").

        --pp_paraNumber             Specify the maximum number of parallel data processing for PyProphet (Default: "9").

        --fdr                       The threshold of FDR control (Default: "0.01").

        --pp_score_lambda           The lambda value for storeys method (Default: "0.4").

        --pp_score_statistics_mode  The parameter option of PyProphet (Default: "global"). You can modify it to "local" or "local-global".

    ====================
    Functions arguments:
    ====================
        
        -------------------------------------------------------------------------------------------------------------------------------------
             These parameters are built-in functions of Nextflow, they can generate some visual graphics, which or show the total time 
             consumption of the pipeline, or show the time consumption, memory occupation, cpu usage of each process. Interested can 
             add these parameters to observe relative information.
        -------------------------------------------------------------------------------------------------------------------------------------

        -with-timeline              It renders a timeline.html file that records the time, memory consumption of different processes. 

        -with-report                It generates a report.html file that records the single core CPU Usage, execution time, memory 
                                    occupation and Disk read write information of different processes.
    
        -with-trace                 It creates an execution tracing file that contains some useful information about each process executed 
                                    in your pipeline script, including: submission time, start time, completion time, cpu and memory used.

        -with-dag                   It outputs the pipeline execution DAG. It creates a file named dag.dot containing a textual representation
                                    of the pipeline execution graph in the DOT format.
        
        -resume                     It means only the processes that are actually changed will be re-executed. The execution of the processes 
                                    that are not changed will be skipped and the cached result used instead.

    """.stripIndent()
}

params.help = false
if (params.help) {
    helpMessage()
    exit 0
}


///////////////////////////////////////////////////////////////////////////////
/* --                                                                     -- */
/* --                    SET UP CONFIGURATION VARIABLES                   -- */
/* --                                                                     -- */
///////////////////////////////////////////////////////////////////////////////


params.workdir = null
common_dir = "${params.workdir}/common"

params.outdir = "default"
params.skipLibGeneration = false

//input mzXML files 
params.centroid = null
params.profile = null
params.fasta = null
params.windows = null
//specify a window size, then the corresponding diau.params can be selected later
params.windowsNumber = null

//openswath parameter settings
params.pp_score_statistics_mode = "global"
params.pp_score_lambda = "0.4"
params.fdr = "0.01"
params.irt = null
params.lib = null

//Set the number of parallel processing when computer resources allow
params.diau_paraNumber = 4
params.mgf_mzML_paraNumber = 4
params.mzML_part_paraNumber = 4
params.comet_paraNumber = 4
params.tandem_paraNumber = 20
params.merge_paraNumber = 9
params.xinteract_paraNumber = 30
params.openSWATH_paraNumber = 4
params.pp_paraNumber = 9

//When the command line lacks the corresponding parameter, give the necessary warning
def Warnings() {
    if (params.skipLibGeneration == false) {
        if (params.workdir == null) {
            println "\nError:\n     The working directory cannot be empty, please specify " +  
            "it with a absolute path by adding the --workdir parameter in the command-line!\n"
            exit 0
        }
        if (params.centroid == null) {
            println "\nError:\n     The raw MS data in centroid mode are not found! Please " + 
            "confirm them exist and add --centroid parameter in the command-line!\n"
            exit 0
        }
        if (params.profile == null) {
            println "\nError:\n     The raw MS data in profile mode are not found! Please " + 
            "confirm them exist and add --profile parameter in the command-line!\n"
            exit 0
        }
        if (params.fasta == null) {
            println "\nError:\n     The database file is not found! Please confirm " + 
            "it exists and add --fasta parameter in the command-line!\n"
            exit 0
        }
        if (params.windows == null) {
            println "\nError:\n     The windows file is not found! Please confirm " + 
            "it exists and add --windows parameter in the command-line!\n"
            exit 0
        }
        if (params.windowsNumber == null) {
            println "\nError:\n     The DIA-Umpire parameter file can not be confirmed! " + 
            "Please add --windowsNumber parameter in the command-line and ensure the diau.params " +
            "corresponding to the window size can be found in the common folder!\n"
            exit 0
        }
        if (params.workdir && params.centroid && params.profile && params.fasta && params.windows && params.windowsNumber) {
            println "\nDiamond's library_free mode starts with the aim to build an assay library! The process OpenSWATH_withInputLib " +
            "will be skipped!\n"
        }
    }

    else if (params.skipLibGeneration) {
        if (params.workdir == null) {
            println "\nError:\n     The working directory cannot be empty, please specify " +  
            "it with a absolute path by adding the --workdir parameter in the command-line!\n"
            exit 0
        }
        if (params.profile == null) {
            println "\nError:\n     The raw MS data in profile mode are not found! Please " + 
            "confirm them exist and add --profile parameter in the command-line!\n"
            exit 0
        }
        if (params.irt == null) {
            println "\nError:\n     The irt file is not found! Please confirm them it and " + 
            "add --irt parameter in the command-line!\n"
            exit 0
        }
        if (params.lib == null) {
            println "\nError:\n     The library file is not found! Please confirm it exist " + 
            "and add --lib parameter in the command-line!\n"
            exit 0
        }
        if (params.windows == null) {
            println "\nError:\n     The windows file is not found! Please confirm it exists " +
            "and add --windows parameter in the command-line!\n"
            exit 0
        }
        if (params.workdir && params.profile && params.irt && params.lib && params.windows) {
            println "\nDiamond's library-based mode starts from the process OpenSWATH_withInputLib " +
            "since a ready-made assay library is provided! \n"
        }
    }
}

Warnings()

//Give the raw input data channel
def SetupChannels() {
    if (params.skipLibGeneration == false) {
        Channel.fromPath(params.centroid).set{ centroid_input }
        Channel.fromPath(params.profile).set{ profile_input_01 }
        Channel.fromPath(params.windows).set{ windows_file_01 }
    }

    else if (params.skipLibGeneration) {
        Channel.fromPath(params.profile).set{ profile_input_02 }
        Channel.fromPath(params.windows).set{ windows_file_02 }
        Channel.fromPath(params.irt).set{ input_irt }
        Channel.fromPath(params.lib).set{ input_lib }
    }
}

SetupChannels()

//define the output folder to store the results
def Makeoutdir() {
    if (params.outdir == "default") {
        outputdir = file("${params.workdir}/results")
        outputdir.mkdir()
        outdir = "${params.workdir}/results"
    } 

    else if (params.outdir != "default") {
        outputdir = file("${params.outdir}")
        outputdir.mkdir()
        outdir = "${params.outdir}"
    }
}

Makeoutdir()

//make dirs to store the temporary results of DIA-Umpire
def Makedirs() {

    diau_mgf_dir = file("${outdir}/diau-mgf")
    diau_temp_dir = file("${outdir}/diau-temp")

    diau_mgf_dir.mkdir()
    diau_temp_dir.mkdir()

    mzML_dir = file("${outdir}/mzML")
    mzML_dir.mkdir()

    split_dir = file("${outdir}/mzML/split")
    split_dir.mkdir()

}

if (params.skipLibGeneration == false) {
    Makedirs()
}


///////////////////////////////////////////////////////////////////////////////
/* --                                                                     -- */
/* --                     START THE ANALYSIS PIPELINE                     -- */
/* --                                                                     -- */
///////////////////////////////////////////////////////////////////////////////


//if skipLibGeneration, diau will be skipped
(centroid_mzXMLs) = (params.skipLibGeneration ? [Channel.empty()] : [centroid_input])

//input:mzXML output:mgf
process diau {

    echo true
    cache "deep"
    publishDir path : "${outdir}/diau-mgf", pattern: "*diau.mgf", mode: 'copy'
    publishDir path : "${outdir}/diau-temp", mode: 'copy'
    maxForks params.diau_paraNumber

    input:
    file mzXML from centroid_mzXMLs

    output:
    file "*"
    file "*diau.mgf" into mgf_files

    script:
    """   
    cp ${common_dir}/diau.params.${params.windowsNumber} ./diau.params
    java -Xmx100G -jar /mnt/software/DIA-Umpire/DIA_Umpire_SE.jar $mzXML ./diau.params

    i=`echo $mzXML | awk -F '/' '{print \$NF;}'`
    j=`echo \${i} | sed "s/-Sample[0-9]*//"`

    cat \${j%.mzXML}_Q1.mgf >  \${j%.mzXML}_diau.mgf
    cat \${j%.mzXML}_Q2.mgf >> \${j%.mzXML}_diau.mgf
    cat \${j%.mzXML}_Q3.mgf >> \${j%.mzXML}_diau.mgf
    """
}

//if skipLibGeneration, mgf_mzML will be skipped
(mgf_channel) = (params.skipLibGeneration ? [Channel.empty()] : [mgf_files])

//convert file format from mgf to mzML
process mgf_mzML {

    echo true
    cache "deep"
    publishDir path : "${outdir}/mzML", mode: 'copy'
    maxForks params.mgf_mzML_paraNumber

    input:
    file mgf from mgf_channel.flatten()

    output:
    file "*"
    file "*.mzML" into mzML_files

    script:
    """
    i=`echo $mgf | awk -F'/' '{print \$NF;}'`
    /mnt/software/TPP-5.2.0/tpp/bin/msconvert --mzML --32 --outfile \${i%mgf}mzML $mgf
    """
}

//mzXML_channel for multiple use
mzML_files.into{mzML_files_01; mzML_files_02}

//if skipLibGeneration, mzML_part will be skipped
(mzML_channel_01) = (params.skipLibGeneration ? [Channel.empty()] : [mzML_files_01]) 

//Divide each mzML file into 4 parts on average
process mzML_part {

    echo true 
    cache "deep"
    publishDir path : "${outdir}/mzML/split", mode: 'copy'
    publishDir path : "${outdir}", pattern: "*.split", mode: 'copy'
    maxForks params.mzML_part_paraNumber

    input:
    file mzML from mzML_channel_01.flatten()

    output:
    file "*"
    file "*.mzML" into part_mzML_files
    file "*.split" into split_files

    script:
    """
    i=`echo $mzML | awk -F '/' '{print \$NF;}'`
    /mnt/software/OpenMS-2.6.0/OpenMS-build/bin/MzMLSplitter -no_chrom -log ./\${i}.log -in $mzML -out ./\${i%.mzML} -parts 4

    j=`echo \${i}.log | awk -F '/' '{print \$NF;}'`
    k=\${j%.mzML.log}
    cat \${i}.log | awk -F ' ' '{if (NR>3){print \$6}}' > \${k}.split 
    """
}

part_mzML_files.into{part_mzML_files_01; part_mzML_files_02}

//if skipLibGeneration, searching will be skipped
(part_mzML_channel_01) = (params.skipLibGeneration ? [Channel.empty()] : [part_mzML_files_01])
//comet for search
process comet_search {

    echo true
    cache "deep"
    maxForks params.comet_paraNumber

    input:
    file mzML from part_mzML_channel_01.flatten()

    output:
    file "*comet.pep.xml"  into comet_pepxml_files

    script:
    """
    cp ${params.fasta} ./db.fasta
    ln -s ${common_dir}/comet.params
    /mnt/software/TPP-5.2.0/tpp/bin/comet $mzML
    """
}
//if skipLibGeneration, searching will be skipped
(part_mzML_channel_02) = (params.skipLibGeneration ? [Channel.empty()] : [part_mzML_files_02])
//tandem for search 
process tandem_search {

    echo true 
    cache "deep"
    publishDir path : "${outdir}", pattern: "*.tandem.params", mode: 'copy'
    publishDir path : "${outdir}", pattern: "*.kscore.params", mode: 'copy'
    publishDir path : "${outdir}", pattern: "*.tandem", mode: 'copy'
    publishDir path : "${outdir}", pattern: "*.kscore", mode: 'copy'
    maxForks params.tandem_paraNumber

    input:
    file mzML from part_mzML_channel_02.flatten()

    output:
    file "*"
    file "*tandem.pep.xml" into tandem_pepxml_files
    file "*kscore.pep.xml" into kscore_pepxml_files

    script:
    """
    ln -s ${common_dir}/tandem.xml
    ln -s ${common_dir}/kscore.xml
    ln -s ${common_dir}/taxonomy.xml
    cp ${params.fasta} ./db.fasta

    j=`echo $mzML | awk -F '/' '{print \$NF;}'`
    cat ${common_dir}/tandem.params | sed "s/PATH_TO_SPECTRUM/\$j/" | sed "s/PATH_TO_OUTPUT/\$j.tandem/" > ./\${j}.tandem.params
    cat ${common_dir}/kscore.params | sed "s/PATH_TO_SPECTRUM/\$j/" | sed "s/PATH_TO_OUTPUT/\$j.kscore/" > ./\${j}.kscore.params

    /mnt/software/TPP-5.2.0/tpp/bin/tandem \${j}.tandem.params
    /mnt/software/TPP-5.2.0/tpp/bin/Tandem2XML \${j}.tandem \${j}.tandem.pep.xml

    /mnt/software/TPP-5.2.0/tpp/bin/tandem \${j}.kscore.params
    /mnt/software/TPP-5.2.0/tpp/bin/Tandem2XML \${j}.kscore \${j}.kscore.pep.xml
    """
}

//if skipLibGeneration, MergeResults will be skipped
(mzML_channel_02) = (params.skipLibGeneration ? [Channel.empty()] : [mzML_files_02])
(split_channel) = (params.skipLibGeneration ? [Channel.empty()] : [split_files])
(tandem_pepxml_channel) = (params.skipLibGeneration ? [Channel.empty()] : [tandem_pepxml_files])
(kscore_pepxml_channel) = (params.skipLibGeneration ? [Channel.empty()] : [kscore_pepxml_files])
(comet_pepxml_channel)  = (params.skipLibGeneration ? [Channel.empty()] : [comet_pepxml_files])

//input: .split , all .pep.xml files  
process MergeResults {

    cache "deep"
    publishDir path : "${outdir}", mode: 'copy'

    input:
    file mzML from mzML_channel_02.collect()
    file split from split_channel.collect()
    file tandem_pepxml from tandem_pepxml_channel.collect()
    file kscore_pepxml from kscore_pepxml_channel.collect()
    file comet_pepxml  from comet_pepxml_channel.collect()

    output:
    file "*"
    file "*.pep.xml" into all_pepxml_files

    script:
    """
    ln -s ${common_dir}/MergePep.java 
    javac -encoding UTF-8 -d . MergePep.java  

    ls | grep "[.]split\$" | xargs -P ${params.merge_paraNumber} -i java MergePep ./{}
    """
}

//if skipLibGeneration, fdr_control will be skipped
(pepxml_channel) = (params.skipLibGeneration ? [Channel.empty()] : [all_pepxml_files])

process fdr_control {

    echo true 
    cache "deep"
    publishDir path : "${outdir}", mode: 'copy'

    input:
    file pepxml from pepxml_channel.collect()

    output:
    file "*"
    file "clean*" into clean_pepxml_files
    file "prot.csv" into prot_csv_file

    script:
    """
    cp ${params.fasta} ./db.fasta
    ln -s ${common_dir}/modifications.tsv
    ln -s ${common_dir}/unimod.xml
    ln -s ${common_dir}/ModFilter.java
    ln -s ${common_dir}/PROT2CSV.java
    ln -s ${common_dir}/PTM2CSV.java 
    ln -s ${common_dir}/LibFilter.java 

    #------------------------------------------------ score ---------------------------------------------------------------
    ls | grep "[.]pep[.]xml\$" | xargs -P ${params.xinteract_paraNumber} -i \\
    /mnt/software/TPP-5.2.0/tpp/bin/xinteract -Ddb.fasta -OARPd -THREADS=2 -dDECOY_ -Ninteract-{} {}

    javac -encoding UTF-8 -d . ModFilter.java 
    ls | grep '^interact-' | grep '[.]pep[.]xml\$' | xargs -P ${params.xinteract_paraNumber} -i java ModFilter {}
    ls | grep '^interact-' | grep '[.]pep[.]xml\$' | xargs -P ${params.xinteract_paraNumber} -i sed -i "/^\\s*\$/d" {}
    ls | grep '^interact-' | grep '[.]pep[.]xml\$' | xargs -P ${params.xinteract_paraNumber} -i sed -i "s/^[\\t]*//" {}

    /mnt/software/TPP-5.2.0/tpp/bin/InterProphetParser THREADS=40 DECOY=DECOY_ interact-*.pep.xml ipro.pep.xml

    #------------------------------------------------  fdr  ---------------------------------------------------------------
    perl /mnt/software/TPP-5.2.0/tpp/bin/Mayu.pl -A ./ipro.pep.xml -C db.fasta -E DECOY_ -G 0.01 -H 51 -I 2 -P mFDR -M fdr

    f=`ls | grep '^fdr_main_' | grep '[.]csv\$'`
    line=`cat \${f} | wc -l`

    for c in {3, 14, 20} ; do 
        awk -F ',' -v i=\${c} -v nl=\${line} 'BEGIN{ret=0;}{if (NR>1){ if(\$i>=0.01&&ret==0){ret=\$4;}} \\
        if (NR==nl&&ret==0){ret=\$4;}} END{print ret;}' \${f} >> fdr.txt
    done
    
    #---------------------------------------------- xml2csv ---------------------------------------------------------------
    fdr_pep=2
    FDR=`sed -n "\${fdr_pep}p" fdr.txt`

    javac -encoding UTF-8 -d . PTM2CSV.java
    java PTM2CSV db.fasta ipro.pep.xml ipro.csv \${FDR}

    javac -encoding UTF-8 -d . LibFilter.java
    java LibFilter ipro.pep.xml lib.pep.xml \${FDR}

    /mnt/software/TPP-5.2.0/tpp/bin/ProteinProphet lib.pep.xml lib.xml IPROPHET

    javac -encoding UTF-8 -d . PROT2CSV.java 
    java PROT2CSV lib.prot.xml prot.csv DECOY_
    """
}

//if skipLibGeneration, build_library will be skipped
(clean_pepxml_channel) = (params.skipLibGeneration ? [Channel.empty()] : [clean_pepxml_files])
(prot_csv_channel) = (params.skipLibGeneration ? [Channel.empty()] : [prot_csv_file])

//build assay library for openswath
process build_library {

    echo true 
    cache "deep"
    publishDir path : "${outdir}", mode: 'copy'

    input:
    file clean_pepxml from clean_pepxml_channel.collect()
    file prot_csv from prot_csv_channel

    output:
    file "*"
    file "lib.os.tsv" into lib_os_tsv_file
    file "lib.os.TraML" into lib_os_TraML_file
    file "irt.TraML" into irt_TraML_file

    script:
    """
    ln -s ${common_dir}/irt.txt 
    ln -s ${common_dir}/spectrast2openswath.sh 
    ln -s ${common_dir}/spectrast2peakview.sh 
    ln -s ${common_dir}/spectrast_selfrt.py 
    ln -s ${common_dir}/modifications.tsv 
    ln -s ${common_dir}/spectrast.usermods 
    ln -s ${common_dir}/lib.update.sh 
    cp ${params.windows} ./win.tsv

    
    for chg in {2..5} ; do 
        list=`ls | grep -E "^clean-\${chg}-interact-" | grep -E "[.]pep[.]xml\$"`
        for i in \${list} ; do 
            j=`echo \${i} | sed "s/^clean-\${chg}-interact-//" | sed "s/[.]pep[.]xml\$//" | sed "s/[.]/_/g"`
            /mnt/software/TPP-5.2.0/tpp/bin/spectrast -M ./spectrast.usermods -c_BIN! \\
            -cNorig-\${chg}-\${j} -cICID-QTOF -cP0.0 ./\${i}
        done &
    done 
    wait
    
    ls | grep -E  "^orig-" | grep -E "[.]splib\$" | xargs \\
    /mnt/software/TPP-5.2.0/tpp/bin/spectrast -M ./spectrast.usermods -c_BIN! -cNorig -cJU

    #enter the virtualenv: spectrast_selfrt
    /mnt/software/anaconda3/envs/spectrast_selfrt/bin/python2.7 ./spectrast_selfrt.py -i orig.splib -o irt.splib -k ./irt.txt  
    
    /mnt/software/TPP-5.2.0/tpp/bin/spectrast -M ./spectrast.usermods -c_BIN! -cICID-QTOF -cAC -cNbuild irt.splib 

    bash lib.update.sh build.splib lib.splib 

    bash spectrast2peakview.sh     #-i orig.splib -o lib.peakview.tsv modifications and win.tsv are needed

    bash spectrast2openswath.sh    #-i orig.splib -o lib.os.tsv, modifications and win.tsv are needed

    /mnt/software/OpenMS-2.6.0/OpenMS-build/bin/TargetedFileConverter -in lib.os.tsv -out lib.TraML -algorithm:force_invalid_mods  
    /mnt/software/OpenMS-2.6.0/OpenMS-build/bin/OpenSwathDecoyGenerator -in lib.TraML -out lib.os.TraML -method shuffle 

    cat $prot_csv | grep -v  "DECOY_" | grep -v "cont_" | awk -F ',' '{printf("%09d\\t%s\\n",\$3,\$1);}' | \\
    sort -r | head -n 3 | awk -F '\\t' '{print \$2;}' > irt.tmp

    cat lib.os.tsv | head -n 1 > irt.os.tsv
    cat lib.os.tsv | grep -f ./irt.tmp | grep -v "DECOY_" | sort >> irt.os.tsv 

    /mnt/software/OpenMS-2.6.0/OpenMS-build/bin/TargetedFileConverter -in irt.os.tsv -out irt.TraML -algorithm:force_invalid_mods
    """
}

//if not skipLibGeneration, library will be built
(profile_mzXMLs_01) = (params.skipLibGeneration ? [Channel.empty()] : [profile_input_01])
(irt_TraML_channel) = (params.skipLibGeneration ? [Channel.empty()] : [irt_TraML_file])
(lib_os_TraML_channel) = (params.skipLibGeneration ? [Channel.empty()] : [lib_os_TraML_file])
(windows_channel_01) = (params.skipLibGeneration ? [Channel.empty()] : [windows_file_01])

//openswath, with a spectral library built above
process OpenSWATH_withBuiltLib {

    echo true 
    cache "deep"
    publishDir path : "${outdir}", mode: 'copy'

    input:
    file profile_mzXML from profile_mzXMLs_01.collect()
    file irt_TraML from irt_TraML_channel
    file lib_os_TraML from lib_os_TraML_channel
    file windows_file from windows_channel_01

    output:
    file "*"
    file "*mzXML.tsv" into mzXML_tsv_channel_01

    script:
    """
    echo -e "start\\tend" > win.os.tsv
    cat $windows_file >> win.os.tsv

    find ./ -name '*.mzXML' | while read i ; do
        j=`echo \${i} | awk -F '/' '{print \$NF;}'`
        echo \${j}
    done | xargs -P ${params.openSWATH_paraNumber} -i \\
    /mnt/software/OpenMS-2.6.0/OpenMS-build/bin/OpenSwathWorkflow -in ./{} \\
    -tr ./$lib_os_TraML -threads 20 -sort_swath_maps -readOptions workingInMemory -rt_extraction_window \\
    1200 -batchSize 0 -tr_irt ./$irt_TraML -swath_windows_file ./win.os.tsv -out_tsv ./{}.tsv
    """
}

//if skipLibGeneration, irt, library and windows file are given in the command line
(profile_mzXMLs_02) = (params.skipLibGeneration ?  [profile_input_02] : [Channel.empty()])
(given_irt_channel) = (params.skipLibGeneration ? [input_irt] : [Channel.empty()])
(given_lib_channel) = (params.skipLibGeneration ? [input_lib] : [Channel.empty()])
(windows_channel_02) = (params.skipLibGeneration ? [windows_file_02] : [Channel.empty()])

//openswath, with an input spectral library
process OpenSWATH_withInputLib {

    echo true 
    cache "deep"
    publishDir path : "${outdir}", mode: 'copy'

    input:
    file profile_mzXML from profile_mzXMLs_02.collect()
    file irt from given_irt_channel
    file library from given_lib_channel
    file windows_file from windows_channel_02

    output:
    file "*"
    file "*mzXML.tsv" into mzXML_tsv_channel_02

    script:
    """
    echo -e "start\\tend" > win.os.tsv
    cat $windows_file >> win.os.tsv

    find ./ -name '*.mzXML' | while read i ; do
        j=`echo \${i} | awk -F '/' '{print \$NF;}'`
        echo \${j}
    done | xargs -P ${params.openSWATH_paraNumber} -i \\
    /mnt/software/OpenMS-2.6.0/OpenMS-build/bin/OpenSwathWorkflow -in ./{} \\
    -tr ./$library -threads 20 -sort_swath_maps -readOptions workingInMemory -rt_extraction_window \\
    1200 -batchSize 0 -tr_irt ./$irt -swath_windows_file ./win.os.tsv -out_tsv ./{}.tsv 
    """  
}

(mzXML_tsv_channel) = (params.skipLibGeneration ? [mzXML_tsv_channel_02] : [mzXML_tsv_channel_01])

//score the results of openswath
process PyProphet {

    echo true 
    cache "deep"
    publishDir path : "${outdir}", mode: 'copy'

    input:
    file mzXML_tsv from mzXML_tsv_channel.collect()

    output: 
    file "*"
    file "*_pp.tsv" into pp_tsv_channel

    script:
    """
    dataFolder="./"
    workFolder="./tmp_pp/"
    resultFolder="./pp/"   
    dataFilenamePattern="*.mzXML.tsv"

    jobCount=`ls ${params.profile} | wc -l` 
    sampleFactor=`echo "scale=6;1.0/\${jobCount}" | bc`
    
    mkdir \${workFolder}
    mkdir \${resultFolder}

    for ((jobNumber=1; jobNumber<=\${jobCount}; ++jobNumber)) ; do 
        mkdir \${resultFolder}\${jobNumber} 
    done

    /mnt/software/anaconda3/envs/pyprophet/bin/pyprophet-cli prepare --data-folder=\${dataFolder} \\
    --work-folder=\${workFolder} --data-filename-pattern=\${dataFilenamePattern} \\
    --separator="tab" --extra-group-column="ProteinName"

    for ((jobNumber=1; jobNumber<=\${jobCount}; ++jobNumber)) ; do
        echo \${jobNumber}
    done | xargs -P ${params.pp_paraNumber} -i \\
        /mnt/software/anaconda3/envs/pyprophet/bin/pyprophet-cli subsample --data-folder=\${dataFolder} \\
        --work-folder=\${workFolder} --data-filename-pattern=\${dataFilenamePattern}  --separator="tab" \\
        --job-number {} --job-count \${jobCount} --sample-factor=\${sampleFactor} 

    /mnt/software/anaconda3/envs/pyprophet/bin/pyprophet-cli learn --work-folder=\${workFolder} \\
    --separator="tab" --ignore-invalid-scores

    for ((jobNumber=1; jobNumber<=\${jobCount}; ++jobNumber)) ; do
        echo \${jobNumber}
    done | xargs -P ${params.pp_paraNumber} -i \\
        /mnt/software/anaconda3/envs/pyprophet/bin/pyprophet-cli apply_weights --data-folder=\${dataFolder} \\
        --work-folder=\${workFolder} --data-filename-pattern=\${dataFilenamePattern} --separator="tab" \\
        --job-number {} --job-count \${jobCount}

    for ((jobNumber=1; jobNumber<=\${jobCount}; ++jobNumber)) ; do
        echo \${jobNumber}
    done | xargs -P ${params.pp_paraNumber} -i \\
        /mnt/software/anaconda3/envs/pyprophet/bin/pyprophet-cli score --data-folder=\${dataFolder} \\
        --work-folder=\${workFolder} --data-filename-pattern=\${dataFilenamePattern}  \\
        --result-folder=\${resultFolder}{}/ --separator="tab" --job-number {} \\
        --job-count \${jobCount} --lambda=${params.pp_score_lambda} --statistics-mode=${params.pp_score_statistics_mode} 

    find \${resultFolder} -name '*mzXML_scored.txt' | xargs -P 25 -i ln -s {}

    scored_txt=`ls | grep "mzXML[_]scored[.]txt\$"`
    for i in \${scored_txt} ; do 
        j=`echo \${i%scored.txt}pp.tsv | awk -F '/' '{print \$NF;}'`
        awk -F '\\t' '{if (NR==1||\$58 < ${params.fdr}) print \$0;}' \${i} > ./\${j}
        sed -i 's/transition_group_id_m_score/m_score/' ./\${j} &
    done
    """
}

process tric {
     
    cache "deep"
    publishDir path : "${outdir}", mode: 'copy'

    input:
    file pp_tsv from pp_tsv_channel.collect()

    output:
    file "*"

    script:
    """
    ln -s ${common_dir}/TricParser.java 

    find . -name '*.mzXML_pp.tsv' | xargs feature_alignment.py --out aligned.tsv --method LocalMST \\
    --realign_method lowess_cython --max_rt_diff 60 --mst:useRTCorrection True --mst:Stdev_multiplier 3.0 \\
    --target_fdr 0.01 --max_fdr_quality 0.05 --verbosity 1 --alignment_score 0.0001 --in 

    javac -encoding UTF-8 -d . TricParser.java 
    java TricParser aligned.tsv
    """
}
