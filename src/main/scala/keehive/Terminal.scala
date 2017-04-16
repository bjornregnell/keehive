package keehive

object Terminal {
  val reader = new jline.console.ConsoleReader

  final val CtrlD = "\u0004"  // End Of Transmission

  private def replaceNull(s: String): String = if (s == null) CtrlD else s

  //jline-2.14.3 is required for default values to work
  def get(prompt: String = "", default: String = ""): String =
    replaceNull(reader.readLine(prompt, null, default))

  def getSecret(prompt: String = "Enter secret: "): String =
    replaceNull(reader.readLine(prompt,'*'))

  def isOk(msg: String = ""): Boolean = get(s"$msg (Y/n): ") == "Y"

  def put(s: String): Unit = reader.println(s)

  def removeCompletions(): Unit = {
    reader.getCompleters.toArray.foreach { c =>
      reader.removeCompleter(c.asInstanceOf[jline.console.completer.Completer])
    }
  }

  def setCompletions(first: Seq[String], second: Seq[String]): Boolean = {
    removeCompletions()
    val sc1 = new jline.console.completer.StringsCompleter(first: _*)
    val sc2 = new jline.console.completer.StringsCompleter(second: _*)
    val ac = new jline.console.completer.ArgumentCompleter(sc1, sc2)
    reader.addCompleter(ac)
  }
}
