import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import xerial.sbt.Sonatype._

ThisBuild / scalacOptions ++= Seq( // use ++= to add to existing options
  "-encoding",
  "utf8", // if an option takes an arg, supply it on the same line
  "-feature", // then put the next option on a new line for easy editing
  "-language:implicitConversions",
  "-language:existentials",
  "-unchecked",
  // "-Xfatal-warnings",
  "-deprecation"
)

inThisBuild(
  List(
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
    scalaVersion := crossScalaVersions.value.head
  )
)

lazy val `url-dsl` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("url-dsl"))
  .settings(name := "url-dsl")
  .settings(
    libraryDependencies ++= Seq(
      "app.tulz" %%% "tuplez-full-light" % "0.4.0",
      "org.scalatest" %%% "scalatest" % "3.2.14" % Test,
      "org.scalacheck" %%% "scalacheck" % "1.17.0" % Test,
      "org.scalameta" %%% "munit" % "0.7.29" % Test
    ),
    (Compile / doc / scalacOptions) ++= Seq(
      "-no-link-warnings" // Suppress scaladoc "Could not find any member to link for" warnings
    )
  )
  .jsSettings(
    scalacOptions ++= sys.env.get("CI").map { _ =>
      val localSourcesPath = (LocalRootProject / baseDirectory).value.toURI
      val remoteSourcesPath = s"https://raw.githubusercontent.com/sherpal/url-dsl/${git.gitHeadCommit.value.get}/"
      val sourcesOptionName = if (scalaVersion.value.startsWith("2.")) "-P:scalajs:mapSourceURI" else "-scalajs-mapSourceURI"

      s"${sourcesOptionName}:$localSourcesPath->$remoteSourcesPath"
    }
  )
  .jvmSettings(
    coverageFailOnMinimum := true,
    coverageMinimumStmtTotal := 99,
    coverageMinimumBranchTotal := 100,
    coverageMinimumStmtPerPackage := 80,
    coverageMinimumBranchPerPackage := 100,
    coverageMinimumStmtPerFile := 60,
    coverageMinimumBranchPerFile := 100
  )

lazy val root = project
  .in(file("."))
  .aggregate(`url-dsl`.js, `url-dsl`.jvm)
  .settings(
    publish / skip := true
  )
