lazy val appname = "keehive"
lazy val versnum = "0.5"
// put jline in lib/ download from here: 
//      https://repo1.maven.org/maven2/jline/jline/2.14.6/jline-2.14.6.jar

organization := "se.bjornregnell"
scalaVersion := "2.12.8"
version      := s"$versnum-SNAPSHOT"
name := appname
assemblyJarName in assembly := s"$appname-$versnum.jar"
scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused"
)
