package org.mmisw.udunits2rdf

import com.hp.hpl.jena.rdf.model.{Statement, Property, Resource, ModelFactory}
import com.hp.hpl.jena.vocabulary.{XSD, RDFS, OWL, RDF}
import scala.collection.mutable

/**
 * Creates the RDF with common definitions for the vocabularies.
 * @param namespace Namespace for the base definitions
 */
class BaseDefs(val namespace: String) {
  require(namespace.matches(".*(/|#)$"), "namespace must end with / or #")

  val model = {
    val model = ModelFactory.createDefaultModel()
    model.setNsPrefix("",      namespace)
    model.setNsPrefix("rdfs",  RDFS.getURI)
    model.setNsPrefix("owl",   OWL.getURI)
    model
  }

  private val statementMap = mutable.Map[Boolean, List[Statement]]()
  statementMap.update(true, List.empty)
  statementMap.update(false, List.empty)

  private def addStatement(forPrefix: Boolean, statement: Statement) {
    val statements = statementMap.get(forPrefix).head
    statementMap.update(forPrefix, statement :: statements)
  }

  /**
   * Adds the definitions that correspond to the given generated vocabulary.
   * @param vocName If None, all definitions are added.
   */
  def addDefinitionsToModel(vocName: Option[String] = None) {
    val statementsToAdd = vocName match {
      case None             => statementMap.values.flatten
      case Some("prefixes") => statementMap.getOrElse(true, List.empty)
      case _                => statementMap.getOrElse(false, List.empty)
    }
    statementsToAdd foreach model.add
  }

  def saveModel(filename: String) {
    util.saveModel(namespace, model, filename)
  }

  val forPrefix = true
  val PrefixClass    = createClass(forPrefix, "Prefix")

  val hasValue       = createProperty(forPrefix, "hasValue")
  val hasPrefixName  = createProperty(forPrefix, "hasPrefixName")

  addDomain(forPrefix, hasValue,       PrefixClass)
  addDomain(forPrefix, hasPrefixName,  PrefixClass)

  addType(forPrefix, hasValue,      OWL.FunctionalProperty)

  val forOther = false
  val UnitClass      = createClass(forOther, "Unit")
  val UnitNameClass  = createClass(forOther, "UnitName")

  val hasDefinition  = createProperty(forOther, "hasDefinition")
  val hasName        = createProperty(forOther, "hasName", range = UnitNameClass)
  val hasAlias       = createProperty(forOther, "hasAlias", range = UnitNameClass)

  val namesUnit      = createProperty(forOther, "namesUnit", range = UnitClass)
  val hasSymbol      = createProperty(forOther, "hasSymbol")
  val hasCardinality = createProperty(forOther, "hasCardinality")

  addDomain(forOther, hasDefinition,  UnitClass)
  addDomain(forOther, hasName,        UnitClass)
  addDomain(forOther, hasAlias,       UnitClass)

  addDomain(forOther, namesUnit,      UnitNameClass)
  addDomain(forOther, hasSymbol,      UnitNameClass)
  addDomain(forOther, hasCardinality, UnitNameClass)

  addType(forOther, hasName,       OWL.FunctionalProperty)
  addType(forOther, hasDefinition, OWL.FunctionalProperty)
  addType(forOther, namesUnit,     OWL.FunctionalProperty)

  private def createProperty(forPrefix: Boolean, name: String, range: Resource = XSD.xstring): Property = {
    val prop = model.createProperty(namespace + name)
    addStatement(forPrefix, model.createStatement(prop, RDFS.range, range))
    prop
  }

  private def addType(forPrefix: Boolean, prop: Property, `type`: Resource) {
    addStatement(forPrefix, model.createStatement(prop, RDF.`type`, `type`))
  }

  private def addDomain(forPrefix: Boolean, prop: Property, domains: Resource*) {
    for (domain <- domains) addStatement(forPrefix, model.createStatement(prop, RDFS.domain, domain))
  }

  private def createClass(forPrefix: Boolean, name: String): Resource = {
    val clazz = model.createResource(namespace + name)
    addStatement(forPrefix, model.createStatement(clazz, RDF.`type`, OWL.Class))
    addStatement(forPrefix, model.createStatement(clazz, RDFS.label, name))
    clazz
  }
}

