package keehive

object AppController:
  val welcomeBanner = raw"""
    |********************************************
    |      _             _     _
    |     | |           | |   (_)
    |     | | _____  ___| |__  ___   _____
    |     | |/ / _ \/ _ \ '_ \| \ \ / / _ \
    |     |   <  __/  __/ | | | |\ V /  __/
    |     |_|\_\___|\___|_| |_|_| \_/ \___|
    |
    | Welcome to keehive version ${Main.version}
    | ${Main.GitHubUrl}
    |
    | Type ? and press Enter for help.
    | Press TAB for completion.
    | Press Ctrl+L to clear screen.
    |
    | Built with Scala 3 using stdlib ${util.Properties.versionNumberString} running on:
    | ${System.getProperty("java.vm.name")} ${System.getProperty("java.version")}
    |
    |********************************************
    """.stripMargin

  val helpText = s"""
    |keehive is a terminal password manager
    |
    |Press TAB for completion of these commands (alphabetical order):
    |
    |add       add a new record, enter each field after prompt
    |add id    add a new record with id, enter each field after prompt
    |
    |copy s    copy password of record with id starting with s
    |          example: c<TAB> s<TAB>      copy password of id starting with s
    |copy s f  copy field f of record with id starting with s
    |          example: c<TAB> someId url    copy the url field of someId
    |
    |del 42    delete the record at index 42
    |del id    delete the record with id
    |
    |edit 42   edit the record at index 42
    |edit id   edit the record with id
    |edit i f  edit/add the field f of record with id/index i
    |
    |xport     export all records to clipboard as plain tex
    |
    |help      show this message; also ?
    |
    |import    import records from clipboard
    |
    |list      list summary of all records, hide password
    |list 42   list fields of record with index 42, hide password
    |list s    list fields of record with id that starts with s, hide password
    |
    |print     prints all records including password
    |
    |quit      quit keehive; also Ctrl+D
    |
    |show      list summary of all records, show password
    |show 42   list fields of record with index 42, show password
    |show s    list fields of record with id that starts with s, show password
    |
    |update    check for new versions of keehive, download and install
    """.stripMargin

  val cmdPrompt     = "\nkeehive> "
  val mpwPrompt     = "Enter master password: "

  def readMasterPassword(msg: String = mpwPrompt): String = Terminal.getSecret("\n" + msg)
  
  def abortIfUnableToVerifyMasterPassword(): Unit =
    val verifyMpw = readMasterPassword("Verify Master Password:")
    if verifyMpw != enteredMasterPassword then Main.abort("Entered passwords does not match.")

  // --------------- Command Control ----------------------


  def start(): Unit  =
    Terminal.put(welcomeBanner)
    Terminal.put(s"Vault directory: $canonicalPath")
    Settings.load()
    enteredMasterPassword = readMasterPassword()

    val Vault.Result(vaultOpt, isCreated) = Vault.open(enteredMasterPassword, Main.path)
    if vaultOpt.isDefined then
      vault = vaultOpt.get
      if isCreated then notifyMpwCreated() else notifyMpwGood()
      setCompletions()
      notifyIfUpdateAvailable()
      cmdLoop()
    else abortMpwBad()

  @annotation.tailrec
  final def cmdLoop(): Unit =
    val cmdLine = Terminal.get(cmdPrompt)
    if cmdLine == Terminal.CtrlD then Main.quit()
    val (cmd, arg) = cmdArg(cmdLine)
    doCmd(cmd, arg)
    cmdLoop()

  def cmdArg(cmdLine: String): (String, String) =
    val (cmd, arg) = cmdLine.span(_ != ' ')
    (cmd, arg.trim)

  case class Cmd(cmd: String, exec: String => Unit, helpMsg: String= "")

  val commands = Vector(
    Cmd("add",    addRecord),
    Cmd("delete", deleteRecord),
    Cmd("edit",   editRecord),
    Cmd("genpw",  _ => copyNewPasswordToClipboard()),
    Cmd("list",   listRecords(_, isShowAll = false)),
    Cmd("show",   listRecords(_, isShowAll = true)),
    Cmd("print",  _ => println(showAllRecordsAndFields)),
    Cmd("copy",   copyRecord),
    Cmd("export", _ => exportAllToClipboard()),
    Cmd("import", _ => importFromClipboard()),
    Cmd("update", _ => checkForUpdateAndInstall()),
    Cmd("help",   help),
    Cmd("quit",   _ => Main.quit())
  )

  lazy val helpLines: Seq[String] = helpText.split('\n').toSeq

  def helpCmd(cmd: String): String =
    val initDropped = helpLines.dropWhile(line => !line.startsWith(cmd))
    initDropped.takeWhile(line => line.startsWith(cmd)).mkString("\n")

  def splitArg(arg: String): Seq[String] =
    arg.split(' ').toVector.map(_.trim).filterNot(_.isEmpty)

  def help(arg: String = ""): Unit =
    if arg == "" then Terminal.put(helpText)
    else splitArg(arg).foreach(arg => Terminal.put(helpCmd(arg)))

  def doCmd(cmd: String, arg: String): Unit =
    if cmd == "?" then help(arg)
    else if cmd.nonEmpty then
      val firstFoundCmdOpt = commands.find(_.cmd == cmd)
      if firstFoundCmdOpt.isDefined then firstFoundCmdOpt.get.exec(arg)
      else Terminal.put(s"Unknown command: $cmd\nTry ? for help")

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
  def notifyMpwGood(): Unit = Terminal.put("Your vault is open!")
  def notifySaveVault(n: Int): Unit = Terminal.put(s"Saving $n secrets in vault.")
  def abortMpwBad(): Unit = Main.abort("Bad master password :( ACCESS DENIED!")
  def notifyIdExists(): Unit = Terminal.put(s"That $Id already exists; pick another.")
  def notifyRecordNotFound(): Unit = Terminal.put(s"That record does not exists.")
  def notifyIndexNotFound(): Unit = Terminal.put(s"Index out of bounds.")
  def notifyIdMustBeOneWord(): Unit = Terminal.put(s"$Id must be one word.")
  def notifyIdCannotBeInteger(): Unit = Terminal.put(s"$Id cannot be integer.")

  // ----------------- mutable attributes ------------------------------

  private var enteredMasterPassword: String = _
  def lastEnteredMasterPassword = enteredMasterPassword
  private var vault: Vault = _

  // ----------------- utilities --------------------------------------

  def canonicalPath: String  = new java.io.File(Main.path).getCanonicalPath

  def randomStr(n: Int = 8): String = java.util.UUID.randomUUID().toString.take(n)
  def randomId(): String =
    var rid = randomStr()
    while vault.isExisting(field = Id, value = rid) do rid = randomStr()
    rid

  def indentString(s: String, indent: Int = 2): String = 
    val pad: String = " " * indent
    s.split("\n").mkString(pad, "\n" + pad, "")

  def toIntOpt(s: String): Option[Int] = scala.util.Try(s.toInt).toOption

  def isInt(xs: String*): Boolean = xs.forall(s => toIntOpt(s).isDefined)

  def showAllFields(r: Secret): String = r.showLines(FieldsInOrder,Seq())

  def showAllRecordsAndFields: String =
    vault.toVector.map(showAllFields).mkString("","\n\n","\n")

  def showRecordById(id: String, fieldsToExclude: Seq[String]): Unit =
    val i = vault.indexStartsWith(field = Id, valueStartsWith = id)
    if i >= 0 then Terminal.put(vault(i).showLines(FieldsInOrder, fieldsToExclude))
    else notifyRecordNotFound()

  def showRecordByIndex(ix: Int, fieldsToExclude: Seq[String]): Unit =
    if ix >= 0 && ix < vault.size then
      Terminal.put(vault(ix).showLines(FieldsInOrder, fieldsToExclude))
    else notifyIndexNotFound()

  def listRange(fromIndex: Int, untilIndex: Int, fieldsToExclude: Seq[String]): Unit =
    for i <- fromIndex until untilIndex do
      val maybeTooLongString = vault(i).show(FieldsInOrder, fieldsToExclude)
      val showString =  maybeTooLongString.take(MaxLineLength)
      val continued = if maybeTooLongString.length > MaxLineLength then "..." else ""
      Terminal.put(s"[$i]  $showString$continued")

  def setCompletions(): Unit =
    val cs = commands.map(_.cmd)
    val _: Boolean = //return value ignored
      Terminal.setCompletions(cs, vault.valuesOf(Id).filterNot(_.isEmpty))

  def userInput(fields: Seq[String], default: Map[String, String] = Map()): Map[String, String] =
    fields.map { field =>
      val pad = " " * (MaxFieldLength - field.length + 1)
      val prompt = s"$field:$pad"

      val input =
        if SecretFields contains field then Terminal.getSecret(prompt)
        else Terminal.get(prompt, default.getOrElse(field,""))

      val value = if input == Terminal.CtrlD then "" else
        if field == Pw && input == "" then
          if Terminal.isOk("Generate new password? ENTER=no ") then
            generatePassword()
          else default.getOrElse(Pw,"")
        else input
      (field, value)
    }.toMap

  def copyToClipboardAndNotify(s: String): Unit = 
    Clipboard.put(s)
    Terminal.put(s"${s.length} characters copied to clipboard! Paste with Ctrl+V")

  def fixLine(line: String): String =
    if line contains ':' then line
    else
      val r = randomStr()
      Terminal.put(s"\n*** [warn] random field name ?$r added as colon is missing in line:\n$line\n")
      s"?$r: $line"

  def fixPair(s: String): (String, String) =
    val indexOfColon = s.indexOf(':')
    val (k, v) = s.splitAt(indexOfColon + 1)
    (k.dropRight(1), v) //remove colon (guaranteed to exits by fixLine)

  def parseFields(lines: String): Option[Secret] =
    val xs = lines.split('\n').filterNot(_.isEmpty).map(fixLine)
    val kvs: Map[String, String] = xs.map(fixPair).collect {
      case (k,v) if !v.isEmpty => (k.trim,v.trim)
    }.toMap
    def kvsWithId = if !kvs.isDefinedAt(Id) then kvs + (Id -> randomStr()) else kvs
    if kvs.nonEmpty then Some(Secret(kvsWithId)) else None

  def notifyIfUpdateAvailable(): Unit =

    def isFirstVersionGreater(vs: Seq[(String, String)]): Boolean =
      if vs.nonEmpty then
        val (first, second) = (toIntOpt(vs.head._1).getOrElse(0), toIntOpt(vs.head._2).getOrElse(0))
        if first == second then isFirstVersionGreater(vs.drop(1))
        else first > second
      else false

    if Main.latestVersion.nonEmpty then
      val vs = Main.latestVersion.split('.') zip Main.version.split('.')
      if isFirstVersionGreater(vs.toIndexedSeq) then
        Terminal.put(s"Keehive version ${Main.latestVersion} is available. Type 'update' to install.")

  def generatePassword(): String =
    val length = Settings.asInt("generatePasswordLength").getOrElse(20)
    val chars = Settings("generatePasswordChars").getOrElse("0-9 A-Z a-z")
    //Terminal.put(s"Generating $length characters that may include: $chars")
    //Terminal.put(s"Password generation settings in file: ${Settings.fileName}")
    Crypto.Password.generate(length, chars)

  // ----------------- commands ---------------------------------------

  def addRecord(arg: String): Unit =
    val args = splitArg(arg)
    val idMaybe = if args.nonEmpty then args.head else Terminal.get(Id + ": ")
    val id = idMaybe.takeWhile(_ != ' ')
    if id != idMaybe then notifyIdMustBeOneWord()
    else if isInt(id) then notifyIdCannotBeInteger()
    else
      if !vault.isExisting(field = Id, value = id) then
        val xs = userInput(EnterFields) + (Id -> id)
        val n = vault.add(Secret(xs))
        Terminal.put(s"New secret at last index: ${n - 1}")
        setCompletions()
      else notifyIdExists()

  def deleteRecord(arg: String): Unit =
    splitArg(arg) match
      case Seq() => Terminal.put(s"Missing argument: index or id")

      case Seq(ix) if isInt(ix) =>
        val i = ix.toInt
        if i >= 0 && i < vault.size then
          if Terminal.isOk(s"Are you sure that you want to delete [$i]") then
            vault.remove(i)
            setCompletions()
            Terminal.put(s"Record at old index [$i] removed.")
          else Terminal.put(s"Delete aborted.")
        else notifyIndexNotFound()

      case Seq(ix1, ix2) if isInt(ix1) && isInt(ix2) =>
        val (start, end) = (ix1.toInt, ix2.toInt)
        if start >= 0 && start < vault.size && end > start && end < vault.size then
          val n = end - start + 1
          if Terminal.isOk(s"Are you sure that you want to delete $n records at [$start-$end]") then
            vault.remove(start, n)
            setCompletions()
            Terminal.put(s"Record at old indices [$start-$end] removed.")
          else Terminal.put(s"Delete aborted.")
        else notifyIndexNotFound()

      case Seq(id) =>
        val i = vault.indexWhere(field = Id, value = id)
        if i < 0 then notifyRecordNotFound()
        else if Terminal.isOk(s"Are you sure that you want to delete id:$id") then
          vault.remove(i)
          setCompletions()
          Terminal.put(s"Record at old index [$i] with id:$id removed.")
        else Terminal.put(s"Delete aborted.")

      case _ => Terminal.put(s"Too many arguments: $arg")

  def editRecord(arg: String): Unit =
    splitArg(arg) match
      case Seq() => Terminal.put(s"Give index or id as argument!")

      case args if args.size <= 2 =>
        val i = if isInt(args.head) then args.head.toInt
                else vault.indexWhere(field = Id, value = args.head)
        if i >= 0 && i < vault.size then
          val id = vault(i).get(Id)
          Terminal.put(s"Edit record with id:$id\n")
          val default = vault(i).data
          val fieldsToEdit: Seq[String] =
            if args.size > 1 then args.drop(1)
            else (vault(i).data.keySet - OldPw - Id).toSeq
          val edited = userInput(fieldsToEdit, default) + (Id -> id)
          val (enteredPw, existingPw) = (edited.getOrElse(Pw, ""), vault(i).get(Pw))
          val appendOldPwMap: Map[String, String] =
            if enteredPw != existingPw then Map(OldPw -> existingPw) else Map()
          vault(i) = Secret(vault(i).data ++ edited ++ appendOldPwMap)
          Terminal.put(s"\nEdited record with id:$id")
          listRecords(i.toString, isShowAll = false)
        else notifyRecordNotFound()

      case _ => Terminal.put(s"Too many arguments: $arg")

  def copyNewPasswordToClipboard(): Unit = copyToClipboardAndNotify(generatePassword())

  def listRecords(arg: String, isShowAll: Boolean): Unit =
    val fieldsToExclude = if isShowAll then Seq() else SecretFields
    splitArg(arg) match
      case Seq() => listRange(0, vault.size, fieldsToExclude)
      case Seq(ix) if isInt(ix) => showRecordByIndex(ix.toInt, fieldsToExclude)
      case Seq(id) => showRecordById(id, fieldsToExclude)
      case Seq(ix1, ix2) if isInt(ix1) && isInt(ix2) =>
        val last = vault.size - 1
        val (a, b) = (ix1.toInt min last max 0, ix2.toInt min last max 0)
        listRange(fromIndex = a, untilIndex = b + 1, fieldsToExclude)
      case _ => Terminal.put(s"too many arguments: $arg")

  def copyRecord(arg: String): Unit =
    splitArg(arg) match
      case Seq() => Terminal.put(s"Give index or id as argument!")

      case args if args.size <= 2 =>
        val fieldToCopy = args.lift(1).getOrElse(Pw)
        val i = if isInt(args.head) then args.head.toInt
                else vault.indexStartsWith(field = Id, valueStartsWith = args.head)
        if i >= 0 && i < vault.size then copyToClipboardAndNotify(vault(i).get(fieldToCopy))
        else notifyRecordNotFound()

      case _ => Terminal.put(s"Too many arguments: $arg")

  def exportAllToClipboard(): Unit =
    Clipboard.put(showAllRecordsAndFields)
    Terminal.put(s"${vault.size} records copied to clipboard.")

  def checkForDuplicates(fields: Seq[Secret] ): Seq[Secret] =
    val newIds = fields.toSet[Secret].map(s => s.get(Id))
    val existingIds = vault.toSet.map(s => s.get(Id))
    val duplicates = newIds intersect existingIds
    if duplicates.nonEmpty then
      Terminal.put("\n *** WARNING! Duplicate ids detected: " + duplicates.mkString(", "))
      if Terminal.isOk("Do you want to remove all these ids in vault before importing?") then
        vault.removeValuesOfField(duplicates.toSeq, Id)
      else Terminal.put("Duplicates kept in vault.")
    val pairs = fields.map(s => (s.get(Id), s))
    val distinctPairs = pairs.toMap.toSeq
    if pairs.size != distinctPairs.size then
      if Terminal.isOk("Duplicates among import detected. Keep last in sequence?") then
        distinctPairs.map(_._2)
      else fields
    else fields


  def importFromClipboard(): Unit =
    val items = Clipboard.get().split("\n\n").toSeq
    val fields = items.filterNot(_.isEmpty).flatMap(parseFields)
    val n = fields.size
    Terminal.put(fields.map(_.get("id")).mkString(", "))
    if Terminal.isOk(s"Do you want to append the $n records to your vault?") then
      val fieldsToAppend = checkForDuplicates(fields)
      vault.add(fieldsToAppend:_*)
      setCompletions()

  def checkForUpdateAndInstall(): Unit =
    if Main.latestVersion.nonEmpty then
      if Main.latestVersion != Main.version then
        if Terminal.isOk(s"Version ${Main.latestVersion} is available. Download and install?") then
          Main.install()
        else Terminal.put("Installation aborted.")
      else Terminal.put(s"Already up to date! Current version of keehive is ${Main.version}")
    else Terminal.put("No information on latest version available.")
