# Diamond: A Nextflow-Based Multi-Modal DIA Mass Spectrometry Data Processing Pipeline

## Flowchart of Diamond
![image](https://github.com/xmuyulab/Diamond/blob/master/images/fig01.png)
Automatically identificating targeted peptides from MS data acquired in DIA mode depends on a spectral library. Usually, we only have raw MS data and lack a spectral library. In this suitation, we can choose Diamond's blue route, as shown in the picture above, first construct a spectral library, and then identify the targeted peptides. Of course, if you have a set of mass spectrometry data with a ready-made spectral library on hand, you can take the green route of Diamond, skip the library building step, and directly analyze the mass spectrometry data.

## Diamond Application
Diamond is containerized by Docker into an image, so Docker must be installed on the host machine. The installation of Docker is described in the Docker documentation (https://docs.docker.com/engine). Make sure that Docker is up and running in the background. On your machine, please start a Terminal session. Execute the following steps within the console:

```shell
docker pull zeroli/diamond:1.0
```

This will take a few minutes to pull the Diamond image from [my Docker Hub](https://hub.docker.com/repository/docker/zeroli/diamond) and cache it on your machine. You can check whether the image `zeroli/diamond:1.0` is successfully pulled by executing `docker images`, and if successfully, it will appear in the images list. Then we create a container based on the Diamond image.

```shell
docker run -it --name diamond_test -v /path/to/Diamond/:/mnt/Diamond zeroli/diamond:1.0 bash
```

Docker starts a container named diamond_test and opens a Bash command line within the container for you to control individual components of Diamond. Besides, it is strongly recommended to add -v parameter for implementing data and scripts mounting: mount the local volume /path/to/Diamond (from your machine) to /mnt/Diamond (to your container) instead of directly copy them into the container. After completion, your will enter the container, and you will find that all software tools are installed in the /mnt/software directory. Type in exit and press Enter, or hit Ctrl+D to exit the container.

## Nextflow Scripts Execution
The data processing workflow of Diamond involves complicated command-lines and script tools. We utilize Nextflow to chain them together, which facilitate complex data manipulations. The Nextflow script is saved as a pipeline.nf file in the Diamond folder. Nextflow has been added into the environment variables, and you can execute `nextflow --help` command to any path in the container created above to ensure Nextflow can be correctly used. 

We provide a set of yeast raw MS data in the data folder without a spectral library , which can be analyzed by executing the following command in your terminal. Assuming your are in the Diamond folder, containing yeast MS data, the common folder and pipeline.nf. Now you can start to process MS data of yeast with the aim to build a spectral library:

```shell
nextflow run pipeline.nf --workdir "/path/to/Diamond" --centroid "/path/to/Diamond/data/yeast/centroid/*.mzXML" --profile "/path/to/Diamond/data/yeast/profile/*.mzXML" --fasta "/path/to/Diamond/data/yeast/sgs_yeast_decoy.fasta" --winodws "/path/to/Diamond/data/yeast/win.tsv.32" --windowsNumber "32" --outdir "/path/to/results_folder"
```

Maybe you need to specify the absolute path for the pipeline.nf file, just like /path/to/Diamond/pipeline.nf. The --outdir parameter is optional. The directory it specifies is used to store the data processing results. The default is the folder named results in the workdir directory. Please execute `nextflow run pipeline.nf --help` to view the detailed information of parameter passing.

We also provide a set of raw MS data in the data folder with a spectral library and an irt file, which can be analyzed by executing the following command in your terminal. Assuming you are in the Diamond folder, containing MS data, the common folder and pipeline.nf. Now you can directly start the targeted analysis of MS data with an input spectral library:  

```shell
nextflow run pipeline.nf --skipLibGeneration --workdir "/path/to/Diamond" --profile "/path/to/Diamond/data/profile/*.mzXML" --lib "/path/to/lib_file" --irt "/path/to/irt_file" --windows "/path/to/windows_file" --outdir "/path/to/results_folder"
```

Maybe you need to specify the absolute path for the pipeline.nf file, just like /path/to/Diamond/pipeline.nf. The --skipLibGeneration parameter means the process of building a spectral library will be skipped. The --outdir parameter here is the same as that mentioned above and is optional too. For elaborate information of parameter passing, execute the command `nextflow run pipeline.nf --help`.

# Help Message
## Library-free mode: 

```
nextflow run pipeline.nf --skipLibGeneration --workdir [] --profile [] --lib [] --irt [] --windows [] <options> <functions>
```
## Library-based mode:

```
nextflow run pipeline.nf --workdir [] --centroid [] --profile [] --fasta [] --windows [] --windowsNumber [] <options> <functions>
```
## Parameters descriptions
|parameter|descriptions|
|-|-|
|--skipLibGeneration|The parameter means the library-free mode of Diamond will be implemented.|
|--workdir|Specify the location of the Diamond folder.|
|--
