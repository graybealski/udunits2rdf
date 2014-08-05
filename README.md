## MMI udunits2rdf tool ##

Conversion of [UDUnits](http://www.unidata.ucar.edu/software/udunits/) XML files to RDF.


### Running the conversion ###

You will need the [sbt tool](http://www.scala-sbt.org/download.html).

The original XML files are included in this repository. If there is a new version of any
of these files:
- go to https://github.com/Unidata/UDUNITS-2/tree/master/lib
- follow the links until finding the raw version of the updated XML file
- download the file to `src/main/resources/`.

For example:
```shell
$ curl "https://raw.githubusercontent.com/Unidata/UDUNITS-2/master/lib/udunits2-accepted.xml" -o src/main/resources/udunits2-accepted.xml
```
Script `scripts/download.sh` helps do all downloads with a single command.

Then run the `udunits2rdf` program to generate the RDF version of the XML files:

```shell
$ sbt run
```

This program reads in configuration parameters from `src/main/resources/application.conf`.
