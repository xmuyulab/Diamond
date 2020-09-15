# Diamond: A Nextflow-Based Multi-Modal DIA Mass Spectrometry Data Processing Pipeline

## Flowchart of Diamond
![image](https://github.com/xmuyulab/Diamond/blob/master/images/fig01.png)
In the suitation when an assay libraries is available, we choose Diamond's library-based mode, as the blue route shown in the picture above, an assay library will be generated first and in the suitation when an assay library is not available, we choose the Diamond's library-free mode, as the green route shown in the picture above, the library building step will be skipped.

## Diamond Application
Diamond is containerized by Docker into an image, so Docker must be installed on the host machine. The installation of Docker is described in the Docker documentation (https://docs.docker.com/engine). Make sure that Docker is up and running in the background. On your machine, please start a Terminal session. Execute the following steps within the console:

```shell
docker pull zeroli/diamond:1.0
```

This will take a few minutes to pull the Diamond image from [Docker Hub](https://hub.docker.com/repository/docker/zeroli/diamond) and cache it on your machine. You can check whether the image `zeroli/diamond:1.0` is successfully pulled by executing `docker images`, and if successfully, it will appear in the images list. A container based on the Diamond image can be created by running the following command.

```shell
docker run -it --name diamond_test -v /path/to/Diamond/:/mnt/Diamond zeroli/diamond:1.0 bash
```

Docker starts a container named diamond_test and opens a Bash command line within the container for you to control individual components of Diamond. Besides, it is strongly recommended to add -v parameter for implementing data and scripts mounting: mount the local volume /path/to/Diamond (from your machine) to /mnt/Diamond (to your container) instead of directly copy them into the container. After completion, your will enter the container, and you will find that all software tools are installed in the /mnt/software directory. Type in exit and press Enter, or hit Ctrl+D to exit the container.

## Nextflow Scripts Execution
Nextflow has been added into the environment variables, and you can execute `nextflow --help` command to any path in the container created above to ensure Nextflow can be correctly used. The Nextflow script is saved as a pipeline.nf file in the Diamond folder.

The SWATH-MS Gold Standard (SGS) data sets are available from the PeptideAtlas raw data repository with accession number [PASS00289](https://db.systemsbiology.net/sbeams/cgi/PeptideAtlas/PASS_View?identifier=PASS00289).  We select the SGS data sets of yeast that you are interested in and store them in a specific folder, for example, the /Diamond/data. Assuming your are in the Diamond folder, containing yeast MS data, the common folder and pipeline.nf. Now you can start to process MS data of yeast with the aim to build a spectral library:

```shell
nextflow run pipeline.nf --workdir "/path/to/Diamond" --centroid "/path/to/Diamond/data/yeast/centroid/*.mzXML" --profile "/path/to/Diamond/data/yeast/profile/*.mzXML" --fasta "/path/to/Diamond/data/yeast/sgs_yeast_decoy.fasta" --winodws "/path/to/Diamond/data/yeast/win.tsv.32" --windowsNumber "32" --outdir "/path/to/results_folder"
```

Maybe you need to specify the absolute path for the pipeline.nf file, just like /path/to/Diamond/pipeline.nf. The --outdir parameter is optional. The directory it specifies is used to store the data processing results. The default is the folder named results in the workdir directory. Please execute `nextflow run pipeline.nf --help` to view the detailed information of parameter passing.

We also provide a set of raw MS data in the data folder with a spectral library and an irt file, which can be analyzed by executing the following command in your terminal. Assuming you are in the Diamond folder, containing MS data, the common folder and pipeline.nf. Now you can directly start the targeted analysis of MS data with an input spectral library:  

```shell
nextflow run pipeline.nf --skipLibGeneration --workdir "/path/to/Diamond" --profile "/path/to/Diamond/data/profile/*.mzXML" --lib "/path/to/lib_file" --irt "/path/to/irt_file" --windows "/path/to/windows_file" --outdir "/path/to/results_folder"
```

Maybe you need to specify the absolute path for the pipeline.nf file, just like /path/to/Diamond/pipeline.nf. The --skipLibGeneration parameter means the process of building a spectral library will be skipped. The --outdir parameter here is the same as that mentioned above and is optional too. For elaborate information of parameter passing, execute the command `nextflow run pipeline.nf --help`.

## Help Message
Two different execution-commands for the two different modes of Diamond:
### Library-based mode:
```
nextflow run pipeline.nf --workdir [] --centroid [] --profile [] --fasta [] --windows [] --windowsNumber [] <Options_01> <Functions>
```
### Library-free mode: 
```
nextflow run pipeline.nf --skipLibGeneration --workdir [] --profile [] --lib [] --irt [] --windows [] <Options_02> <Functions>
```

### Parameters descriptions
#### Mandatory arguments
|parameters|descriptions|
|---|---|
|--workdir|Specify the location of the Diamond folder. For example: --workdir "/PATH/TO/Diamond" (Do not contain a slash at the end!)|
|--centroid|Deliver centroided data. For example: --centriod "/PATH/TO/Diamond/data/centroid/\*.mzXML"|
|--profile|Deliver profile data. For example: --profile "/PATH/TO/Diamond/data/profile/\*.mzXML"|
|--windows|Deliver the windows file. For example: --windows "/PATH/TO/win.tsv"|
|--windowsNumber|Deliver the number of the windows. For example: --windowsNumber "32"|
|--lib|Deliver a ready-made assay library. For example: --lib "/PATH/TO/lib.TraML"|
|--irt|Deliver a transition file containing RT normalization coordinates. For example: --irt "/PATH/TO/irt.TraML"|
|<div style="width: 300pt">--skipLibGeneration</div>|The parameter means the library-free mode of Diamond will be implemented.|
#### Options_01 arguments
|parameters|descriptions|
|-|-|
|--outdir|Specify a results folder. For example: --outdir "/PATH/TO/Diamond/outputs" (Do not contain a slash at the end! Default: the folder named results under the workdir)|
|--diau_paraNumber|Specify the number of data parallel processing of DIA-Umpire.|
|--mgf_mzML_paraNumber|Specify the number of data parallel processing for file format conversion.|
|--mzML_part_paraNumber|Specify the number of parallel processing for dividing mzML files.|
|--comet_paraNumber|Specify the number of parallel processing of Comet searching.|
|--tandem_paraNumber|Specify the number of parallel processing of X!Tandem searching.|
|--merge_paraNumber|Specify the number of parallel processing for merging searching results.|
|--xinteract_paraNumber|Specify the number of parallel processing for xinteract.|
|--openSWATH|Specify the number of parallel processing for openSWATH.|
|--pp_paraNumber|Specify the number of parallel processing for PyProphet.|
|--fdr|The threshold of FDR control (Default: "0.01").|
|--statistics_mode|The parameter option of PyProphet (Default: "global"). You can modify it to "local" or "local-global".|
#### Options_02 arguments
|parameters|descriptions|
|---|---|
|<div style="width: 5cm">--openSWATH</div>|Specify the number of parallel processing for openSWATH.|
|<div style="width: 5cm">--pp_paraNumber</div>|Specify the number of parallel processing for PyProphet.|
|<div style="width: 5cm">--fdr</div>|The threshold of FDR control (Default: "0.01").|
|<div style="width: 5cm">--statistics_mode</div>|The parameter option of PyProphet (Default: "global"). You can modify it to "local" or "local-global".|
#### Functions arguments
These parameters are built-in functions of Nextflow, they can generate some visual graphics, which or show the total time consumption of the pipeline, or show the time consumption, memory occupation, cpu usage of each process. Interested can add these parameters to observe relative information.
|parameters|descriptions|
|-|-|
|-with-timeline|It renders a timeline.html file that records the time, memory consumption of different processes.|
|-with-report|It generates a report.html file that records the single core CPU Usage, execution time, memory occupation and Disk read write information of different processes.|
|-with-trace|It creates an execution tracing file that contains some useful information about each process executed in your pipeline script, including: submission time, start time, completion time, cpu and memory used.|
|-with-dag|It outputs the pipeline execution DAG. It creates a file named dag.dot containing a textual representation of the pipeline execution graph in the DOT format.|
|-resume|It means only the processes that are actually changed will be re-executed. The execution of the processes that are not changed will be skipped and the cached result used instead. Also, the pipeline can be restarted by add the parameter when any disconnection of the network or server occurs.| 
