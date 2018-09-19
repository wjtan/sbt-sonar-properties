package sg.wjtan

import sbt._
import sbt.Keys._
import com.github.sbt.jacoco._
import com.github.sbt.jacoco.JacocoKeys._
import scala.collection.immutable.Map
import scala.language.implicitConversions

object SbtSonarPropertiesPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = NoTrigger

  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {
    val generateSonarPropertiesFile = TaskKey[Unit]("generate-sonar-properties", "Generates Sonar Properties")
    val sonarExclusions = SettingKey[Seq[String]]("sonar-exclusions")
    val sonarModuleExclusions = SettingKey[Seq[String]]("sonar-module-exclusions")
    val sonarAdditionalProperties = SettingKey[Seq[(String, String)]]("sonar-additional-properties")
  }

  val sonarProperties = TaskKey[Seq[(String, String)]]("sonar-properties")
  val submoduleTestProperties = TaskKey[Map[String, Seq[(String, String)]]]("sonar-module-test-properties")
  val submoduleITProperties = TaskKey[Map[String, Seq[(String, String)]]]("sonar-module-it-properties")
  val submoduleProperties = TaskKey[Map[String, Seq[(String, String)]]]("sonar-module-properties")

  import autoImport._
  import SbtSonarProperties._

  lazy val defaultSettings: Seq[Setting[_]] = Seq(
    sonarExclusions := Seq(),
    sonarModuleExclusions := Seq(),
    sonarAdditionalProperties := Seq(),
    submoduleTestProperties := submoduleSettings.value.toMap,
    submoduleITProperties := submoduleITSettings.value.toMap,
    submoduleProperties := combineProperties(submoduleTestProperties.value, submoduleITProperties.value),
    sonarProperties := {
      generateSonarProperties(version.value, organization.value, name.value, submoduleProperties.value,
        sonarExclusions.value, sonarModuleExclusions.value, sonarAdditionalProperties.value)
    },
    generateSonarPropertiesFile := { writeSonarPropertiesFile(sonarProperties.value, target.value, streams.value) })

  override def projectSettings: Seq[Setting[_]] =
    defaultSettings
}

object SbtSonarProperties {
  import SbtSonarPropertiesPlugin.autoImport._

  // Convert java.io.File path to String
  implicit private def fileToString(file: java.io.File): String = file.toString

  implicit private def filePathsToString(files: Seq[File]) = files.filter(_.exists).map(_.getAbsolutePath).toSet.mkString(",")

  def submoduleSettingsTask: Def.Initialize[Task[(String, Seq[(String, String)])]] = Def.task {
    val project = (thisProject in Compile).value
    val moduleName = project.id

    val testDirectory = (javaSource in Test).value
    //In case no unit tests
    testDirectory.mkdirs

    val properties: Seq[(String, String)] = Seq(
      moduleName + ".sonar.projectBaseDir" -> project.base,
      moduleName + ".sonar.sources" -> (javaSource in Compile).value,
      moduleName + ".sonar.tests" -> testDirectory,
      moduleName + ".sonar.junit.reportPaths" -> (target in Compile).value / "test-reports",
      moduleName + ".sonar.jacoco.reportPaths" -> (jacocoDataFile in Test).value,
      moduleName + ".sonar.java.binaries" -> (classDirectory in Compile).value,
      moduleName + ".sonar.java.libraries" -> (dependencyClasspath in Compile).value.map(_.data).filter(_.exists()).mkString(","),
      moduleName + ".sonar.java.test.libraries" -> (dependencyClasspath in Test).value.map(_.data).filter(_.exists()).mkString(","))

    (moduleName, properties)
  }

  def submoduleITTask: Def.Initialize[Task[(String, Seq[(String, String)])]] = Def.task {
    val project = (thisProject in IntegrationTest).value
    val moduleName = project.id

    val itDirectory = (javaSource in IntegrationTest).value
    //In case no integration tests
    itDirectory.mkdirs

    val properties: Seq[(String, String)] = Seq(
      moduleName + ".sonar.tests" -> itDirectory,
      moduleName + ".sonar.jacoco.reportPaths" -> (jacocoDataFile in IntegrationTest).value)

    (moduleName, properties)
  }

  lazy val submoduleSettings = submoduleSettingsTask.all(ScopeFilter(inAggregates(ThisProject, includeRoot = false), inConfigurations(Compile, Test)))
  lazy val submoduleITSettings = submoduleITTask.all(ScopeFilter(inAggregates(ThisProject, includeRoot = false), inConfigurations(IntegrationTest)))

  def combineProperties(
    testProperties: Map[String, Seq[(String, String)]],
    itProperties: Map[String, Seq[(String, String)]]) = {
    (testProperties /: itProperties) {
      case (acc, entry) =>
        // Merge map
        val moduleName = entry._1
        val currentMap = entry._2.toMap

        val accMap = scala.collection.mutable.Map[String, String]() ++ acc.getOrElse(moduleName, Seq.empty)

        for ((key, value) <- currentMap) {
          if (accMap.contains(key)) {
            accMap += key -> (value.split(",").toSet ++ accMap(key).split(",").toSet).mkString(",")
          } else {
            accMap += key -> value
          }
        }

        val newMap = accMap.toSeq.map({ case (k, v) => (k, v.replace("\\", "\\\\")) })
        acc + ((moduleName, newMap))
    }
  }

  def generateSonarProperties(verion: String, organisation: String, name: String, submoduleProperties: Map[String, Seq[(String, String)]],
    sonarExclusions: Seq[String], sonarModuleExclusions: Seq[String], additionalProperties: Seq[(String, String)]): Seq[(String, String)] = {
    val currentProperties = submoduleProperties.filterKeys(!sonarModuleExclusions.contains(_));
    Seq(
      "sonar.projectKey" -> "%s:%s".format(organisation, name),
      "sonar.projectName" -> name,
      "sonar.projectVersion" -> verion,
      "sonar.sourceEncoding" -> "UTF-8",
      "sonar.modules" -> currentProperties.keys.mkString(","),
      "sonar.exclusions" -> sonarExclusions.mkString(",")) ++ additionalProperties ++ currentProperties.values.flatten
  }

  def writeSonarPropertiesFile(sonarProperties: Seq[(String, String)], outputDirectory: File, streams: TaskStreams) {
    val propertiesFile = outputDirectory / "sonar-project.properties"
    streams.log info s"Writing sonar properties to $propertiesFile"

    val propertiesAsString = sonarProperties.map { case (k, v) => "%s=%s".format(k, v) }.mkString("\n")
    IO.write(propertiesFile, propertiesAsString)
  }
}
