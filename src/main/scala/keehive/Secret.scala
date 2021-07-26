package keehive

case class Secret (
    data: Map[String, String] = Map(),
    timestamp: Long = System.currentTimeMillis){

  def get(key: String): String = data.getOrElse(key, "")

  def updated(kv: (String, String)): Secret = new Secret(data.updated(kv._1, kv._2))

  lazy val maxKeyLength: Int = data.keys.map(_.length).max

  lazy val pad: Map[String, String] =
    data.map{ case (k,_) => (k, " " * (maxKeyLength - k.length)) }

  def select(firstKeys: Seq[String], excludeKeys: Seq[String]): Seq[(String, String)] = {
    val otherKeys = (data.keySet diff firstKeys.toSet).toSeq.sorted
    val selectedKeys = (firstKeys ++ otherKeys) diff excludeKeys
    val kvs = selectedKeys.map(k => (k, get(k)))
    kvs.filterNot(_._2.isEmpty)
  }

  def showLines(firstKeys: Seq[String], dontShow: Seq[String]): String = {
    val kvs = select(firstKeys, dontShow)
    kvs.map{ case (k,v)  => s"$k:${pad(k)} $v"}.mkString("\n")
  }

  def show(firstKeys: Seq[String], excludeKeys: Seq[String], padFirstTo: Int = 15 ): String = {
    val kvs = select(firstKeys, excludeKeys)
    val xs = kvs.map{ case (k,v)  => s"$k:$v" }
    val padded = if xs.nonEmpty then xs.updated(0, xs.head.padTo(padFirstTo, ' ')) else xs
    padded.mkString(" ")
  }

  def ageMillis: Long = System.currentTimeMillis - timestamp

  def ageDays: Int = (ageMillis / (1000 * 60 * 60 * 24)).toInt

  def resetTimestamp: Secret = new Secret(data, System.currentTimeMillis)
}

object Secret {
  def empty: Secret = new Secret()
}
