lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "se.bjornregnell",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "kkehive",
    assemblyJarName in assembly := "keehive.jar"
  )
