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
    version := "1.0.0",
    crossScalaVersions := Seq("3.0.0-M2"),
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ++= Seq("-feature", "-deprecation", "-language:implicitConversions")
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
      "org.scalacheck" %%% "scalacheck" % "1.15.1" % "test"
    ),
    scalacOptions ++= List(
      //"-source:3.0-migration",
      //"-rewrite",
      //"-new-syntax",
      //"-indent"
    )
  )
  .jvmSettings(
    libraryDependencies ++= List(
      "org.scalatest" %% "scalatest" % "3.2.3" % "test"
    )
  )
  .jsSettings(
    libraryDependencies ++= List(
      "org.scalatest" %%% "scalatest" % "3.2.3" % "test"
    )
  )

val sharedJvm = shared.jvm
val sharedJS = shared.js
