package keehive

// object TerminalOld {
//   final val reader = new jline.console.ConsoleReader
//   reader.setExpandEvents(false)  //to avoid crash on '!' : https://github.com/jline/jline2/issues/96

//   final val CtrlD = "\u0004"  // End Of Transmission

//   private def replaceNull(s: String): String = if (s == null) CtrlD else s

//   //jline-2.14.3 is required for default values to work
//   def get(prompt: String = "", default: String = ""): String =
//     replaceNull(reader.readLine(prompt, null, default))

//   def getSecret(prompt: String = "Enter secret: "): String =
//     replaceNull(reader.readLine(prompt,'*'))

//   def isOk(msg: String = ""): Boolean = get(s"$msg (Y/n): ") == "Y"

//   def put(s: String): Unit = reader.println(s)

//   def removeCompletions(): Unit = {
//     reader.getCompleters.toArray.foreach { c =>
//       reader.removeCompleter(c.asInstanceOf[jline.console.completer.Completer])
//     }
//   }

//   def setCompletions(first: Seq[String], second: Seq[String]): Boolean = {
//     removeCompletions()
//     val sc1 = new jline.console.completer.StringsCompleter(first: _*)
//     val sc2 = new jline.console.completer.StringsCompleter(second: _*)
//     val ac = new jline.console.completer.ArgumentCompleter(sc1, sc2)
//     reader.addCompleter(ac)
//   }
// }

object Terminal:
    //https://github.com/jline/jline3/wiki
    //https://search.maven.org/artifact/org.jline/jline
  import org.jline.terminal.TerminalBuilder
  import org.jline.reader.LineReaderBuilder
  import org.jline.reader.impl.completer.{ArgumentCompleter, StringsCompleter}
  import org.jline.reader.impl.LineReaderImpl
  final val CtrlD = "\u0004"  // End Of Transmission

  val terminal = TerminalBuilder.terminal // builder.system(true).build
  val reader = LineReaderBuilder.builder
    .terminal(terminal)
    .build
    .asInstanceOf[LineReaderImpl] //cast hack to expose set/getCompleter

  def get(prompt: String = "", default: String = ""): String =
    util.Try(reader.readLine(prompt, null: Character, default)).getOrElse(CtrlD)

  def getSecret(prompt: String = "Enter secret: ", mask: Char = '*'): String = 
    util.Try(reader.readLine(prompt, mask)).getOrElse(CtrlD)

  def isOk(msg: String = ""): Boolean = get(s"$msg (Y/n): ") == "Y"
  
  def put(s: String): Unit = terminal.writer().println(s)

  def removeCompletions(): Unit = reader.setCompleter(null)
  
  def setCompletions(first: Seq[String], second: Seq[String]): Boolean =
    removeCompletions()
    val sc1 = new StringsCompleter(first: _*)
    val sc2 = new StringsCompleter(second: _*)
    val ac = new ArgumentCompleter(sc1, sc2)
    reader.setCompleter(ac)
    
    true  // to be compatible with old readline which used to return if ok

