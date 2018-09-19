# sbt-sonar-properties - Generate SonarQube Scanner Project Properties

This is an [sbt](http://scala-sbt.org/) plugin for generating project properties to be used by [SonarQube Scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner).
The plugin also generate properties to use coverage reports by [JaCoCo](http://www.eclemma.org/jacoco/) through [sbt-jacoco](https://github.com/sbt/sbt-jacoco).

## Installation
Install the plugin by adding the following to `project/plugins.sbt`:

    addSbtPlugin("sg.wjtan" % "sbt-sonar-properties" % "1.0.0")

And then modify your `build.sbt` to enable the SbtByteBuddy plugin:

```
import sg.wjtan.SbtSonarPropertiesPlugin
import sg.wjtan.SbtSonarPropertiesPlugin.autoImport._

lazy val myProject = (project in file("."))
  .enablePlugins(SbtSonarPropertiesPlugin)
```

If you are using subprojects, only enable the plugin for the parent project.

## Running

To generate the project properties,
```
sbt generateSonarProperties
```
The file will be generated at `target/sonar-project.properties`, which you can use in SonarScanner:
```
sonar-scanner -Dproject.settings=target/sonar-project.properties
```

## Configurations

You can add additional properties by:
```
sonarAdditionalProperties := Seq("sonar.java.source" -> "8"),
```

You can exclude files from code analysis by:
```
sonarExclusions := Seq("test/**"),
```
This setting is passed to the `sonar.exclusions` variable in the properties.
Refer to [Sonar Exclusion](https://docs.sonarqube.org/display/SONAR/Narrowing+the+Focus) for the pattern to exclude files/directories.

You can also exclude certain subprojects from analysis by :
```
sonarModuleExclusions := Seq("sub-project-name")
```

## Changelog

1.0
------------------
- First Release

1.1
------------------
- Update to sbt-jacoco 3.1
- Update sonar properties for Sonar runner 6

## License
This project is released under terms of the [Apache 2.0](https://opensource.org/licenses/Apache-2.0).
