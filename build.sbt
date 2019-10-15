name := """rummy-server"""
organization := "de.htwg.web.tech"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.7"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
libraryDependencies += "com.h2database" % "h2" % "1.4.196"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "comde.htwg.web.tech.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "comde.htwg.web.tech.binders._"
