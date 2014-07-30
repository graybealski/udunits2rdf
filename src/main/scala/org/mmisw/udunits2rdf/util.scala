package org.mmisw.udunits2rdf

import com.hp.hpl.jena.rdf.model.Model

object util {

  def saveModel(xmlbase: String, model: Model, filename: String) {
    val writer = if (filename.endsWith(".n3")) {
      model.getWriter("N3")
    }
    else {
      val writer = model.getWriter("RDF/XML-ABBREV")
      writer.setProperty("showXmlDeclaration", "true")
      writer.setProperty("relativeURIs", "same-document,relative")
      writer.setProperty("xmlbase", xmlbase)
      writer
    }
    val output = new java.io.FileOutputStream(filename)
    writer.write(model, output, null)
  }

}
