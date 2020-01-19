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
    version := "0.1.3",
    crossScalaVersions := Seq("2.13.1", "2.12.10"),
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
  .settings(
    publishTo := sonatypePublishTo.value,
    sonatypeProjectHosting := Some(GitHubHosting("sherpal", "url-dsl", "antoine.doeraene@gmail.com")),
    organization := "be.doeraene",
    description := "A tiny library for parsing and creating urls in a type-safe way",
    licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php"))
//    developers := List(
//      Developer(
//        "sherpal",
//        "Antoine Doeraene",
//        "antoine.doeraene@gmail.com",
//        new java.net.URL("https://github.com/sherpal")
//      )
//    ),
//    homepage := Some(new java.net.URL("https://github.com/sherpal/url-dsl")),
//    pomExtra := <url>https://github.com/sherpal/url-dsl</url><developers>
//      <developer>
//        <id>sherpal</id>
//        <name>Antoine Doeraene</name>
//        <url>https://github.com/sherpal</url>
//      </developer>
//    </developers>

    //sonatypeProjectHosting := Some(GitHubHosting("sherpal", "url-dsl", "antoine.doeraene@gmail.com"))
  )

val sharedJvm = shared.jvm
val sharedJS = shared.js
