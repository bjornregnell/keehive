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

  // --------------- Command Control ----------------------

  def start(nonDefaultPath: String = ""): Unit  = {
    if (!nonDefaultPath.isEmpty) path = nonDefaultPath
    Terminal.put(welcomeBanner)
    Terminal.put(s"Your current keehive directory: $canonicalPath\n")
    enteredMasterPassword = Terminal.getSecret(mpwPrompt)

    val Vault.Result(vaultOpt, isCreated) = Vault.open(enteredMasterPassword, path)
    if (vaultOpt.isDefined) {
      vault = vaultOpt.get
      if (isCreated) notifyMpwCreated() else notifyMpwGood()
      setCompletions()
      cmdLoop()
    } else abortMpwBad()
  }

  @annotation.tailrec
  final def cmdLoop(): Unit = {
    val cmdLine = Terminal.get(cmdPrompt)
    if (cmdLine == Terminal.CtrlD) Main.quit()
    val (cmd, arg) = cmdArg(cmdLine)
    doCmd(cmd, arg)
    cmdLoop()
  }

  def cmdArg(cmdLine: String): (String, String) = {
    val (cmd, arg) = cmdLine.span(_ != ' ')
    (cmd, arg.trim)
  }

  case class Cmd(cmd: String, exec: String => Unit, helpMsg: String= "")

  val commands = Vector(
    Cmd("add",  addRecord),
    Cmd("del",  deleteRecord),
    Cmd("edit", editRecord),
    Cmd("list", listRecords(_, isShowAll = false)),
    Cmd("show", listRecords(_, isShowAll = true)),
    Cmd("print", _ => println(showAllRecordsAndFields)),
    Cmd("copy", copyRecord),
    Cmd("xport", _ => exportAllToClipboard()),
    Cmd("import", _ => importFromClipboard()),
    Cmd("help", help),
    Cmd("quit", _ => Main.quit())
  )

  lazy val helpLines = helpText.split('\n').toSeq
  def helpCmd(cmd: String): String = {
    val initDropped = helpLines.dropWhile(line => !line.startsWith(cmd))
    helpLines.takeWhile(line => line.startsWith(cmd)).mkString("\n")
  }

  def splitArg(arg: String): Seq[String] =
    arg.split(' ').toVector.map(_.trim).filterNot(_.isEmpty)

  def help(arg: String = ""): Unit =
    if (arg == "") Terminal.put(helpText)
    else splitArg(arg).map(arg => Terminal.put(helpCmd(arg)))

  def doCmd(cmd: String, arg: String): Unit =
    if (cmd == "?") help(arg)
    else if (cmd.size > 0) {
      val firstFoundCmdOpt = commands.find(_.cmd == cmd)
      if (firstFoundCmdOpt.isDefined) firstFoundCmdOpt.get.exec(arg)
      else Terminal.put(s"Unkown command: $cmd\nTry ? for help")
    }

  // --------------  Constants to access fields in Secrets ------------

  final val Id = "id"
  final val Pw = "pw"
  final val OldPw = "oldpw"

  final val SecretFields   = Vector(Pw,"oldpw")
  final val EnterFields    = Vector("url", "user", Pw, "info")
  final val FieldsInOrder  = Vector(Id, Pw, "url", "info", "user", "oldpw")
  final val MaxFieldLength = FieldsInOrder.map(_.length).max
  final val MaxLineLength  = 50

  // ---------------  Notifications to user ----------------------------

  def notifyMpwCreated(): Unit = Terminal.put("New master password file created.")
  def notifyMpwGood(): Unit = Terminal.put("Master password ok: keehive is open!")

  def notifySaveVault(n: Int): Unit = Terminal.put(s"Saving $n secrets in vault.")
  def notifyCreateVault(): Unit = Terminal.put("Creating new empty vault.")
  def abortMpwBad(): Unit = Main.abort("Bad master password :( ACCESS DENIED!")
  def notifyIdExists(): Unit = Terminal.put(s"That $Id already exists; pick another.")
  def notifyRecordNotFound(): Unit = Terminal.put(s"That record does not exists.")
  def notifyIndexNotFound(): Unit = Terminal.put(s"Index out of bounds.")
  def notifyIdMustBeOneWord(): Unit = Terminal.put(s"$Id must be one word.")
  def notifyIdCannotBeInteger(): Unit = Terminal.put(s"$Id cannot be integer.")

  // ----------------- mutable attributes ------------------------------

  private var enteredMasterPassword: String = _
  private var vault: Vault = _
  private var path: String = s"${Disk.userDir}/keehive/"

  // ----------------- utilities --------------------------------------

  def canonicalPath = new java.io.File(path).getCanonicalPath

  def randomStr(n: Int = 8): String = java.util.UUID.randomUUID().toString.take(n)
  def randomId(): String = {
    var rid = randomStr()
    while (vault.isExisting(field = Id, value = rid)) rid = randomStr()
    rid
  }

  def indentString(s: String, indent: Int = 2): String =  {
    val pad: String = " " * indent
    s.split("\n").mkString(pad, "\n" + pad, "")
  }

  def toIntOpt(s: String): Option[Int] = scala.util.Try(s.toInt).toOption

  def isInt(xs: String*): Boolean = xs.forall(s => toIntOpt(s).isDefined)

  def showAllFields(r: Secret): String = r.showLines(FieldsInOrder,Seq())

  def showAllRecordsAndFields: String =
    vault.toVector.map(showAllFields).mkString("","\n\n","\n")

  def showRecordById(id: String, fieldsToExclude: Seq[String]): Unit = {
    val i = vault.indexStartsWith(field = Id, valueStartsWith = id)
    if (i >= 0) Terminal.put(vault(i).showLines(FieldsInOrder, fieldsToExclude))
    else notifyRecordNotFound
  }

  def showRecordByIndex(ix: Int, fieldsToExclude: Seq[String]): Unit =
    if (ix >= 0 && ix < vault.size)
      Terminal.put(vault(ix).showLines(FieldsInOrder, fieldsToExclude))
    else notifyIndexNotFound

  def listRange(fromIndex: Int, untilIndex: Int, fieldsToExclude: Seq[String]): Unit =
    for (i <- fromIndex until untilIndex) {
      val maybeTooLongString = vault(i).show(FieldsInOrder, fieldsToExclude)
      val showString =  maybeTooLongString.take(MaxLineLength)
      val continued = if (maybeTooLongString.length > MaxLineLength) "..." else ""
      Terminal.put(s"[$i]  $showString$continued")
    }

  def setCompletions(): Unit = {
    val cmds = commands.map(_.cmd)
    Terminal.setCompletions(cmds, vault.valuesOf(Id).filterNot(_.isEmpty))
  }

  def userInput(fields: Seq[String], default: Map[String, String] = Map()): Map[String, String] = {
    fields.map { field =>
      val pad = " " * (MaxFieldLength - field.length + 1)
      val prompt = s"$field:$pad"
      val s =
        if (SecretFields contains field) Terminal.getSecret(prompt)
        else Terminal.get(prompt, default.getOrElse(field,""))
      val value = if (s == Terminal.CtrlD) "" else s
      (field, value)
    }.toMap
  }

  def copyToClipboardAndNotify(s: String): Unit =  {
    Clipboard.put(s)
    Terminal.put(s"${s.length} charachters copied to clipboard! Paste with Ctrl+V")
  }

  def fixLine(line: String): String =
    if (line contains ':') line
    else {
      Terminal.put(s"\n*** [warn] adding random fieldname; missing colon in line:\n$line\n")
      s"?${randomStr()}: $line"
    }

  def fixPair(s: String): (String, String) = {
    val indexOfColon = s.indexOf(':')
    val (k, v) = s.splitAt(indexOfColon + 1)
    (k.dropRight(1), v) //remove colon (guaranteed to exits by fixLine)
  }

  def parseFields(lines: String): Option[Secret] = {
    val xs = lines.split('\n').filterNot(_.isEmpty).map(fixLine)
    val kvs: Map[String, String] = xs.map(fixPair).collect {
      case (k,v) if !v.isEmpty => (k.trim,v.trim)
    }.toMap
    def kvsWithId =
      if (!kvs.isDefinedAt(Id)) kvs + (Id -> randomStr()) else kvs
    if (kvs.size > 0) {
      val parsed = kvsWithId
      Terminal.put(parsed.map{case (k ,v) => s"$k:$v"}.mkString("\n","\n",""))
      Some(Secret(parsed))
    } else None
  }

  // ----------------- commands ---------------------------------------

  def addRecord(arg: String): Unit = {
    val args = splitArg(arg)
    val idMaybe = if (args.size > 0) args(0) else Terminal.get(Id + ": ")
    val id = idMaybe.takeWhile(_ != ' ')
    if (id != idMaybe) notifyIdMustBeOneWord()
    else if (isInt(id)) notifyIdCannotBeInteger()
    else {
      if (!vault.isExisting(field = Id, value = id)) {
        val xs = userInput(EnterFields) + (Id -> id)
        val n = vault.add(Secret(xs))
        Terminal.put(s"New secret at last index: ${n - 1}")
        setCompletions()
      } else notifyIdExists()
    }
  }

  def deleteRecord(arg: String): Unit = {
    splitArg(arg) match {
      case Seq() => Terminal.put(s"Missing argument: index or id")

      case Seq(ix) if isInt(ix) =>
        val i = ix.toInt
        if (i >= 0 && i < vault.size) {
          if (Terminal.isOk(s"Are you shure that you want to delete [$i]")) {
            vault.remove(i)
            setCompletions()
            Terminal.put(s"Record at old index [$i] removed.")
          } else Terminal.put(s"Delete aborted.")
        } else notifyIndexNotFound

      case Seq(id) =>
        val i = vault.indexWhere(field = Id, value = id)
        if (i < 0) notifyRecordNotFound
        else if (Terminal.isOk(s"Are you shure that you want to delete id:$id")) {
          vault.remove(i)
          setCompletions()
          Terminal.put(s"Record at old index [$i] with id:$id removed.")
        } else Terminal.put(s"Delete aborted.")

      case _ => Terminal.put(s"Too many arguments: $arg")
    }
  }

  def editRecord(arg: String): Unit = {
    splitArg(arg) match {
      case Seq() => Terminal.put(s"give index or id as argument")

      case args if args.size <= 2 =>
        val i = if (isInt(args(0))) args(0).toInt
                else vault.indexWhere(field = Id, value = args(0))
        if (i >= 0 && i < vault.size) {
          val id = vault(i).get(Id)
          Terminal.put(s"Edit record with id:$id\n")
          val default = vault(i).data
          val fieldsToEdit: Seq[String] =
            if (args.size > 1) args.drop(1)
            else (vault(i).data.keySet - OldPw - Id).toSeq
          val edited = userInput(fieldsToEdit, default) + (Id -> id)
          vault(i) = Secret(vault(i).data ++ edited)
          Terminal.put(s"\nEdited record with id:$id")
          listRecords(i.toString, isShowAll = false)
        } else notifyRecordNotFound

      case _ => Terminal.put(s"too many arguments: $arg")
    }
  }

  def listRecords(arg: String, isShowAll: Boolean): Unit = {
    val fieldsToExclude = if (isShowAll) Seq() else SecretFields
    splitArg(arg) match {
      case Seq() => listRange(0, vault.size, fieldsToExclude)
      case Seq(ix) if isInt(ix) => showRecordByIndex(ix.toInt, fieldsToExclude)
      case Seq(id) => showRecordById(id, fieldsToExclude)
      case Seq(ix1, ix2) if isInt(ix1) && isInt(ix2) =>
        val last = vault.size - 1
        val (a, b) = (ix1.toInt min last max 0, ix2.toInt min last max 0)
        listRange(fromIndex = a, untilIndex = b + 1, fieldsToExclude)
      case _ => Terminal.put(s"too many arguments: $arg")
    }
  }

  def copyRecord(arg: String): Unit = {
    splitArg(arg) match {
      case Seq() => Terminal.put(s"Give index or id as argument!")

      case args if args.size <= 2 =>
        val fieldToCopy = args.lift(1).getOrElse(Pw)
        val i = if (isInt(args(0))) args(0).toInt
                else vault.indexStartsWith(field = Id, valueStartsWith = args(0))
        if (i >= 0 && i < vault.size) copyToClipboardAndNotify(vault(i).get(fieldToCopy))
        else notifyRecordNotFound

      case _ => Terminal.put(s"too many arguments: $arg")
    }
  }

  def exportAllToClipboard(): Unit = {
    Clipboard.put(showAllRecordsAndFields)
    Terminal.put(vault.size + " records copied to clipboard.")
  }

  def importFromClipboard(): Unit = {
    val items = Clipboard.get.split("\n\n").toSeq
    val fields = items.filterNot(_.isEmpty).flatMap(parseFields)
    Terminal.put(fields.map(showAllFields).mkString("","\n\n","\n"))
    val size =
      if (Terminal.isOk("Do you want to import the above records?")) {
        vault.add(fields:_*)
        setCompletions()
      } else 0
    Terminal.put(s"Appended $size records.")
  }

}
