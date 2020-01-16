name := "UrlDSL"

version := "0.1.0"

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"
)

// used as `artifactId`
name := "url-dsl"

// used as `groupId`
organization := "be.doeraene"

// open source licenses that apply to the project
licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php"))

description := "A tiny library for parsing and creating urls in a type-safe way"

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("sherpal", "url-dsl", "antoine.doeraene@gmail.com"))

// publish to the sonatype repository
publishTo := sonatypePublishTo.value
