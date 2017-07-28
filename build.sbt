lazy val appname = "keehive"
lazy val versnum = "0.4"

lazy val root = (project in file(".")).
  settings(
    organization := "se.bjornregnell",
    scalaVersion := "2.12.2",
    version      := s"$versnum-SNAPSHOT",
    name := appname,
    assemblyJarName in assembly := s"$appname-$versnum.jar",
    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation",
      "-Xfuture",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ywarn-unused"
    )
  )
