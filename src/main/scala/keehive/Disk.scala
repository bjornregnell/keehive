package keehive

object Disk:

  def isExisting(fileName: String): Boolean =
    new java.io.File(fileName).exists

  def createDirIfNotExist(dir: String): Boolean = new java.io.File(dir).mkdirs()

  def createFileIfNotExist(fileName: String): Boolean =
      val file = new java.io.File(fileName)
      var isCreated = false
      if !file.exists then
        file.getParentFile.mkdirs()
        isCreated = file.createNewFile()
      isCreated

  def userDir: String = System.getProperty("user.home")

  def saveObject[T](obj: T, fileName: String): Unit =
    val file = new java.io.File(fileName)
    val oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(file))
    try oos.writeObject(obj) finally oos.close()

  def saveString(s: String, fileName: String, enc: String = "UTF-8"): Unit =
    val file = new java.io.File(fileName)
    val pw = new java.io.PrintWriter(file, enc)
    try pw.write(s) finally pw.close()

  def saveLines(lines: Seq[String], fileName: String, enc: String = "UTF-8"): Unit =
    saveString(lines.mkString("\n"), fileName, enc)

  def loadObject[T](fileName: String): T =
    val file = new java.io.File(fileName)
    val ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))
    try { ois.readObject.asInstanceOf[T] } finally ois.close()

  def loadString(fileName: String, enc: String = "UTF-8"): String =
    scala.io.Source.fromFile(fileName, enc).getLines().mkString("\n")

  def loadLines(fileName: String, enc: String = "UTF-8"): Vector[String] =
    scala.io.Source.fromFile(fileName, enc).getLines().toVector
