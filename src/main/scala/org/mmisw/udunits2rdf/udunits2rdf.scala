package org.mmisw.udunits2rdf

import java.io.PrintWriter
import scala.collection.mutable


/**
 * main program.
 */
object udunits2rdf extends App {

  /**
   * helper to process arguments and run program.
   * @param opts   map of options values
   * @param block  block to execute
   * @return
   */
  def withOptions(opts: mutable.Map[String, String])(block : => Unit) {
    val defaults = {
      val sep = "\n    "
      sep + (for ((k,v) <- opts.toMap) yield s"$k = $v").mkString(sep)
    }
    val usage =
      s"""
        | USAGE:
        |   udunits2rdf --namespace ns --xml xml [--rdf rdf]
        | Example:
        |   udunits2rdf --namespace http://mmisw.org/ont/mmitest/udunits2-accepted/ --xml src/main/resources/udunits2-accepted.xml
        |   generates src/main/resources/udunits2-accepted.rdf
        |
        | Defaults: $defaults
        |
      """.stripMargin

    next(args.toList)

    def next(list: List[String]) {
      list match {
        case "--namespace" :: namespace :: args => opts("namespace") = namespace; next(args)
        case "--xml" :: xml :: args => opts("xml") = xml; next(args)
        case "--rdf" :: rdf :: args => opts("rdf") = rdf; next(args)
        case Nil => if (opts.contains("namespace") && opts.contains("xml")) block else println(usage)
        case _ => println(usage)
      }
    }
  }

  val opts = mutable.Map[String, String]()

  withOptions(opts) {
    val xmlFilename = opts("xml")
    val rdfFilename = opts.getOrElse("rdf", xmlFilename.replaceAll("\\.xml$", ".rdf"))
    val statsFilename = xmlFilename.replaceAll("\\.xml$", ".conv-stats.txt")
    val namespace   = opts("namespace")

    val xmlIn = scala.xml.XML.loadFile(xmlFilename)
    val converter = if (xmlFilename.endsWith("prefixes.xml")) new PrefixConverter(xmlIn, namespace)
                    else new UnitConverter(xmlIn, namespace)
    val model = converter.convert

    def getStats = {
      s"""udunits2rdf conversion
           |date:   ${new java.util.Date()}
           |input:  $xmlFilename
           |output: $rdfFilename
           |
           |conversion stats:
           |${converter.getStats}
        """.stripMargin
    }

    def saveModel() {
      //model.getWriter("N3").write(model, new java.io.FileOutputStream(rdfFilename + ".n3"), null)
      val writer = model.getWriter("RDF/XML-ABBREV")
      writer.setProperty("showXmlDeclaration", "true")
      writer.setProperty("relativeURIs", "same-document,relative")
      writer.setProperty("xmlbase", namespace)
      writer.write(model, new java.io.FileOutputStream(rdfFilename), null)
    }

    def writeStats(statsStr: String) {
      val pw = new PrintWriter(statsFilename)
      pw.printf(statsStr)
      pw.close()
    }

    saveModel()
    val statsStr = getStats
    writeStats(statsStr)
    println(statsStr)
  }
}
