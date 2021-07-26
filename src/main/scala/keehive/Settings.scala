package keehive

object Settings:
  import scala.util.Try

  val fileName = Main.path +"/settings.txt"
  val default: Map[String, String] = Map(
    "defaultUser"  -> System.getProperty("user.name"),
    "defaultEmail" -> "",
    "generatePasswordChars"  -> "0-9 A-Z a-z !,.%",
    "generatePasswordLength" -> "20"
  )

  private var settings: Map[String, String] = default

  def toMap: Map[String, String] = settings
  def apply(key: String): Option[String] = settings.get(key)
  def update(key: String, value: String): Unit =
    settings = settings.updated(key,value)
    val _ = save()

  def asInt(key: String): Option[Int] = Try { settings(key).toInt } .toOption

  override def toString: String = settings.map{ case (k, v) => s"$k=$v"}.mkString("\n")

  def save() = Try {
    Disk.saveString(toString, fileName)
    Terminal.put(s"Settings saved to file: $fileName")
  } recover { case e => println(s"Error when saving settings: $e") }

  def load() =
    Try {
      if !Disk.isExisting(fileName) then
        Terminal.put(s"No settings file found: $fileName")
        if !Disk.isExisting(Main.path) then
          Terminal.put(s"No keehive directory found: ${Main.path}")
          Disk.createDirIfNotExist(Main.path)
          Terminal.put(s"Directory created: ${Main.path}")
        save()
      else
      Terminal.put(s"Loading settings from file: $fileName")
      val lines = Disk.loadLines(fileName).filter(_.nonEmpty)
      val mappings: Seq[Seq[String]] = lines.map(_.split('=').toSeq)
      mappings.collect {
        case Seq(key, value) => settings = settings.updated(key.trim, value.trim)
      }
      Terminal.put(s"Loaded settings: $settings")
    } recover { case e => Terminal.put(s"Error when loading settings: $e") }

