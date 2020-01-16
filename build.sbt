import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

ThisBuild / name := "UrlDSL"

ThisBuild / version := "0.1.0"

lazy val `shared` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))

val sharedJvm = shared.jvm
val sharedJS = shared.js

ThisBuild / crossScalaVersions := Seq("2.13.1", "2.12.8", "2.11.12")
ThisBuild / scalaVersion := crossScalaVersions.value.head

ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation")

ThisBuild / libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"
)

// used as `artifactId`
ThisBuild / name := "url-dsl"

// used as `groupId`
ThisBuild / organization := "be.doeraene"

// open source licenses that apply to the project
ThisBuild / licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php"))

ThisBuild / description := "A tiny library for parsing and creating urls in a type-safe way"

import xerial.sbt.Sonatype._
ThisBuild / sonatypeProjectHosting := Some(GitHubHosting("sherpal", "url-dsl", "antoine.doeraene@gmail.com"))

// publish to the sonatype repository
ThisBuild / publishTo := sonatypePublishTo.value
