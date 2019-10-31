name := "sbt-sonar-properties"

organization := "sg.wjtan"

version := "1.1.3"

sbtPlugin := true

resolvers += Resolver.bintrayRepo("stringbean", "sbt-plugins")

addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.2.0")

bintrayRepository := "sbt-plugins"

bintrayPackage := "sbt-sonar-properties"

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
