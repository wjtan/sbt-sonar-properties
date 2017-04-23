name := "sbt-sonar-properties"

organization := "sg.wjtan"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.6"

sbtPlugin := true

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.2.0")
