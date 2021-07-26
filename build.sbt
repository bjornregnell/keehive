lazy val appname = "keehive"
lazy val versnum = "0.6"
// put jline in lib/ download from here: 
// https://search.maven.org/artifact/org.jline/jline/3.20.0/jar
// OLD https://repo1.maven.org/maven2/jline/jline/2.14.6/jline-2.14.6.jar

organization := "se.bjornregnell"
scalaVersion := "2.12.14"
version      := s"$versnum-SNAPSHOT"
name := appname
assembly / assemblyJarName := s"$appname-$versnum.jar"
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
// jline is included in a fat jar in target by `sbt assembly`
libraryDependencies += "org.jline" % "jline" % "3.20.0"
fork                :=true
connectInput        :=true
outputStrategy      := Some(StdoutOutput)