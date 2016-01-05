//////////////////////////////
// Project Meta Information //
//////////////////////////////
organization  := "de.frosner"

version       := "1.1.0-SNAPSHOT"

name          := "repl-helper"

scalaVersion  := "2.10.5"

/////////////////////
// Compile Options //
/////////////////////
fork in Compile := true

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

//////////////////////////
// Library Dependencies //
//////////////////////////
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
