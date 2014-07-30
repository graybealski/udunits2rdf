#! /bin/bash

destdir=src/main/resources
authority="mmitest"
echo "will use authority=$authority"

for vocname in accepted base common derived prefixes; do
    xml=$destdir/udunits2-$vocname.xml
    echo "convert $libdir/udunits2-$vocname.xml"
    basedefs="http://mmisw.org/ont/mmitest/udunits2/"
    namespace="http://mmisw.org/ont/$authority/udunits2-$vocname/"

    # as a workaround for a rendering limitation in the ORR, make
    # basedefs's namespace equal to that of the vocabulary:
    basedefs=$namespace

    sbt "run --basedefs $basedefs --namespace $namespace --xml $xml"
done
