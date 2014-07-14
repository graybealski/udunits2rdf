## MMI udunits2rdf tool ##

Conversion of [UDUnits](http://www.unidata.ucar.edu/software/udunits/) XML files to RDF.


### Running the conversion ###

You will need the [sbt tool](http://www.scala-sbt.org/download.html).

Starting from https://github.com/Unidata/UDUNITS-2/tree/master/lib, follow the links
until finding the one for the raw version of the XML file to be converted, and download that file somewhere
on your computer. Then run the `udunits2rdf` program to generate the RDF version.

A complete session on the command line:

```shell
$ curl "https://raw.githubusercontent.com/Unidata/UDUNITS-2/master/lib/udunits2-accepted.xml" -o src/main/resources/udunits2-accepted.xml
$ sbt
> run --namespace http://mmisw.org/ont/ucar/udunits2-accepted/ --xml src/main/resources/udunits2-accepted.xml
[info] Running org.mmisw.udunits2rdf.udunits2rdf --namespace http://mmisw.org/ont/ucar/udunits2-accepted/ --xml src/main/resources/udunits2-accepted.xml
udunits2rdf conversion
date:   Sun Jul 13 20:00:52 PDT 2014
input:  src/main/resources/udunits2-accepted.xml
output: src/main/resources/udunits2-accepted.rdf

conversion stats:
  numUnitsInInput    = 23
  numUnitsInOutput   = 41
  numUnitsWithNoNameOrAlias = 0
```

Script `scripts/downloadandconvert.sh` helps do all downloads and conversions with a single command:
```
$ scripts/downloadandconvert.sh
```
