#! /bin/bash 

#install pyprophet. It's strongly recommended to install pyprophet within a virtualenv
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

sh -c '/bin/echo "y" | conda create -n pyprophet python=2.7'

#激活虚拟环境，并没有真正意义上的在终端进入pyprophet，而是在脚本内部空间激活虚拟环境，因此同样可以在虚拟环境中安装packages
conda activate pyprophet 

apt-get install --allow-unauthenticated -y python-dev build-essential \
libssl-dev libffi-dev libxml2-dev libxslt1-dev zlib1g-dev 

/mnt/software/anaconda3/envs/pyprophet/bin/pip install numpy 
/mnt/software/anaconda3/envs/pyprophet/bin/pip install pyprophet==0.24.1 
/mnt/software/anaconda3/envs/pyprophet/bin/pip install pyprophet-cli

conda deactivate 


