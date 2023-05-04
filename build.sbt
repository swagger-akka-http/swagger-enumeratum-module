// Settings file for all the modules.
import xml.Group
import sbt._
import Keys._
import org.typelevel.sbt.gha.JavaSpec.Distribution.Zulu

organization := "com.github.swagger-akka-http"

ThisBuild / scalaVersion := "2.13.10"

ThisBuild / crossScalaVersions := Seq("2.11.12", "2.12.17", "2.13.10", "3.2.2")

ThisBuild / organizationHomepage := Some(url("https://github.com/swagger-akka-http/swagger-enumeratum-module"))

ThisBuild / scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")

Test / publishArtifact := false

pomIncludeRepository := { x => false }

//resolvers ++= Resolver.sonatypeOssRepos("snapshots")

libraryDependencies ++= Seq(
  "io.swagger.core.v3" % "swagger-core-jakarta" % "2.2.9",
  "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.9.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2",
  "com.beachape" %% "enumeratum" % "1.7.2",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.slf4j" % "slf4j-simple" % "2.0.7" % Test
)

homepage := Some(new URL("https://github.com/swagger-akka-http/swagger-enumeratum-module"))

Test / parallelExecution := false

startYear := Some(2020)

licenses := Seq(("Apache License 2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.html")))

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

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec(Zulu, "8"))
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
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}",
      "CI_SNAPSHOT_RELEASE" -> "+publishSigned"
    )
  )
)
