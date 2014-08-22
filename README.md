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
$ XML="https://raw.githubusercontent.com/Unidata/UDUNITS-2/master/lib/udunits2-accepted.xml"
$ curl $XML -o src/main/resources/udunits2-accepted.xml
```
Script `scripts/download.sh` helps do all downloads with a single command.

Then run the `udunits2rdf` program to generate the RDF version of the XML files:

```shell
$ sbt run
```

This program reads in configuration parameters from `src/main/resources/application.conf`.

### Noteworthy changes

- 2014-08-22
  - for each vocab, include only relevant base definitions #9

- 2014-08-04
  - general code adjustments to simplify execution
  - include metadata

- 2014-07-30
  - capture base definitions separately
  - more complete conversion, but still needs adjustments

- 2014-07-13
  - first "new" version based on old "watchdog" code.
