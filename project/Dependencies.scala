import sbt._

object Dependencies {
  lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val json4s: ModuleID = "org.json4s" %% "json4s-native" % "3.6.0-M3"

  var serverDeps: Seq[ModuleID] = Seq(json4s)
}
