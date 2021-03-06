// Settings file for all the modules.
import xml.Group
import sbt._
import Keys._

organization := "com.github.swagger-akka-http"

scalaVersion := "2.13.6"

crossScalaVersions := Seq("2.11.12", "2.12.13", scalaVersion.value)

ThisBuild / organizationHomepage := Some(url("https://github.com/swagger-akka-http/swagger-enumeratum-module"))

ThisBuild / scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")

publishMavenStyle := true

Test / publishArtifact in Test := false

pomIncludeRepository := { x => false }

libraryDependencies ++= Seq(
  "io.swagger.core.v3" % "swagger-core" % "2.1.10",
  "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.3.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.4",
  "com.beachape" %% "enumeratum" % "1.7.0",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "org.slf4j" % "slf4j-simple" % "1.7.31" % Test
)

publishTo := {
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
  else
    Some("Sonatype Nexus Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}

ThisBuild / credentials += Credentials (Path.userHome / ".ivy2" / ".credentials")

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("snapshots")
)

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

homepage := Some(new URL("https://github.com/swagger-akka-http/swagger-enumeratum-module"))

Test / parallelExecution := false

startYear := Some(2020)

licenses := Seq(("Apache License 2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0.html")))

releasePublishArtifactsAction := PgpKeys.publishSigned.value

pomExtra := {
  pomExtra.value ++ Group(
    <scm>
      <connection>scm:git:git@github.com:swagger-akka-http/swagger-enumeratum-module.git</connection>
      <developerConnection>scm:git:git@github.com:swagger-akka-http/swagger-enumeratum-module.git</developerConnection>
      <url>https://github.com/swagger-akka-http/swagger-enumeratum-module</url>
    </scm>
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
