import sbt._
import sbt.Keys._

object ScalahttpdBuild extends Build {

  lazy val scalahttpd = Project(
    id = "scala-httpd",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "scala-httpd",
      organization := "com.npcode",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" % "akka-actor" % "2.0.1",
        "org.apache.tika" % "tika-core" % "1.2",
        "org.scalatest" %% "scalatest" % "1.8",
        "junit" % "junit" % "4.10" % "test"
      )
    )
  )
}
