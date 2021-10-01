// Settings file for all the modules.
import xml.Group
import sbt._
import Keys._

organization := "com.github.swagger-akka-http"

ThisBuild / scalaVersion := "2.13.6"

ThisBuild / crossScalaVersions := Seq("2.11.12", "2.12.15", "2.13.6")

ThisBuild / organizationHomepage := Some(url("https://github.com/swagger-akka-http/swagger-enumeratum-module"))

ThisBuild / scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")

Test / publishArtifact := false

pomIncludeRepository := { x => false }

libraryDependencies ++= Seq(
  "io.swagger.core.v3" % "swagger-core-jakarta" % "2.1.11",
  "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.5.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0",
  "com.beachape" %% "enumeratum" % "1.7.0",
  "org.scalatest" %% "scalatest" % "3.2.10" % Test,
  "org.slf4j" % "slf4j-simple" % "1.7.32" % Test
)

homepage := Some(new URL("https://github.com/swagger-akka-http/swagger-enumeratum-module"))

Test / parallelExecution := false

startYear := Some(2020)

licenses := Seq(("Apache License 2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0.html")))

releasePublishArtifactsAction := PgpKeys.publishSigned.value

pomExtra := {
  pomExtra.value ++ Group(
      <issueManagement>
        <system>github</system>
        <url>https://github.com/swagger-api/swagger-enumeratum-module/issues</url>
      </issueManagement>
      <developers>
        <developer>
          <id>pjfanning</id>
          <name>PJ Fanning</name>
          <url>https://github.com/pjfanning</url>
        </developer>
      </developers>
  )
}

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)
