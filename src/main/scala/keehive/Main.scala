package keehive

object Main {
  val version = "0.2"
  val isHelp:    Set[String] = Set("-h", "?", "--help", "-help", "help")
  val isInstall: Set[String] = Set("--install", "-i")
  val isVault:   Set[String] = Set("--vault", "-v")
  val usageHelpMsg: String = s"""
    | keehive terminal password manager version: $version
    | usage:
    |   java -jar keehive-$version.jar [args]
    |
    | args:
    |   no args              start keehive using default vault dir: ~/keehive
    |   --install [path]     install keehive, in optional path, default: ~/keehive
    |   -i [path]            same as --install [path]
    |   --vault [path]       set path of vault directory, default ~/keehive
    |   -v [path]            same as --vault [path]
    """.stripMargin.trim

  val GithubUrl    = "https://github.com/bjornregnell/keehive"
  val GithubRawUrl = "https://raw.githubusercontent.com/bjornregnell/keehive/master"

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

  val latestVersionFuture: Future[Option[String]] = Future {
    // download this in a Future to make app start fast
    val s = Download.asString(s"$GithubRawUrl/version.txt")
    val key = "latest-version:"
    if (s.startsWith(key)) Some(s.stripPrefix(key)) else None
  }

  def latestVersion: String = scala.util.Try(latestVersionFuture.value.get.get.get).getOrElse("")

  def isUpdateAvailable: Boolean = latestVersion.nonEmpty && latestVersion != version

  def install(): Unit = scala.util.Try {
    println(s"Installing keehive in directory: $path")
    import scala.concurrent.duration._
    Await.ready(latestVersionFuture, 3.seconds)
    if (latestVersion.nonEmpty) {
      val v = latestVersion
      val jarFileName = s"keehive-$v.jar"
      val jarFilePath = s"$path/$jarFileName"
      val jarFileUrl = s"$GithubUrl/releases/download/v$v/$jarFileName"
      println(s"Downloading: $jarFileUrl\n\nOutfile: $jarFilePath")
      val exists = Disk.createIfNotExist(jarFilePath)
      Download.toBinaryFile(jarFileUrl, jarFilePath)
      print("\n")
    } else println("Error: No version info is available. Try again later.")
  }.recover{case e => println(s"Error: $e")}

}
