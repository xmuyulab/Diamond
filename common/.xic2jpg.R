Args <- commandArgs()
path=Args[6]
out.wid=10
out.hei=4
out.cnt=TOTAL_RUN_COUNT
wanted_exp=c(1:out.cnt)

library(ggplot2)
library(showtext)
library(reshape2)

font.add("DejaVu Sans","/data/gpfs02/jhan/software/dejavu/DejaVuSans.ttf")
showtext.auto()  ## automatically use showtext for new devices)

theme(text=element_text(family="DejaVu Sans")) 

# 修改geom_text的字体
geom_text(family="DejaVu Sans")
 

 #file="C:/Users/dot/Desktop/draw_xic/sp_A2AKX3_SETX_MOUSE-(Acetyl)STC(Carbamidomethyl)C(Carbamidomethyl)WC(Carbamidomethyl)TPGGSSTIDVLKR-2"
draw_tr=function(file){
  data <- read.delim(paste(file,".txt",sep = ""), header=FALSE, stringsAsFactors=FALSE)
  lines = readLines(paste(file,".info.txt",sep = ""), n = 3)
  
  #deal info
  exp_num=as.numeric(strsplit(lines,"\t")[[1]])
  exp_size=length(exp_num)
  mz=as.numeric(strsplit(lines,"\t")[[2]])
  mz_size=length(mz)
  peak=as.numeric(strsplit(lines,"\t")[[3]])
  peak[20]=5
  #deal data
  data=data[,-dim(data)[2]]
  data_len1=dim(data)[1]
  data_len2=dim(data)[2]
  
  # get ms1 and ms2 data
  #ms1_rt=data[1,]
  #ms1_int=data[2,]
  ms2_rt=data[3,]
  ms2_int=data[c(4:data_len1),]
  #row.names(ms2_int)=mz
  
  # deal peak
  peak_info=NA
  exp_num_peak=NA
  for(i in c(1:exp_size)){
    if(peak[i*2]>0){
      peak_info=c(peak_info,(sum(exp_num[0:(i-1)])+c((peak[i*2-1]+1-1):(peak[i*2-1]+peak[i*2]+1))))
      exp_num_peak=c(exp_num_peak,peak[i*2]+2)    
    }else{
      exp_num_peak=c(exp_num_peak,0)
    }
  }
  peak_info=peak_info[-1]
  exp_num_peak=exp_num_peak[-1]
  
  # get peak info
 # ms1_rt_peak=data[1,peak_info]
 # ms1_int_peak=data[2,peak_info]
  ms2_rt_peak=data[3,peak_info]
  ms2_int_peak=data[c(4:data_len1),peak_info]
  #row.names(ms2_int_peak)=mz
  
  max_y=max(ms2_int_peak)*1.0
 # ms1_ms2_rate=max(ms1_int_peak)/max(ms2_int_peak)/0.8
  ms1_ms2_rate=1.0

  exp_name=data.frame(exp=wanted_exp,name=wanted_exp)
  
  rate=ms1_ms2_rate
  #ms1_int=ms1_int/rate
  exp=NA
  for(i in c(1:exp_size)){
    exp=c(exp,rep(i,exp_num[i]))
  }
  exp=exp[-1]
 # ms1=data.frame(exp=exp,rt=drop(as.matrix(ms1_rt[1,])),int=drop(as.matrix(ms1_int[1,])))
  
  ms2=data.frame(exp=exp,rt=drop(as.matrix(ms2_rt[1,])))
  ms2=cbind(ms2,t(ms2_int))
  long_ms2=melt(ms2,id.vars=c("exp","rt"))
  
  long_ms2=long_ms2[which(long_ms2$exp %in% wanted_exp),]
 # ms1=ms1[which(ms1$exp %in% wanted_exp),]
  long_ms2=merge(long_ms2,exp_name)
  long_ms2=data.frame(exp=long_ms2$name,rt=long_ms2$rt,variable=long_ms2$variable,value=long_ms2$value)
 # ms1=merge(ms1,exp_name)
 # ms1=data.frame(exp=ms1$name,rt=ms1$rt,int=ms1$int)
  
  ggplot()+

   geom_line(data=long_ms2, aes(rt, value, colour = variable)) +  
#geom_point(data=long_ms2, aes(rt, value, colour = variable),size=1,shape=20) +
   facet_grid(exp~.,margins=F,scales="free_y")+
    scale_y_continuous(limits=c(0, max_y))+
    ggtitle(sub(".+?-","",file,perl = T)) + xlab("Retention time (s)") + ylab("Intensity")+
    theme(text = element_text(size = 12, face = "plain",family="DejaVu Sans"),
          plot.title=element_text(size = 12, face = "plain",family="DejaVu Sans"),
          legend.text=element_text(size = 8, face = "plain",family="DejaVu Sans"),
          axis.text=element_text(size = 10, face = "plain",family="DejaVu Sans"),axis.text.x=element_text(angle = 0),
          axis.title.x=element_text(vjust=-0.2),axis.title.y=element_text(vjust=1))+
	scale_colour_discrete(name="Product ions m/z",labels=as.character(mz))
}

files=list.files(path,"*.info.txt")
files=sub(".info.txt","",files)
files
library(parallel)  
cl <- makeCluster(detectCores(),type="FORK")
parLapply(cl,files,function(f){
	draw_tr(paste(path,f,sep = ""))
	ggsave(paste(path,f,".png",sep = ""),width=out.wid,height=out.cnt*out.hei,units="in",dpi=300,limitsize=FALSE)
	#f
}) 
stopCluster(cl)
