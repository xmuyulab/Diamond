# Flowchart of Diamond
![image](https://github.com/xmuyulab/Diamond/blob/master/images/fig01.png)
In the suitation when an assay library is not available, we choose Diamond's library-free mode, as the blue route shown in the picture above, an assay library will be generated first and in the suitation when an assay library is available, we choose the Diamond's library-based mode, as the green route shown in the picture above, the library building step will be skipped.

# An Instruction on the Analysis of Example Datasets
## Data preparation
First, please execute the following command in your terminal to clone the Diamond repository from [my GitHub](https://github.com/xmuyulab/Diamond) to your own machine. 
```shell
git clone https://github.com/xmuyulab/Diamond.git
```
Then , download the example MS data. Provided here are the three mzXML files in profile mode in SWATH-MS Gold Standard (SGS) data of yeast, which are available from the PeptideAtlas raw data repository with accession number [PASS00289](http://www.peptideatlas.org/PASS/PASS00289) and the three mzXML files in centroid mode, which can be obtained by preprocessing the profile data with [ProteoWizard](http://proteowizard.sourceforge.net/download.html).

(1) Three profile data files: please visit [PASS00289](http://www.peptideatlas.org/PASS/PASS00289), click on the link "ftp://PASS00289:XY4524h@ftp.peptideatlas.org/" at the bottom of the page, select the three files `napedro_L120228_00{1,2,3}_SW.mzXML.gz` under the `/SGS/mzxml` folder, download and store them in the `/Diamond/data/profile` folder. Note that the profile files are in a compressed format, so execute the following commands to decompress them.
```shell
cd /path/to/Diamond/data/profile
gunzip ./napedro_L120228_00{1,2,3}_SW.mzXML.gz
```
(2) Three centroid data files: please visit [cMS01](https://zeroli.oss-cn-hangzhou.aliyuncs.com/SGS_data_yeast/centroid/napedro_L120228_001_SW.mzXML.gz), [cMS02](https://zeroli.oss-cn-hangzhou.aliyuncs.com/SGS_data_yeast/centroid/napedro_L120228_002_SW.mzXML.gz), [cMS03](https://zeroli.oss-cn-hangzhou.aliyuncs.com/SGS_data_yeast/centroid/napedro_L120228_003_SW.mzXML.gz) respectively, download and store them in the `/Diamond/data/centroid` folder. Note that the centroid files are in a compressed format, so execute the following commands to decompress them.
```shell
cd /path/to/Diamond/data/centroid
gunzip ./napedro_L120228_00{1,2,3}_SW.mzXML.gz
```
(3) The library file, irt file, windows file and database file have been stored in the `/Diamond/data/` folder. Note that the library file and the irt file are in a compressed format, so execute the following commands to decompress them.
```shell
cd /path/to/Diamond/data
tar -zxvf ./library.TraML.tar.gz
tar -zxvf ./irt.TraML.tar.gz
```
After all the data is ready, an example tree structure diagram of the `/Diamond/data` folder is as follows:

![image](https://github.com/xmuyulab/Diamond/blob/master/images/data-folder-struction.png)

## Diamond acquisition
Diamond is containerized by Docker into an image, the installation tutorial of Docker is described in the [Docker documentation](https://docs.docker.com/engine). On your machine, please start a Terminal session and then execute the following command within the console:
```shell
docker pull zeroli/diamond:1.0
```
This will take a few minutes to pull the Diamond image from [Docker Hub](https://hub.docker.com/r/zeroli/diamond/) to your machine. You can check whether the image `zeroli/diamond:1.0` is successfully pulled by executing `docker images`, and if successfully, it will appear in the images list.  

## Container creation and startup
Create a container (named diamond_test) based on the image `zeroli/diamond:1.0` and simultaneously mount the local folder `/path/to/Diamond` to the folder `/mnt/Diamond` (in the container) by running the following command in your terminal:
```shell
docker run -it --name diamond_test -v /path/to/Diamond/:/mnt/Diamond/ zeroli/diamond:1.0 bash
```
After the above command is executed, you will enter the container. Please switch to the folder `/mnt/Diamond` by executing `cd /mnt/Diamond` in your terminal.

**Note:** Type in `exit` and press `Enter`, or hit `Ctrl+D` to exit the container. Please follow the commands below to re-enter the container after exiting:
```shell
docker start diamond_test
docker exec -it diamond_test bash
```

## Data analysis
The Nextflow script is saved as a `pipeline.nf` file in the `Diamond` folder. Diamond's two modes: library-free and library-based execution commands are as follows.
### Library-free mode
Execute the following command in your terminal to start the analysis of MS data with the aim to build an assay library:
```shell
nextflow run /mnt/Diamond/pipeline.nf --workdir "/mnt/Diamond" --centroid "/mnt/Diamond/data/centroid/*.mzXML" --profile "/mnt/Diamond/data/profile/*.mzXML" --fasta "/mnt/Diamond/data/sgs_yeast_decoy.fasta" --windows "/mnt/Diamond/data/win.tsv.32" --windowsNumber "32"
```
The MS data processing results will be stored in the folder named `results` under `/mnt/Diamond` by default. Please refer to the **Help Message** section or execute `nextflow run /mnt/Diamond/pipeline.nf --help` in the container to view the detailed information of parameter passing.
### Library-based mode
Execute the following command in your terminal to start the analysis of MS data by providing an assay library:
```shell
nextflow run /mnt/Diamond/pipeline.nf --skipLibGeneration --workdir "/mnt/Diamond" --profile "/mnt/Diamond/data/profile/*.mzXML" --lib "/mnt/Diamond/data/library.TraML" --irt "/mnt/Diamond/data/irt.TraML" --windows "/mnt/Diamond/data/win.tsv.32"
```
The `--skipLibGeneration` parameter means the process of building an assay library will be skipped. The data processing results will be also stored in the folder named `results` under `/mnt/Diamond` by default. Please refer to the **Help Message** section or execute `nextflow run /mnt/Diamond/pipeline.nf --help` in the container to view the detailed information of parameter passing.

# Help Message
Two different execution-commands for the two different modes of Diamond. This help message can also be obtained by executing the following command in the container：
```shell
nextflow run /mnt/Diamond/pipeline.nf --help
```
## Library-free mode:
```
nextflow run /mnt/Diamond/pipeline.nf --workdir "" --centroid "" --profile "" --fasta "" --windows "" --windowsNumber "" <Options_library_free> <Functions>
```
## Library-based mode: 
```
nextflow run /mnt/Diamond/pipeline.nf --skipLibGeneration --workdir "" --profile "" --lib "" --irt "" --windows "" <Options_library_based> <Functions>
```

## Parameters descriptions
### Mandatory arguments
|parameters|descriptions|
|---|---|
|--workdir|Specify the location of the Diamond folder. For example: --workdir "/path/to/Diamond" (Do not contain a slash at the end!)|
|--centroid|Deliver centroided data. For example: --centroid "/path/to/Diamond/data/centroid/\*.mzXML"|
|--profile|Deliver profile data. For example: --profile "/path/to/Diamond/data/profile/\*.mzXML"|
|--fasta|Deliver the database file. For example: --fasta "/path/to/Diamond/data/sgs_yeast_decoy.fasta"|
|--windows|Deliver the windows file. For example: --windows "/path/to/Diamond/data/win.tsv.32"|
|--windowsNumber|Deliver the number of the windows to select a suitable parameter file for DIA-Umpire. For example: --windowsNumber "32"|
|--irt|Deliver a transition file containing RT normalization coordinates. For example: --irt "/path/to/Diamond/data/irt.TraML"|
|--lib|Deliver a ready-made assay library. For example: --lib "/path/to/Diamond/data/library.TraML"|
|--skipLibGeneration|The parameter means the step of building an assay library will be skipped and Diamond's library-based mode will be choosed. No need to give a specific parameter.|
### Options_library_free arguments
|parameters|descriptions|
|---|---|
|--outdir|Specify a results folder. For example: --outdir "/path/to/Diamond/outputs" (Do not contain a slash at the end! Default: the folder named results under the workdir)|
|--diau_paraNumber|Specify the maximum number of parallel data processing of DIA-Umpire (Default: "4").|
|--mgf_mzML_paraNumber|Specify the maximum number of parallel data processing for file format conversion (Default: "4").|
|--mzML_part_paraNumber|Specify the maximum number of parallel data processing for dividing mzML files (Default: "4").|
|--comet_paraNumber|Specify the maximum number of parallel data processing of Comet searching (Default: "4").|
|--tandem_paraNumber|Specify the maximum number of parallel data processing of X!Tandem searching (Default: "20").|
|--merge_paraNumber|Specify the maximum number of parallel data processing for merging searching results (Default: "9").|
|--xinteract_paraNumber|Specify the maximum number of parallel data processing for xinteract (Default: "30").|
|--min_decoy_fraction|Specify the minimum fraction of decoy / target peptides and proteins for OpenSwathDecoyGenerator (Default: "0.8").|
|--openSWATH_paraNumber|Specify the maximum number of parallel data processing for openSWATH (Default: "4").|
|--min_rsq|Specify the minimum r-squared of RT peptides regression for OpenSwathWorkflow (Default: "0.95").|
|--pp_paraNumber|Specify the maximum number of parallel data processing for PyProphet (Default: "9").|
|--fdr|The threshold of FDR control (Default: "0.01").|
|--pp_score_statistics_mode|The parameter option of PyProphet (Default: "global"). You can modify it to "local" or "local-global".|
|--pp_score_lambda|The lambda value for storeys method (Default: "0.4").|  

**Note:** We process the MS data on a machine with a 64-core CPU and 256G memory. The greater the number of parallel data processing, the higher the memory and CPU resources consumed. If the memory is insufficient, you can appropriately reduce the number of parallel data processing.

### Options_library_based arguments
|parameters|descriptions|
|---|---|
|--outdir|Specify a results folder. For example: --outdir "/path/to/Diamond/outputs" (Do not contain a slash at the end! Default: the folder named results under the workdir)|
|--openSWATH_paraNumber|Specify the maximum number of parallel data processing for openSWATH (Default: "4").|
|--min_rsq|Specify the minimum r-squared of RT peptides regression for OpenSwathWorkflow (Default: "0.95").|
|--pp_paraNumber|Specify the maximum number of parallel data processing for PyProphet (Default: "9").|
|--fdr|The threshold of FDR control (Default: "0.01").|
|--pp_score_statistics_mode|The parameter option of PyProphet (Default: "global"). You can modify it to "local" or "local-global".|
|--pp_score_lambda|The lambda value for storeys method (Default: "0.4").| 
### Functions arguments
These parameters are built-in functions of Nextflow, they can generate some visual graphics, which or show the total time consumption of the pipeline, or show the time consumption, memory occupation, cpu usage of each process. Interested can add these parameters to observe relative information.
|parameters|descriptions|
|---|---|
|-with-timeline|It renders a timeline.html file that records the time, memory consumption of different processes.|
|-with-report|It generates a report.html file that records the single core CPU Usage, execution time, memory occupation and Disk read write information of different processes.|
|-with-trace|It creates an execution tracing file that contains some useful information about each process executed in your pipeline script, including: submission time, start time, completion time, cpu and memory used.|
|-with-dag|It outputs the pipeline execution DAG. It creates a file named dag.dot containing a textual representation of the pipeline execution graph in the DOT format.|
|-resume|It means only the processes that are actually changed will be re-executed. The execution of the processes that are not changed will be skipped and the cached result used instead. Also, the pipeline can be restarted by add the parameter when any disconnection of the network or server occurs.| 
