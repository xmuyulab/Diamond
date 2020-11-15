# Flowchart of Diamond
![image](https://github.com/xmuyulab/Diamond/blob/master/images/fig01.png)
In the suitation when an assay library is not available, we choose Diamond's library-free mode, as the blue route shown in the picture above, an assay library will be generated first and in the suitation when an assay library is available, we choose the Diamond's library-based mode, as the green route shown in the picture above, the library building step will be skipped.

# An Instruction on the Analysis of Example Datasets
## Data preparation
Firstly, please execute the following command in your terminal to clone the Diamond repository from [my GitHub](https://github.com/xmuyulab/Diamond) to your own machine. 
```shell
git clone https://github.com/xmuyulab/Diamond.git
```
Then, execute the following command step by step to create a series of folders to store the example MS data.
```shell
cd /path/to/Diamond
mkdir data data/centroid data/profile
```
Finally, download the example MS data. Provided here are the three mzXML files in profile mode in SWATH-MS Gold Standard (SGS) data of yeast, which are available from the PeptideAtlas raw data repository with accession number [PASS00289](http://www.peptideatlas.org/PASS/PASS00289) and the three mzXML files in centroid mode, which can be obtained by preprocessing the profile data with [ProteoWizard](http://proteowizard.sourceforge.net/download.html).

(1) three profile data files: please visit [PASS00289](http://www.peptideatlas.org/PASS/PASS00289), click on the link "ftp://PASS00289:XY4524h@ftp.peptideatlas.org/" at the bottom of the page, select the three files `napedro_L120228_00{1,2,3}_SW.mzXML.gz` under the /SGS/mzxml/ folder, download them one by one and store them in the profile folder. Note that the profile files are in a compressed format, so execute the following command to decompress them.
```shell
cd /path/to/Diamond/data/profile
gunzip ./napedro_L120228_00{1,2,3}_SW.mzXML.gz
```
(2) three centroid data files: [cMS01](), [cMS02](), [cMS03](), download them one by one and store them in the centroid folder. Note that the centroid files are in a compressed format, so execute the following command to decompress them.
```shell
cd /path/to/Diamond/data/centroid
gunzip ./centroided_napedro_L120228_00{1,2,3}_SW.mzXML.gz
```

## Diamond acquisition
Diamond is containerized by Docker into an image, the installation tutorial of Docker is described in the [Docker documentation](https://docs.docker.com/engine). On your machine, please start a Terminal session. Execute the following command within the console:
```shell
docker pull zeroli/diamond:1.0
```
This will take a few minutes to pull the Diamond image from [Docker Hub](https://hub.docker.com/r/zeroli/diamond/) to your machine. You can check whether the image `zeroli/diamond:1.0` is successfully pulled by executing `docker images`, and if successfully, it will appear in the images list.  

## Container creation and startup
Create a container (named diamond_test) based on the image `zeroli/diamond:1.0` and simultaneously mount the local folder `/path/to/Diamond` to the folder `/mnt/Diamond` in the container by running the following command in your terminal:
```shell
docker run --name diamond_test -v /path/to/Diamond/:/mnt/Diamond zeroli/diamond:1.0 bash
```
Next, start and enter the container, and then switch to the folder /mnt/Diamond by executing the following commands in order:
```shell
docker start diamond_test
docker exec -it diamond_test bash 
cd /mnt/Diamond
```
Note: type in `exit` and press `Enter`, or hit `Ctrl+D` to exit the container.
## Data analysis
The Nextflow script is saved as a `pipeline.nf` file in the Diamond folder. Diamond's two modes: library-free and library-based execution commands are as follows.
### Library-free mode
Execute the following command in your terminal to start the analysis of MS data with the aim to build an assay library:
```shell
nextflow run /mnt/Diamond/pipeline.nf --workdir "/mnt/Diamond" --centroid "/mnt/Diamond/data/centroid/*.mzXML" --profile "/mnt/Diamond/data/profile/*.mzXML" --fasta "/mnt/Diamond/data/sgs_yeast_decoy.fasta" --winodws "/mnt/Diamond/data/win.tsv.32" --windowsNumber "32"
```
The MS data processing results will be stored in the folder named `results` under `/mnt/Diamond` by default. Please refer to the Help Message section or execute `nextflow run /mnt/Diamond/pipeline.nf --help` in the container to view the detailed information of parameter passing.
### Library-based mode
Execute the following command in your terminal to start the analysis of MS data by providing an assay library:
```shell
nextflow run /mnt/Diamond/pipeline.nf --skipLibGeneration --workdir "/mnt/Diamond" --profile "/mnt/Diamond/data/profile/*.mzXML" --lib "/mnt/Diamond/data/lib.os.TraML" --irt "/mnt/Diamond/data/irt.TraML" --windows "/mnt/Diamond/data/win.tsv.32"
```
The `--skipLibGeneration` parameter means the process of building an assay library will be skipped. The data processing results will be also stored in the folder named `results` under `/mnt/Diamond` by default. Please refer to the Help Message section or execute `nextflow run /mnt/Diamond/pipeline.nf --help` in the container to view the detailed information of parameter passing.

## Help Message
Two different execution-commands for the two different modes of Diamond. This help message can also be obtained by executing the following command in the container：
```shell
nextflow run /mnt/Diamond/pipeline.nf --help
```
### Library-free mode:
```
nextflow run /mnt/Diamond/pipeline.nf --workdir "" --centroid "" --profile "" --fasta "" --windows "" --windowsNumber "" <Options_library_free> <Functions>
```
### Library-based mode: 
```
nextflow run /mnt/Diamond/pipeline.nf --skipLibGeneration --workdir "" --profile "" --lib "" --irt "" --windows "" <Options_library_based> <Functions>
```

### Parameters descriptions
#### Mandatory arguments
|parameters|descriptions|
|---|---|
|--workdir|Specify the location of the Diamond folder. For example: --workdir "/path/to/Diamond" (Do not contain a slash at the end!)|
|--centroid|Deliver centroided data. For example: --centriod "/path/to/Diamond/data/centroid/\*.mzXML"|
|--profile|Deliver profile data. For example: --profile "/path/to/Diamond/data/profile/\*.mzXML"|
|--fasta|Deliver the database file. For example: --fasta "/path/to/Diamond/common/db.fasta"|
|--windows|Deliver the windows file. For example: --windows "/path/to/Diamond/common/win.tsv.32"|
|--windowsNumber|Deliver the number of the windows. For example: --windowsNumber "32"|
|--lib|Deliver a ready-made assay library. For example: --lib "/path/to/lib.TraML"|
|--irt|Deliver a transition file containing RT normalization coordinates. For example: --irt "/path/to/irt.TraML"|
|--skipLibGeneration    | The parameter means the library-free mode of Diamond will be implemented. |
#### Options_library_free arguments
|parameters|descriptions|
|---|---|
|--outdir|Specify a results folder. For example: --outdir "/path/to/Diamond/outputs" (Do not contain a slash at the end! Default: the folder named results under the workdir)|
|--diau_paraNumber|Specify the number of data parallel processing of DIA-Umpire (Default: "4").|
|--mgf_mzML_paraNumber|Specify the number of data parallel processing for file format conversion (Default: "4").|
|--mzML_part_paraNumber|Specify the number of parallel processing for dividing mzML files (Default: "4").|
|--comet_paraNumber|Specify the number of parallel processing of Comet searching (Default: "4").|
|--tandem_paraNumber|Specify the number of parallel processing of X!Tandem searching (Default: "20").|
|--merge_paraNumber|Specify the number of parallel processing for merging searching results (Default: "9").|
|--xinteract_paraNumber|Specify the number of parallel processing for xinteract (Default: "30").|
|--openSWATH_paraNumber|Specify the number of parallel processing for openSWATH (Default: "4").|
|--pp_paraNumber|Specify the number of parallel processing for PyProphet (Default: "9").|
|--fdr|The threshold of FDR control (Default: "0.01").|
|--pp_score_statistics_mode|The parameter option of PyProphet (Default: "global"). You can modify it to "local" or "local-global".|
|--pp_score_lambda|The lambda value for storeys method (Default: "0.4").|  
#### Options_library_based arguments
|parameters|descriptions|
|---|---|
|--openSWATH_paraNumber|Specify the number of parallel processing for openSWATH (Default: "4").|
|--pp_paraNumber|Specify the number of parallel processing for PyProphet (Default: "9").|
|--fdr|The threshold of FDR control (Default: "0.01").|
|--pp_score_statistics_mode|The parameter option of PyProphet (Default: "global"). You can modify it to "local" or "local-global".|
|--pp_score_lambda|The lambda value for storeys method (Default: "0.4").| 
#### Functions arguments
These parameters are built-in functions of Nextflow, they can generate some visual graphics, which or show the total time consumption of the pipeline, or show the time consumption, memory occupation, cpu usage of each process. Interested can add these parameters to observe relative information.
|parameters|descriptions|
|---|---|
|-with-timeline|It renders a timeline.html file that records the time, memory consumption of different processes.|
|-with-report|It generates a report.html file that records the single core CPU Usage, execution time, memory occupation and Disk read write information of different processes.|
|-with-trace|It creates an execution tracing file that contains some useful information about each process executed in your pipeline script, including: submission time, start time, completion time, cpu and memory used.|
|-with-dag|It outputs the pipeline execution DAG. It creates a file named dag.dot containing a textual representation of the pipeline execution graph in the DOT format.|
|-resume|It means only the processes that are actually changed will be re-executed. The execution of the processes that are not changed will be skipped and the cached result used instead. Also, the pipeline can be restarted by add the parameter when any disconnection of the network or server occurs.| 
