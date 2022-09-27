import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import xerial.sbt.Sonatype._

val commonSettings = Def.settings(
  // publish to the sonatype repository
  publishTo := sonatypePublishTo.value
)

ThisBuild / publishTo := sonatypePublishTo.value
ThisBuild / name := "url-dsl"
ThisBuild / organization := "be.doeraene"
ThisBuild / sonatypeProjectHosting := Some(GitHubHosting("sherpal", "url-dsl", "antoine.doeraene@gmail.com"))
ThisBuild / description := "A tiny library for parsing and creating urls in a type-safe way"
ThisBuild / licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php"))
//ThisBuild / homepage := Some(new java.net.URL("https://github.com/sherpal/url-dsl"))
//ThisBuild / developers := List(
//  Developer(
//    "sherpal",
//    "Antoine Doeraene",
//    "antoine.doeraene@gmail.com",
//    new java.net.URL("https://github.com/sherpal")
//  )
//)
//ThisBuild / homepage := Some(url("https://github.com/sherpal/url-dsl"))

sonatypeProjectHosting := Some(GitHubHosting("sherpal", "url-dsl", "antoine.doeraene@gmail.com"))
organization := "be.doeraene"

inThisBuild(
  Def.settings(
    version := "0.4.1",
    crossScalaVersions := Seq("3.2.0", "2.13.5", "2.12.13"),
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ++= Seq("-feature", "-deprecation")
  )
)

lazy val `shared` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    name := "url-dsl",
    publishTo := sonatypePublishTo.value,
    sonatypeProjectHosting := Some(GitHubHosting("sherpal", "url-dsl", "antoine.doeraene@gmail.com")),
    organization := "be.doeraene",
    description := "A tiny library for parsing and creating urls in a type-safe way",
    licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php")),
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.9" % "test",
      "org.scalacheck" %%% "scalacheck" % "1.15.4" % "test"
    )
  )

val sharedJvm = shared.jvm
val sharedJS = shared.js
