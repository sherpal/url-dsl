import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import xerial.sbt.Sonatype._

val commonSettings = Def.settings(
  // publish to the sonatype repository
  publishTo := sonatypePublishTo.value
)

inThisBuild(
  Def.settings(
    // used as `artifactId`
    name := "url-dsl",
    // used as `groupId`
    organization := "be.doeraene",
    licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php")),
    description := "A tiny library for parsing and creating urls in a type-safe way",
    sonatypeProjectHosting := Some(GitHubHosting("sherpal", "url-dsl", "antoine.doeraene@gmail.com")),
    version := "0.1.1",
    crossScalaVersions := Seq("2.13.1", "2.12.8", "2.11.12"),
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ++= Seq("-feature", "-deprecation"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.1.0" % "test",
      "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"
    )
  )
)

lazy val `shared` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)

val sharedJvm = shared.jvm
val sharedJS = shared.js
