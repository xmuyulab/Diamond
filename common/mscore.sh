
i=$2

j=`echo ${i%scored.txt}pp.tsv|awk -F'/' '{print $NF;}' `

awk -F'\t' '{if (NR==1||$58<0.01)print $0;}' ${i} >$1/${j}

sed -i 's/transition_group_id_m_score/m_score/' $1/${j}





