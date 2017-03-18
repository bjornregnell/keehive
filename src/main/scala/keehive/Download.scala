package keehive

object Download {
  def asString(url: String): String = scala.io.Source.fromURL(url).getLines.mkString("\n")

  def toTextFile(url: String, fileName: String, enc: String = "UTF-8"): Unit = {
    val s = asString(url)
    val file = new java.io.File(fileName)
    val pw = new java.io.PrintWriter(file, enc)
    try pw.write(s) finally pw.close
  }

  def printBytesWritten(n: Long): Unit = {
    val s = s"$n bytes written"
    print(s + {"\b" * s.length})
  }

  def toBinaryFile(url: String, fileName: String, onWrite: Long => Unit = printBytesWritten, bufSize: Int = 4096): Unit = {
    val file = new java.io.File(fileName)
    val output = new java.io.FileOutputStream(file)
    try {
      val connection = new java.net.URL(url).openConnection
      val input = connection.getInputStream
      val buffer = new Array[Byte](bufSize)
      var n: Int = input.read(buffer)
      var tot: Long = n
      while (n != -1){
          output.write(buffer, 0, n)
          onWrite(tot)
          n = input.read(buffer)
          tot = tot + n
      }
    } finally output.close
  }
}
