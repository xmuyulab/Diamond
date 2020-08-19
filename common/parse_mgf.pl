#!/usr/bin/perl -w
use IO::File;
open $flist,  '<', 'gd.params';

#read list
$name = <$flist>;
chomp $name;
$windows = <$flist>;
chomp $windows;
$path = <$flist>;
<$flist>;
chomp $path;
$path .= '/';
$exp_size = 0;

@out;

while (<$flist>) {
    if (/([^\/]+)_ms1scan.mzXML/) {
	
		$out[$exp_size]= '../../gd-mgf/'.$1.'_gd.mgf';

        $exp_size++;
    }
}

# read windows
open $fw, '<', $windows;
$windows_num = 0;
while (<$fw>) {
    chomp;
    if (/\d/) {
        $windows_num++;
    }
}

@file = <$path*.mgf>;
@fo = map { IO::File->new( $_, 'w' ) } @out;
for $i ( 0 .. ( $exp_size - 1 ) ) {

    #open $fc, '>', $name . '.spec.' . $i . '.mgf';
    $fo[$i]->print( '
' );

    #    push @fo, $fc;
}

$cur_scan = 0;

for $file (@file) {
    $content = "";
    open $fi, '<', $file;
    while ( $l = <$fi> ) {
        if ( $l =~ /(^TITLE=.+\.)(\d+?)\.(\d+?)(\.\d+$)/ ) {
            $cur_scan++;
            $origin_scan      = $2;
            $l                = $1 . $cur_scan . '.' . $cur_scan . $4 . "\n";
        }
        if ( $l =~ /SCANS=(.+$)/ ) {
            $scan  = $1;
            $decoy = $scan % 2;
            $scan  = ( $scan - $decoy ) / 2;
            $win   = $scan % $windows_num;
            $scan  = ( $scan - $win ) / $windows_num;
            $exp   = $scan % $exp_size;

            $l = 'SCANS=' . $cur_scan . "\n";
        }

        if ( $l =~ /BEGIN\ IONS/ ) {
            $content = $l;
        }
        else {
            $content .= $l;
        }

        if ( $l =~ /END\ IONS/ ) {
            $fc = $fo[$exp];
            $fo[$exp]->print( $content . "\n" );
        }
    }
}

