package org.mmisw.udunits2rdf

import com.hp.hpl.jena.rdf.model.{Property, Resource, ModelFactory}
import com.hp.hpl.jena.vocabulary.{XSD, RDFS, OWL, RDF}

/**
 * Creates the RDF with common definitions for the vocabularies.
 * @param namespace
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

  def saveModel(filename: String) {
    util.saveModel(namespace, model, filename)
  }

  val UnitClass      = createClass("Unit")
  val UnitNameClass  = createClass("UnitName")

  val hasDefinition  = createProperty("hasDefinition")
  val hasName        = createProperty("hasName", range = UnitNameClass)
  val hasAlias       = createProperty("hasAlias", range = UnitNameClass)

  addDomain(hasDefinition, UnitClass)
  addDomain(hasName, UnitClass)
  addDomain(hasAlias, UnitClass)

  val namesUnit      = createProperty("namesUnit", range = UnitClass)
  val hasSymbol      = createProperty("hasSymbol")
  val hasCardinality = createProperty("hasCardinality")

  addDomain(namesUnit, UnitNameClass)
  addDomain(hasSymbol, UnitNameClass)
  addDomain(hasCardinality, UnitNameClass)

  addType(hasName,       OWL.FunctionalProperty)
  addType(hasDefinition, OWL.FunctionalProperty)
  addType(namesUnit,     OWL.FunctionalProperty)

  private def createProperty(name: String, range: Resource = XSD.xstring): Property = {
    val prop = model.createProperty(namespace + name)
    model.add(model.createStatement(prop, RDFS.range, range))
    prop
  }

  private def addType(prop: Property, `type`: Resource) {
    model.add(model.createStatement(prop, RDF.`type`, `type`))
  }

  private def addDomain(prop: Property, domains: Resource*) {
    for (domain <- domains) model.add(model.createStatement(prop, RDFS.domain, domain))
  }

  private def createClass(name: String): Resource = {
    val clazz = model.createResource(namespace + name)
    model.add(model.createStatement(clazz, RDF.`type`, OWL.Class))
    model.add(model.createStatement(clazz, RDFS.label, name))
    clazz
  }
}

