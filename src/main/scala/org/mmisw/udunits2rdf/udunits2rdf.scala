package org.mmisw.udunits2rdf

import java.io.PrintWriter
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

/**
 * main program.
 */
object udunits2rdf extends App {

  val config = ConfigFactory.load().getConfig("udunits2rdf")

  val origDir             = config.getString("origDir")
  val basedefsNamespace   = config.getString("basedefs")
  val baseNamespace       = config.getString("baseNamespace")

  config.getStringList("vocs") foreach processVoc

  def processVoc(vocName: String) {

    val xmlFilename         = s"src/main/resources/udunits2-$vocName.xml"
    val namespace           = s"$baseNamespace/udunits2-$vocName/"

    val rdfFilename   = xmlFilename.replaceAll("\\.xml$", ".rdf")
    val statsFilename = xmlFilename.replaceAll("\\.xml$", ".conv-stats.txt")

    val baseDefs = new BaseDefs(basedefsNamespace)
    if (namespace != baseDefs.namespace) baseDefs.saveModel("src/main/resources/udunits2.n3")

    val xmlIn = scala.xml.XML.loadFile(xmlFilename)

    val converter = if (xmlFilename.endsWith("prefixes.xml"))
      new PrefixConverter(xmlIn, baseDefs, namespace) else
      new UnitConverter(xmlIn, baseDefs, namespace)

    val model = converter.convert

    def getStats = {
      s"""udunits2rdf conversion
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

    def addMetadata() {
      def addStr(name:String, value:String) { converter.ontology.addStringProperty(name, value) }

      val omvmmi = "http://mmisw.org/ont/mmi/20081020/ontologyMetadata/"
      val omv    = "http://omv.ontoware.org/2005/05/ontology#"

      model.setNsPrefix("omvmmi", omvmmi)
      model.setNsPrefix("omv", omv)

      val origVocab = origDir + "udunits2-" + vocName + ".xml"

      val contact = "Steve Emmerson (303-497-8648, emmerson@ucar.edu, http://www.unidata.ucar.edu/staff/steve/)"
      val documentation = "http://www.unidata.ucar.edu/software/udunits/udunits-2-units.html"

      addStr(omvmmi + "hasResourceType",          "unit")
      addStr(omvmmi + "hasContentCreator",        "Unidata")
      addStr(omvmmi + "origVocUri",               origVocab)
      addStr(omvmmi + "origVocManager",           contact)
      addStr(omvmmi + "contact",                  contact)
      addStr(omvmmi + "contactRole",              "Content Manager")
      addStr(omvmmi + "temporaryMmiRole",         "Ontology Producer")
      addStr(omvmmi + "creditRequired",           "no")
      addStr(omvmmi + "origVocDocumentationUri",  origVocab)
      addStr(omvmmi + "origVocKeywords",          "units, Unidata, udunits, scientific units, base units")
      addStr(omvmmi + "origVocSyntaxFormat",      "XML")

      addStr(omv + "name",             "udunits2-" + vocName)
      addStr(omv + "acronym",          "udunits2-" + vocName)
      addStr(omv + "documentation",    documentation)
      addStr(omv + "hasCreator",       "MMI")
      addStr(omv + "reference",        "https://github.com/mmisw/udunits2rdf/wiki")
      addStr(omv + "hasContributor",   "John Graybeal, Carlos Rueda")
      addStr(omv + "keywords",         "units, Unidata, udunits, scientific units, base units")
    }

    addMetadata()

    util.saveModel(namespace, model, rdfFilename + ".n3")
    util.saveModel(namespace, model, rdfFilename)

    val statsStr = getStats
    writeStats(statsStr)
    println(statsStr)
  }
}
