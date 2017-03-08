package keehive

object AppController {
  val welcomeBanner = s"""
    |*************************************************
    |            WELCOME TO keehive!
    |                version ${Main.version}
    |     keehive is a terminal password manager
    |       PROTECT YOUR KEYBOARD FROM PEEKERS!
    |              Type '?' for help
    |           Press TAB for completion
    |  Built with Scala ${util.Properties.versionNumberString} running Java ${System.getProperty("java.version")}
    |*************************************************
    """.stripMargin

  val helpText = s"""
    |Press TAB for completion of these commands (alphabetical order):
    |
    |add       add a new record, enter each field after prompt
    |add id    add a new record with id, enter each field after prompt
    |
    |copy s    copy password of record with id starting with s
    |          example: c<TAB> s<TAB>      copy password of id starting with s
    |copy s f  copy field f of record with id starting with s
    |          example: c<TAB> myid url    copy the url field of id:myid
    |
    |del 42    delete the record at index 42
    |del id    delete the record with id
    |
    |edit 42   edit the record at index 42
    |edit id   edit the record with id
    |edit i f  edit/add the field f of record with id/index i
    |
    |help      show this message; also ?
    |
    |list      list all records, hide password
    |list 42   list fileds of record with index 42, hide password
    |list s    list fields of record with id that starts with s, hide password
    |
    |quit      quit keehive; also Ctrl+D
    |
    |show      list all records, show password
    |show 42   list fileds of record with index 42, show password
    |show s    list fields of record with id that starts with s, show password
    |
    |xport     export all records to clipboard as plain tex
    """.stripMargin

  val cmdPrompt     = "\nkeehive> "
  val mpwPrompt     = "Enter secure master password: "


  def start(nonDefaultPath: String = ""): Unit  = {
    println(welcomeBanner)
    cmdLoop()
  }

  case class Cmd(cmd: String, exec: String => Unit, helpMsg: String= "")

  val commands = Vector(
    Cmd("help", help),
    Cmd("quit", _ => Main.quit())
  )

  def help(arg: String = ""): Unit = Terminal.put(helpText)

  def doCmd(cmd: String, arg: String): Unit =
    if (cmd == "?") help(arg)
    else if (cmd.size > 0) {
      val firstFoundCmdOpt = commands.find(_.cmd == cmd)
      if (firstFoundCmdOpt.isDefined) firstFoundCmdOpt.get.exec(arg)
      else Terminal.put(s"Unkown command: $cmd\nTry ? for help")
    }

  def cmdArg(cmdLine: String): (String, String) = {
    val (cmd, arg) = cmdLine.span(_ != ' ')
    (cmd, arg.trim)
  }

  @annotation.tailrec
  final def cmdLoop(): Unit = {
    val cmdLine = Terminal.get(cmdPrompt)
    if (cmdLine == Terminal.CtrlD) Main.quit()
    val (cmd, arg) = cmdArg(cmdLine)
    doCmd(cmd, arg)
    cmdLoop()
  }
}
