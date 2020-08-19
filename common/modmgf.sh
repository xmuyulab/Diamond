
awk -F'=' 'BEGIN{T=0;N=0;}{if (T==1&&$1!="SCANS"){++N;printf("SCANS=%d\n",N);}T=0;if ($0=="BEGIN IONS"){T=1;}if ($1=="PEPMASS"){t=index($2," ");if (t!=0){print $0;}else{print $0,1000;}}else{print $0;}}' $1 > $2

