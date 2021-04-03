addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.7")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.2")
//addSbtPlugin("com.dwijnand" % "sbt-dynver" % "3.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.5.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
