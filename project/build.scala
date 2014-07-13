import sbt._
import sbt.Keys._

object build extends Build {

  lazy val udunits2rdf = Project(
    id = "udunits2rdf",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "udunits2rdf",
      organization := "org.mmisw",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.2",
      libraryDependencies ++= Seq(
        "com.hp.hpl.jena"   % "jena"    % "2.6.3"
      )
    )
  )
}
