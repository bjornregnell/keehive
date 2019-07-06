package keehive

object Main {
  val version = "0.5"
  val isHelp:    Set[String] = Set("-h", "?", "--help", "-help", "help")
  val isInstall: Set[String] = Set("--install", "-i")
  val isVault:   Set[String] = Set("--vault", "-v")
  val usageHelpMsg: String = s"""
    | Keehive terminal password manager version: $version
    |
    | args:
    |   no args              launch keehive using default vault dir: ~/keehive
    |   --vault [path]       launch keehive with specified path to vault dir
    |   -v [path]            same as --vault [path]
    |   --install [path]     install keehive, in optional path, default: ~/keehive
    |   -i [path]            same as --install [path]
    """.stripMargin.trim

  val GitHubUrl     = "https://github.com/bjornregnell/keehive"
  val GitHubRawUrl  = "https://raw.githubusercontent.com/bjornregnell/keehive/master"
  val GitHubRelease = s"$GitHubUrl/releases/download"

  val defaultPath  = s"${Disk.userDir}/keehive"
  var path: String = defaultPath

  def main(args: Array[String]): Unit = {
    def setPath(argsIndex: Int = 1): Unit = { path = args.lift(argsIndex).getOrElse(path) }
    args.headOption match {
      case Some(arg) if isInstall(arg) => setPath(); install()
      case Some(arg) if isVault(arg)   => setPath(); AppController.start()
      case Some(arg) if isHelp(arg)    => quit(usageHelpMsg)
      case _                           => AppController.start()
    }
  }

  def quit(msg: String = "Goodbye!"): Unit = { println(msg); sys.exit(0) }

  def abort(errMsg: String): Unit = { println(s"Error: $errMsg"); sys.exit(1) }

  import scala.concurrent.{Future, Await}
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  val latestVersionFuture: Future[String] = Future {
    Download.asString(s"$GitHubRawUrl/version.txt")
  }

  def latestVersion: String = scala.util.Try(latestVersionFuture.value.get.get).getOrElse("")

  //TODO refactor corresponding functionality in AppController to here:
  def isUpdateAvailable: Boolean = latestVersion.nonEmpty && latestVersion != version

  def install(): Unit = {
    val _ = scala.util.Try {

      def createDir(dir: String): Unit =
        if (Disk.createDirIfNotExist(dir)) println(s"Directory created: $dir")

      def download(source: String, dest: String): Unit = {
        println(s"Downloading: $source\nOutfile: $dest")
        Download.toBinaryFile(source, dest)
      }

      def isWindows: Boolean = System.getProperty("os.name").toLowerCase.contains("win")

      println(s"Keehive installer, current version: $version")
      println(Seq("os.name", "os.version", "java.vm.name", "java.runtime.version").
        map(System.getProperty).mkString(" "))
      createDir(path)
      println("Checking if latest version... ")
      Await.ready(latestVersionFuture, 5.seconds)

      if (latestVersion.nonEmpty) {
        val v = latestVersion
        println(s"\nAttempting installation of latest version $v in directory: $path")
        createDir(s"$path/bin")
        val jarFile = s"$path/bin/keehive-$v.jar"
        if (!Disk.isExisting(jarFile)) {
          download(source = s"$GitHubRelease/v$v/keehive-$v.jar", dest = jarFile)
          val launcher = if (isWindows) "kh.bat" else "kh"
          val launchCmd = if (isWindows) s"""java -jar $jarFile %*\n""" else  s"""java -jar $jarFile "$$@"\n"""
          Disk.saveString(launchCmd, s"$path/bin/$launcher")
          if (isWindows)
            println(s"\nRun keehive by double-clicking on $path/bin/$launcher\nor write this in cmd or powershell:\n$launchCmd")
          else {
            scala.sys.process.Process(s"chmod +x $path/bin/$launcher").!  // make launcher executable
            println(s"\nRun keehive using this command in terminal:\nsource $path/bin/$launcher")
            println(s"\nTo install the kh command for keehive, enter this command in terminal:")
            println(s"sudo ln -s $path/bin/$launcher /usr/local/bin")
          }
        } else println(s"\nError: File already exists: $jarFile")
      } else println("\nError: Version info is not yet available. Check internet connection to https://github.com")

    }.recover{case e => println(s"Error: $e")}
  }

}
