lazy val appname = "keehive"
lazy val versnum = "0.1.0"

lazy val root = (project in file(".")).
  settings(
    organization := "se.bjornregnell",
    scalaVersion := "2.11.8",
    version      := s"$versnum-SNAPSHOT",
    name := appname,
    assemblyJarName in assembly := s"$appname-0.1.0.jar"
  )
