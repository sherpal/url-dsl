import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import xerial.sbt.Sonatype._

inThisBuild(List(
  name := "url-dsl",
  organization := "be.doeraene",
  description := "A tiny library for parsing and creating urls in a type-safe way",
  homepage := Some(url("https://github.com/sherpal/url-dsl")),
  licenses := List("MIT" -> url("http://www.opensource.org/licenses/mit-license.php")),
  developers := List(
    Developer(
      "sherpal",
      "Antoine Doeraene",
      "antoine.doeraene@gmail.com",
      url("https://github.com/sherpal")
    )
  ),
  crossScalaVersions := Seq("3.2.0", "2.13.5", "2.12.13"),
  scalaVersion := crossScalaVersions.value.head,
  scalacOptions ++= Seq("-feature", "-deprecation"),
))

lazy val `shared` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.9" % "test",
      "org.scalacheck" %%% "scalacheck" % "1.15.4" % "test"
    )
  ).jvmSettings(
    publish / skip := true
  )

lazy val root = project.in(file("."))
  .aggregate(shared.js, shared.jvm).settings(
    publish / skip := true,
  )
