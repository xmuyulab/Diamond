# Diamond: An Nextflow-based Multi-modal DIA Mass Spectrometry Data Processing Pipeline Encapsulated By Docker

## Flowchart of Diamond
![image](https://github.com/xmuyulab/Diamond/blob/master/images/fig01.png)
Automatically identificating targeted peptides from MS data acquired in DIA mode depends on a spectral library. Usually, we only have raw MS data and lack a spectral library. In this suitation, we can choose Diamond's blue route, as shown in the picture above, first construct a spectral library, and then identify the targeted peptides. Of course, if you have a set of mass spectrometry data with a ready-made spectral library on hand, you can take the green route of Diamond, skip the library building step, and directly analyze the mass spectrometry data.

## Diamond Application
Diamond is containerized by Docker into an image, so Docker must be installed on the host machine. The installation of Docker is described in the Docker documentation (https://docs.docker.com/engine). Make sure that Docker is up and running in the background. On your machine, please start a Terminal session. Execute the following steps within the console:

```shell
docker pull zeroli/diamond:1.0
```

This will take a few minutes to pull the Diamond Docker image from Docker Hub and cache it on your machine. You can check whether the image *zeroli/diamond:1.0* is succeccfully pulled by executing *docker images*. Then we start a container based on the Diamond image.

```shell
docker run -it --name diamond_test -v /path/to/Diamond/:/path/to/Diamond zeroli/diamond:1.0 bash
```

Docker starts a container named diamond_test and opens a Bash command line within the container for you to control individual components of Diamond. Besides, it is strongly recommended to add -v parameter for implementing data and scripts mounting: mount the local volume /path/to/Diamond (from your machine) to /path/to/Diamond (to your container) instead of directly store them in the container.

## Nextflow Scripts Execution
The data processing workflow of Diamond involves complicated command-lines and script tools. We utilize Nextflow to chain them together, which facilitate complex data manipulations. The Nextflow script is saved as a pipeline.nf file in the current folder. Nextflow has been added into the environment variables, and you can execute *nextflow --help* command to any path in your container to ensure that Nextflow can be correctly used. 

Assuming your are in the Diamond folder, which contains raw MS data, the common folder and pipeline.nf. Now you can start the pipeline to process MS data of yeast with the aim to build a spectral library by executing the following command.

```shell
nextflow run pipeline.nf --workdir "/path/to/Diamond" --centroid "/path/to/Diamond/data/centroid/\*.mzXML" --profile "/path/to/Diamond/data/profile/\*.mzXML" --fasta "/path/to/Diamond/data/sgs_yeast_decoy.fasta" --winodws "/path/to/Diamond/data/win.tsv.32" --windowsNumber "32" --outdir "/path/to/results_folder"
```

The --outdir parameter is optional. The folder it specifies is used to store the data processing results. The default is the folder named results in the workdir directory. You can also specify another folder as needed.

If you additionally have a ready-made spectral library and an irt file, execute the following command to skip the process of building a spectral library and directly start the targeted peptide identification. 

```shell
nextflow run pipeline.nf --skipLibGeneration --workdir "/path/to/Diamond" --profile "/path/to/Diamond/data/profile/\*.mzXML" --irt "/path/to/irt_file" --lib "/path/to/lib_file" --windows "/path/to/windows_file" --outdir "/path/to/results_folder"
```

The --outdir parameter here is the same as that mentioned above and is optional too.Execute the command *nextflow run pipeline.nf --help* to view the detailed information of parameter passing.
