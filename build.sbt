name := "tcp_anadi"

version := "1.0"

scalaVersion := "2.12.2"

mainClass := Some("presentation.MainApplicationGUI")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.0",
  "com.typesafe" % "config" % "1.3.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.scalafx" % "scalafx_2.12" % "8.0.102-R11",
  "org.scalatest" % "scalatest_2.12" % "3.0.1"
)

assemblyJarName in assembly := "tcp_anadi.jar"

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")