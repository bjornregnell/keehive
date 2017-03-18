package keehive

object Main {
  val version = "0.4"
  val helpArgs: Set[String] = Set("-h", "?", "--help", "-help", "help")
  val usageHelpMsg: String = s"usage: java -jar keehive-$version.jar [pathToVaultDir]"

  val GithubUrl    = "https://github.com/bjornregnell/keehive/"
  val GithubRawUrl = "https://raw.githubusercontent.com/bjornregnell/keehive/master/"

  var path: String = s"${Disk.userDir}/keehive/"

  def main(args: Array[String]): Unit = {
    args.headOption match {
      case Some(arg) if helpArgs contains arg => quit(usageHelpMsg)
      case Some(otherPath) => path = otherPath; AppController.start()
      case _          => AppController.start()
    }
  }

  def quit(msg: String = "Goodbye!"): Unit = { println(msg); sys.exit(0) }

  def abort(errMsg: String): Unit = { println(s"Error: $errMsg"); sys.exit(1) }

  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global

  val latestVersionFuture: Future[Option[String]] = Future {
    // download this in a Future to make app start fast
    val s = Download.asString(s"$GithubRawUrl/version.txt")
    val key = "latest-version:"
    if (s.startsWith(key)) Some(s.stripPrefix(key)) else None
  }

  def latestVersion: String = scala.util.Try(latestVersionFuture.value.get.get.get).getOrElse("")

  def isUpdateAvailable: Boolean = if (latestVersionFuture.isCompleted) {
    scala.util.Try(latestVersion.toDouble > version.toDouble).getOrElse(false)
  } else false

  def install(path: String): Unit = {

  }
}
