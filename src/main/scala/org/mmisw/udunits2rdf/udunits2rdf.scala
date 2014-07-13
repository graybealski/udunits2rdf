package org.mmisw.udunits2rdf
import scala.xml.Node

import com.hp.hpl.jena.rdf.model.{ModelFactory, Model, Resource}
import com.hp.hpl.jena.vocabulary.OWL
import com.hp.hpl.jena.vocabulary.RDF
import com.hp.hpl.jena.vocabulary.RDFS
import com.hp.hpl.jena.vocabulary.XSD
import java.io.PrintWriter


/**
 * UDUnits to RDF converter.
 *
 * @param xmlIn       Input XML
 * @param namespace   Namespace for the generated ontology
 */
class Converter(xmlIn: Node, namespace: String) {
  require(namespace.matches(".*(/|#)$"), "namespace must end with / or #")

  def createModel: Model = {
    val model = ModelFactory.createDefaultModel()
    model.setNsPrefix("", namespace)
    model
  }

  private val model = createModel
  private val UnitClass: Resource = model.createResource(namespace + "Unit")
  private val defProp    = model.createProperty(namespace + "def")
  private val symbolProp = model.createProperty(namespace + "symbol")
  private val aliasProp  = model.createProperty(namespace + "alias")

  model.add(model.createStatement(UnitClass, RDF.`type`, OWL.Class))
  model.add(model.createStatement(UnitClass, RDFS.label, "Unit"))

  object stats {
    var numUnitsInInput = 0
    var numUnitsInOutput = 0
    var numUnitsWithNoName = 0

    override def toString =
      s"""  numUnitsInInput    = $numUnitsInInput
         |  numUnitsInOutput   = $numUnitsInOutput
         |  numUnitsWithNoName = $numUnitsWithNoName
       """.stripMargin
  }

  /**
   * @return  Resulting Jena model
   */
  def convert: Model = {

    for (unit <- xmlIn \\ "unit") {
      stats.numUnitsInInput += 1

      val name = (unit \ "name").text.trim
      if (name.length > 0) {
        val id = {
          val singular = (unit \ "name" \ "singular").text.trim
          if (singular.length > 0) singular else name
        }
        createConcept(id, unit)
      }
      else {
        stats.numUnitsWithNoName += 1
      }
    }

    def createConcept(id: String, unit: Node) = {
      val concept = _createUnitInstance(namespace + id)

      for (def_ <- unit \\ "def") {
        concept.addProperty(defProp, def_.text.trim)
      }
      for (symbol <- unit \\ "symbol") {
        concept.addProperty(symbolProp, symbol.text.trim)
      }
      for (alias <- unit \\ "aliases" \\ "name" \\ "singular") {
        concept.addProperty(aliasProp, alias.text.trim)
      }
    }

    model
  }

  def _createUnitInstance(uri: String): Resource = {
    val concept = model.createResource(uri, UnitClass)
    model.add(model.createStatement(concept, RDF.`type`, UnitClass))
    stats.numUnitsInOutput += 1
    concept
  }
}

/**
 * main program.
 */
object udunits2rdf extends App {

  import scala.collection.mutable

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
        |   udunits2rdf --namespace http://mmisw.org/ont/ucar/udunits2-accepted/ --xml src/main/resources/udunits2-accepted.xml
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
    val converter = new Converter(xmlIn, namespace)
    val model = converter.convert

    def getStats = {
      s"""udunits2rdf conversion
           |date:   ${new java.util.Date()}
           |input:  $xmlFilename
           |output: $rdfFilename
           |
           |conversion stats:
           |${converter.stats}
        """.stripMargin
    }

    def saveModel() {
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
