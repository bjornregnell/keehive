lazy val appname = "keehive"
lazy val versnum = "0.4"

lazy val root = (project in file(".")).
  settings(
    organization := "se.bjornregnell",
    scalaVersion := "2.11.8",
    version      := s"$versnum-SNAPSHOT",
    name := appname,
    assemblyJarName in assembly := s"$appname-$versnum.jar"
  )
