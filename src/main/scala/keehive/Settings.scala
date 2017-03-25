package keehive

object Settings {
  lazy val fileName = Main.path +"/keehive.settings"
  lazy val default: Map[String, String] = Map(
    "defaultUser"  -> System.getProperty("user.name"),
    "defaultEmail" -> "",
    "generatePasswordChars"  -> "0-9 A-Z a-z *-.",
    "generatePasswordLength" -> "20"
  )

  private var settings: Map[String, String] = default

  override def toString: String = settings.map{ case (k, v) => s"$k=$v"}.mkString("\n")

  def save(): Unit = scala.util.Try {
    Disk.saveString(toString, fileName)
  } recover { case e => println(s"Error when saving settings: $e") }

  def load(): Unit = {
    Terminal.put(s"Loading settings from file: $fileName")
    scala.util.Try {
      if (!Disk.isExisting(fileName)) {
        Terminal.put(s"New settings file created.")
        save()
      }
      val lines = Disk.loadString(fileName).split('\n').filter(_.nonEmpty)
    } recover { case e => Terminal.put(s"Error when loading settings: $e") }
  }

}
