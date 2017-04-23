name := "sbt-sonar-properties"

organization := "sg.wjtan"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.6"

sbtPlugin := true

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.2.0")

bintrayRepository := "sbt-plugins"

bintrayPackage := "sbt-sonar-properties"

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))