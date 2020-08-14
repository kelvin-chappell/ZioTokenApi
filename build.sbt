import Dependencies._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "ZioTokenApi",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "dev.zio" %% "zio" % "1.0.0",
      "com.lihaoyi" %% "upickle" % "1.2.0",
      "org.scalaj" %% "scalaj-http" % "2.4.2"
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
