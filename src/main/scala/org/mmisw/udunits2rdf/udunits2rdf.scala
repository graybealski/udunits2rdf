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
      val omvmmi = "http://mmisw.org/ont/mmi/20081020/ontologyMetadata/"
      val omv    = "http://omv.ontoware.org/2005/05/ontology#"

      model.setNsPrefix("omvmmi", omvmmi)
      model.setNsPrefix("omv", omv)

      val origVocab = origDir + "udunits2-" + vocName + ".xml"

      converter.ontology.addStringProperty(omvmmi + "hasResourceType", "unit")
      converter.ontology.addStringProperty(omvmmi + "hasContentCreator", "Unidata")
      converter.ontology.addStringProperty(omvmmi + "origVocUri", origVocab)
      converter.ontology.addStringProperty(omvmmi + "origVocManager", "Steve Emmerson (303-497-8648, emmerson@ucar.edu, http://www.unidata.ucar.edu/staff/steve/)")
      converter.ontology.addStringProperty(omvmmi + "contact",        "Steve Emmerson (303-497-8648, emmerson@ucar.edu, http://www.unidata.ucar.edu/staff/steve/)")
      converter.ontology.addStringProperty(omvmmi + "contactRole",    "Content Manager")
      converter.ontology.addStringProperty(omvmmi + "temporaryMmiRole", "Ontology Producer")
      converter.ontology.addStringProperty(omvmmi + "creditRequired", "no")
      converter.ontology.addStringProperty(omvmmi + "origVocDocumentationUri", origVocab)
      converter.ontology.addStringProperty(omvmmi + "origVocKeywords", "units, Unidata, udunits, scientific units, base units")
      converter.ontology.addStringProperty(omvmmi + "origVocSyntaxFormat", "XML")

      converter.ontology.addStringProperty(omv + "name", "udunits2-" + vocName)
      converter.ontology.addStringProperty(omv + "acronym", "udunits2-" + vocName)
      converter.ontology.addStringProperty(omv + "documentation", "http://www.unidata.ucar.edu/software/udunits/udunits-2-units.html")
      converter.ontology.addStringProperty(omv + "hasCreator", "MMI")
      converter.ontology.addStringProperty(omv + "reference", "https://github.com/mmisw/udunits2rdf/wiki")
      converter.ontology.addStringProperty(omv + "hasContributor", "John Graybeal, Carlos Rueda")
      converter.ontology.addStringProperty(omv + "keywords", "units, Unidata, udunits, scientific units, base units")
    }

    addMetadata()

    util.saveModel(namespace, model, rdfFilename + ".n3")
    util.saveModel(namespace, model, rdfFilename)

    val statsStr = getStats
    writeStats(statsStr)
    println(statsStr)
  }
}
