#!/usr/bin/env python2
import sys, csv, getopt

if len(sys.argv) < 6:
    print(
    """Usage: FeatureXMLGenerator.py input_csv_filename output_featureXML_filename m/z_column_name retention_time_column_name charge_column_name [-t] [-m]
    -t the csv file is splited by tab. If the flag is not set, means that the csv file is splited by comma.
    -m the retention time is in minute. If the flag is not set, means that the retention time is in second.
    """)
    exit()

is_min=False
is_tab=False

filename = sys.argv[1]
outfilename = sys.argv[2]
item_mz = sys.argv[3]
item_rt = sys.argv[4]
item_charge = sys.argv[5]

opts=sys.argv[6:]
for op in opts:
     if op == "-t":
         is_tab=True
     elif op == "-m":
         is_min=True

try:
    csvfile = open(filename, 'rb')
    if is_tab:
        csvreader = csv.reader(csvfile, delimiter='\t')
    else:
        csvreader = csv.reader(csvfile, delimiter=',')

    curline = csvreader.next()
except:
    print ("File not found or file is empty.")
    exit()

num_mz = -1
num_rt = -1
num_charge = -1
all_mz = []
all_rt = []
all_charge = []

for i in range(0, len(curline)):
    if curline[i] == item_mz:
        num_mz = i
    if curline[i] == item_rt:
        num_rt = i
    if curline[i] == item_charge:
        num_charge = i

if num_mz < 0 or num_rt < 0 or num_charge < 0:
    print ("A column name is missing!")
    exit()

while 1:
    try:
        curline = csvreader.next()
        try:
            mz = curline[num_mz]
            rt = curline[num_rt]
            if is_min:
                rt=float(rt)*60.0
            charge = curline[num_charge]
            all_mz.append(mz)
            all_rt.append(rt)
            all_charge.append(charge)
        except:
            pass
    except:
        break

if len(all_mz) != len(all_rt) or len(all_mz) != len(all_charge):
    print("A row miss m/z or retention time or charge.")
    exit()

head = """<?xml version="1.0" encoding="ISO-8859-1"?>
<featureMap version="1.6" xsi:noNamespaceSchemaLocation="http://open-ms.sourceforge.net/schemas/FeatureXML_1_6.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <featureList count=\"""" + str(len(all_mz)) + '">\n'
cur_num = 1

f = open(outfilename, 'w')
f.write(head)
while len(all_mz) > 0:
    f.write('                <feature id="f_' + str(cur_num) + '">\n')
    f.write('                        <position dim="0">' + str(all_rt.pop()) + '</position>\n')
    f.write('                        <position dim="1">' + str(all_mz.pop()) + '</position>\n')
    f.write("""                        <intensity>0</intensity>
                        <quality dim="0">0</quality>
                        <quality dim="1">0</quality>
                        <overallquality>0</overallquality>\n""")
    f.write('                        <charge>' + str(all_charge.pop()) + '</charge>\n')
    f.write('                </feature>\n')
    cur_num += 1
f.write("""        </featureList>
</featureMap>""")
f.close()
