name := "World Earthquake Forecaster"

version := "0.1"

scalaVersion := "2.12.12"

val scalaTestVersion = "3.1.0"
val sparkVersion = "3.0.1"
val log4jVersion = "2.4.1"

enablePlugins(PlayScala)

routesGenerator := InjectedRoutesGenerator

cancelable in Global := true

libraryDependencies ++= Seq(
  guice,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion
)
