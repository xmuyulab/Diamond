#! /bin/bash 

#install packages for spectrast_selfrt.py[python2.7] 
#scipy matplotlib numpy msproteomicstools 

#command: conda create -n your_env_name python=2.7/3.6 to create a virtualenv

# >>> conda initialize >>>
# !! Contents within this block are managed by 'conda init' !!
__conda_setup="$('/mnt/software/anaconda3/bin/conda' 'shell.bash' 'hook' 2> /dev/null)"
if [ $? -eq 0 ]; then
    eval "$__conda_setup"
else
    if [ -f "/mnt/software/anaconda3/etc/profile.d/conda.sh" ]; then
        . "/mnt/software/anaconda3/etc/profile.d/conda.sh"
    else
        export PATH="/mnt/software/anaconda3/bin:$PATH"
    fi
fi
unset __conda_setup
# <<< conda initialize <<<

#创建虚拟环境，版本为Python2.7
sh -c '/bin/echo "y" | conda create -n spectrast_selfrt python=2.7'

#激活虚拟环境，并没有真正意义上的在终端进入spectrast_selfrt，而是在脚本内部空间激活虚拟环境，因此同样可以在虚拟环境中安装packages
conda activate spectrast_selfrt 

#命令全部分开，避免pip install 过程中出现错误
/mnt/software/anaconda3/envs/spectrast_selfrt/bin/pip install numpy 
/mnt/software/anaconda3/envs/spectrast_selfrt/bin/pip install scipy 
/mnt/software/anaconda3/envs/spectrast_selfrt/bin/pip install Cython
/mnt/software/anaconda3/envs/spectrast_selfrt/bin/pip install matplotlib 
/mnt/software/anaconda3/envs/spectrast_selfrt/bin/pip install pymzml==0.7.8
#Biopython 1.76 is the final release that supports python2.7 or python3.5
/mnt/software/anaconda3/envs/spectrast_selfrt/bin/pip install Biopython==1.76
/mnt/software/anaconda3/envs/spectrast_selfrt/bin/pip install msproteomicstools 

conda deactivate
