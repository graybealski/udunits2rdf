#! /bin/bash

authority="mmitest"
libdir=https://raw.githubusercontent.com/Unidata/UDUNITS-2/master/lib
destdir=src/main/resources
echo "downloading from $libdir to $destdir"

for vocname in accepted base common derived prefixes; do
    echo "download $libdir/udunits2-$vocname.xml"
    curl "$libdir/udunits2-$vocname.xml" -o "$destdir/udunits2-$vocname.xml"
done
