addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.7")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")
//addSbtPlugin("com.dwijnand" % "sbt-dynver" % "3.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.16.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.6")
