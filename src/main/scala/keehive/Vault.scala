package keehive

object Vault {
  case class Master(salt: String = "", saltedHash: String = "")
  val username: String = System.getProperty("user.name")
  final val mpwFileName   = s"$username-mpw.txt"
  final val vaultFileName = s"$username-vlt.txt"

  def saveMasterPassword(file: String, mpw: String): String = {
    val salt = Crypto.Salt.next
    val m = Master(salt, Crypto.SHA.hash(mpw + salt))
    val encrypted = Crypto.AES.encryptObjectToString(m, mpw)
    Disk.saveString(encrypted, file)
    salt
  }

  case class MasterCheck(isValid: Boolean, isCreated: Boolean, salt: String = "")

  def checkMasterPassword(file: String, mpw: String): MasterCheck = {
    if (Disk.isExisting(file)) {
      val encrypted = Disk.loadString(file)
      val Master(salt, saltedHash) =
        Crypto.AES.decryptObjectFromString[Master](encrypted, mpw).getOrElse(Master())
      if (Crypto.SHA.hash(mpw + salt) == saltedHash)
        MasterCheck(isValid = true, isCreated = false, salt)
      else MasterCheck(isValid = false, isCreated = false)
    } else {
      Disk.createIfNotExist(file)
      val salt = saveMasterPassword(file, mpw)
      MasterCheck(isValid = true, isCreated = true, salt)
    }
  }

  case class Result(valtOpt: Option[Vault], isCreated: Boolean)

  def open(masterPassword: String, path: String): Result = {
    val mpwFile   = s"$path/$mpwFileName"
    val vaultFile = s"$path/$vaultFileName"
    val MasterCheck(isValid, isCreated, salt) =
      checkMasterPassword(mpwFile, masterPassword)
    if (isValid) {
      val vault = new Vault(masterPassword, mpwFile, vaultFile, salt)
      Result(Some(vault), isCreated)
    } else Result(None, isCreated = false)
  }
}

class Vault private (
        initMasterPassword: String,
        masterPasswordFile: String,
        vaultFile:          String,
        private var salt:   String){

  import scala.collection.mutable.ArrayBuffer

  type Secrets = ArrayBuffer[Secret]

  private var mpw = initMasterPassword
  private var secrets: Secrets = loadSecrets()
  private def key = mpw + salt

  private def saveSecrets(ss: Secrets): Unit = {
    val encrypted = Crypto.AES.encryptObjectToString(ss, key)
    Terminal.put(s"Saving ${ss.size} secrets in vault.")
    Disk.saveString(encrypted, vaultFile)
  }

  private def loadSecrets(): Secrets = {
    if (Disk.isExisting(vaultFile)) {
      val encrypted = Disk.loadString(vaultFile)
      val secretsOpt: Option[Secrets] = Crypto.AES.decryptObjectFromString(encrypted, key)
      if (secretsOpt.isEmpty)
        Main.abort("Inconsistency between master password and encrypted vault!")
      Terminal.put(s"Loaded ${secretsOpt.get.size} secrets.")
      secretsOpt.get
    } else {
      val emptySecrets: Secrets = ArrayBuffer.empty
      Terminal.put(s"Creating new empty vault.")
      saveSecrets(emptySecrets)
      emptySecrets
    }
  }

  def toVector: Vector[Secret] = secrets.toVector
  def toSet: Set[Secret] = secrets.toSet
  def apply(index: Int): Secret = secrets(index)
  def size: Int = secrets.size
  def valuesOf(field: String): Seq[String] = secrets.map(s => s.get(field))
  def isExisting(field: String, value: String): Boolean = valuesOf(field) contains value

  def indexOf(field: String, value: String): Int = valuesOf(field) indexOf value
  def indexStartsWith(field: String, valueStartsWith: String): Int =
    valuesOf(field).indexWhere(_.startsWith(valueStartsWith))
  def indexWhere(field: String, value: String): Int =
    valuesOf(field).indexWhere(_ == value)

  def add(xs: Secret*): Int = {
    xs.foreach(s => secrets.append(s))
    saveSecrets(secrets)
    secrets.size
  }

  def update(index: Int, newSecret: Secret): Unit = {
    secrets(index) = newSecret
    saveSecrets(secrets)
  }

  def remove(index: Int, n: Int = 1): Unit = {
    secrets.remove(index, n)
    saveSecrets(secrets)
  }

  def removeValuesOfField(values: Seq[String], field: String): Unit = {
    values.foreach{ v =>
      var i = indexWhere(field, v)
      while (i >= 0) {
        secrets.remove(i)
        i = indexWhere(field, v)
      }
    }
    saveSecrets(secrets)
  }

}
