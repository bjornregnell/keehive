package keehive

object Main {
  val version = "0.2" 
  val helpArgs: Set[String] = Set("-h", "?", "--help", "-help", "help")
  val usageHelpMsg: String = s"usage: java -jar keehive.jar [pathToVaultDir]"

  def main(args: Array[String]): Unit = args.lift(0) match {
    case Some(arg) if helpArgs contains arg => quit(usageHelpMsg)
    case Some(path) => AppController.start(path)
    case _          => AppController.start()
  }

  def quit(msg: String = "Goodbye!"): Unit = { println(msg); sys.exit(0) }

  def abort(errMsg: String): Unit = { println(s"Error: $errMsg"); sys.exit(1) }
}
