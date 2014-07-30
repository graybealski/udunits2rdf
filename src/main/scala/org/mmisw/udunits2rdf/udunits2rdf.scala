package org.mmisw.udunits2rdf

import java.io.PrintWriter
import scala.collection.mutable


/**
 * main program.
 */
object udunits2rdf extends App {

  /**
   * helper to process arguments and run program.
   * @param opts       map of options values
   * @param required   required keys
   * @param block  block to execute
   */
  def withOptions(opts: mutable.Map[String, String], required: String*)(block : => Unit) {
    val defaults = {
      val sep = "\n    "
      sep + (for ((k,v) <- opts.toMap) yield s"$k = $v").mkString(sep)
    }
    val usage =
      s"""
        | USAGE:
        |   udunits2rdf --basedefs ns --namespace ns --xml xml [--rdf rdf]
        | Example:
        |   udunits2rdf --basedefs  http://mmisw.org/ont/mmitest/udunits2/  --namespace http://mmisw.org/ont/mmitest/udunits2-accepted/ --xml src/main/resources/udunits2-accepted.xml
        |   generates
        |       src/main/resources/udunits2.n3
        |       src/main/resources/udunits2-accepted.rdf
        |
        | Defaults: $defaults
        |
      """.stripMargin

    next(args.toList)

    def next(list: List[String]) {
      list match {
        case "--basedefs" :: namespace :: args => opts("basedefs") = namespace; next(args)
        case "--namespace" :: namespace :: args => opts("namespace") = namespace; next(args)
        case "--xml" :: xml :: args => opts("xml") = xml; next(args)
        case "--rdf" :: rdf :: args => opts("rdf") = rdf; next(args)
        case Nil => if (required.forall(opts.contains)) block else println(usage)
        case _ => println(usage)
      }
    }
  }

  val opts = mutable.Map[String, String]()

  withOptions(opts, "basedefs", "namespace", "xml") {
    val basedefsNamespace   = opts("basedefs")
    val xmlFilename = opts("xml")
    val rdfFilename = opts.getOrElse("rdf", xmlFilename.replaceAll("\\.xml$", ".rdf"))
    val statsFilename = xmlFilename.replaceAll("\\.xml$", ".conv-stats.txt")
    val namespace   = opts("namespace")

    val baseDefs = new BaseDefs(basedefsNamespace)
    if (namespace != baseDefs.namespace) baseDefs.saveModel("src/main/resources/udunits2.n3")

    val xmlIn = scala.xml.XML.loadFile(xmlFilename)
    val converter = if (xmlFilename.endsWith("prefixes.xml")) new PrefixConverter(xmlIn, namespace)
                    else new UnitConverter(xmlIn, baseDefs, namespace)
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

    def writeStats(statsStr: String) {
      val pw = new PrintWriter(statsFilename)
      pw.printf(statsStr)
      pw.close()
    }

    util.saveModel(namespace, model, rdfFilename + ".n3")
    util.saveModel(namespace, model, rdfFilename)

    val statsStr = getStats
    writeStats(statsStr)
    println(statsStr)
  }
}
