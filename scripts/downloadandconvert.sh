#! /bin/bash

authority="ucar_test"
echo "will use authority=$authority"

libdir=https://raw.githubusercontent.com/Unidata/UDUNITS-2/master/lib
echo "downloading from $libdir"
for vocname in accepted base common derived prefixes; do
    echo "download $libdir/udunits2-$vocname.xml"
    curl "$libdir/udunits2-$vocname.xml" -o src/main/resources/udunits2-$vocname.xml
    echo "convert $libdir/udunits2-$vocname.xml"
    sbt "run --namespace http://mmisw.org/ont/$authority/udunits2-$vocname/ --xml src/main/resources/udunits2-$vocname.xml"
done
