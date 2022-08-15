import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val json4s = "org.json4s" %% "json4s-native" % "3.6.0-M3"

  var serverDeps = Seq(json4s)
}
