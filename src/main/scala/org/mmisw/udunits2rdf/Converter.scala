package org.mmisw.udunits2rdf

import com.hp.hpl.jena.rdf.model.{Property, Resource, ModelFactory, Model}
import com.hp.hpl.jena.vocabulary.{RDFS, OWL, RDF}

import scala.xml.Node

/**
 * Base converter class for the unit and prefix subclasses.
 *
 * @param xmlIn       Input XML
 * @param namespace   Namespace for the generated ontology
 */
abstract class Converter(xmlIn: Node, namespace: String) {
  require(namespace.matches(".*(/|#)$"), "namespace must end with / or #")

  protected def createModel: Model = {
    val model = ModelFactory.createDefaultModel()
    model.setNsPrefix("", namespace)
    model
  }

  protected val model = createModel

  protected def createResource(name: String): Resource = {
    model.createResource(namespace + name)
  }

  protected def createProperty(name: String): Property = {
    model.createProperty(namespace + name)
  }

  protected def createClass(name: String): Resource = {
    val clazz  = createResource(name)
    model.add(model.createStatement(clazz, RDF.`type`, OWL.Class))
    model.add(model.createStatement(clazz, RDFS.label, name))
    clazz
  }

  /**
   * @return  Resulting Jena model
   */
  def convert: Model

  def getStats: String
}

/**
 * UDUnits to RDF converter. A Unit in the XML will be converted into multiple Unit
 * instances in the model, each instance corresponding to a name or alias associated
 * to the unit. Each instance will be associates with all its aliases via an alias property.
 */
class UnitConverter(xmlIn: Node, namespace: String) extends Converter(xmlIn: Node, namespace: String) {
  private val UnitClass  = createClass("Unit")
  private val defProp    = createProperty("def")
  private val symbolProp = createProperty("symbol")
  private val aliasProp  = createProperty("alias")

  private object stats {
    var numUnitsInInput = 0
    var numUnitsInOutput = 0
    var numUnitsWithNoDef = 0
    var numUnitsWithNoNameOrAlias = 0

    override def toString =
      s"""  numUnitsInInput    = $numUnitsInInput
         |  numUnitsInOutput   = $numUnitsInOutput
         |  numUnitsWithNoDef  = $numUnitsWithNoDef
         |  numUnitsWithNoNameOrAlias = $numUnitsWithNoNameOrAlias
       """.stripMargin
  }

  private def convertUnit(unit: Node) {
    // set with all names from the node: the proper names and the aliases.
    val names: Set[String] = {
      val names   = (unit \\ "name" \\ "singular") map(_.text.trim)
      val aliases = (unit \\ "aliases" \\ "name" \\ "singular") map(_.text.trim)
      (names ++ aliases).toSet
    }
    if (names.size > 0) {
      val name = names.head
      val instance0 = createUnitInstance(namespace + name, unit)

      for (otherName <- names.tail) {
        val instance = createUnitInstance(namespace + otherName, unit)
        model.add(model.createStatement(instance0, aliasProp, instance))
        model.add(model.createStatement(instance, aliasProp, instance0))
      }
    }
    else {
      stats.numUnitsWithNoNameOrAlias += 1
    }
  }

  def convert: Model = {
    for (unit <- xmlIn \\ "unit") {
      stats.numUnitsInInput += 1
      convertUnit(unit)
    }
    model
  }

  def getStats = stats.toString

  private def createUnitInstance(uri: String, unit: Node): Resource = {
    val instance = model.createResource(uri, UnitClass)
    model.add(model.createStatement(instance, RDF.`type`, UnitClass))

    val def_ = (unit \\ "def").text.trim
    if (def_.length > 0) {
      instance.addProperty(defProp, def_)
    }
    else {
      stats.numUnitsWithNoDef +=1
    }

    for (symbol <- unit \\ "symbol") {
      instance.addProperty(symbolProp, symbol.text.trim)
    }

    stats.numUnitsInOutput += 1
    instance
  }
}

/**
 * UDUnits prefixes to RDF converter.
 */
class PrefixConverter(xmlIn: Node, namespace: String) extends Converter(xmlIn: Node, namespace: String) {
  private val PrefixClass = createClass("Prefix")
  private val valueProp   = createProperty("value")
  private val symbolProp  = createProperty("symbol")

  private object stats {
    var numPrefixesInInput = 0
    var numPrefixesInOutput = 0
    var numPrefixesWithNoName = 0

    override def toString =
      s"""  numPrefixesInInput    = $numPrefixesInInput
         |  numPrefixesInOutput   = $numPrefixesInOutput
         |  numPrefixesWithNoName = $numPrefixesWithNoName
       """.stripMargin
  }

  def convert: Model = {
    for (prefix <- xmlIn \\ "prefix") {
      stats.numPrefixesInInput += 1

      val name = (prefix \ "name").text.trim
      if (name.length > 0) {
        val concept = createPrefixInstance(namespace + name)

        for (value <- prefix \\ "value") {
          concept.addProperty(valueProp, value.text.trim)
        }
        for (symbol <- prefix \\ "symbol") {
          concept.addProperty(symbolProp, symbol.text.trim)
        }
      }
      else {
        stats.numPrefixesWithNoName += 1
      }
    }
    model
  }

  def getStats = stats.toString

  private def createPrefixInstance(uri: String): Resource = {
    val instance = model.createResource(uri, PrefixClass)
    model.add(model.createStatement(instance, RDF.`type`, PrefixClass))
    stats.numPrefixesInOutput += 1
    instance
  }
}

