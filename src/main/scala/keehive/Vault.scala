package keehive

object Vault:
  case class Master(salt: String = "", saltedHash: String = "")

  def saveMasterPassword(file: String, mpw: String): String =
    val salt = Crypto.Salt.next
    val m = Master(salt, Crypto.SHA.hash(mpw + salt))
    val encrypted = Crypto.AES.encryptObjectToString(m, mpw)
    Disk.saveString(encrypted, file)
    salt

  case class MasterCheck(isValid: Boolean, isCreated: Boolean, salt: String = "")

  def checkMasterPassword(file: String, mpw: String): MasterCheck =
    if Disk.isExisting(file) then
      val encrypted = Disk.loadString(file)
      val Master(salt, saltedHash) =
        Crypto.AES.decryptObjectFromString[Master](encrypted, mpw).getOrElse(Master())
      if Crypto.SHA.hash(mpw + salt) == saltedHash then
        MasterCheck(isValid = true, isCreated = false, salt)
      else MasterCheck(isValid = false, isCreated = false)
    else
      Disk.createFileIfNotExist(file)
      val salt = saveMasterPassword(file, mpw)
      MasterCheck(isValid = true, isCreated = true, salt)

  case class Result(valtOpt: Option[Vault], isCreated: Boolean)

  def open(user: String, masterPassword: String, path: String): Result =
    val mpwFile   = s"$path/$user-mpw.txt"
    val vaultFile = s"$path/$user-vlt.txt"
    Terminal.put(s"Master password file: $mpwFile")
    Terminal.put(s"          Vault file: $vaultFile")
    val MasterCheck(isValid, isCreated, salt) =
      checkMasterPassword(mpwFile, masterPassword)
    if isValid then
      val vault = new Vault(masterPassword, mpwFile, vaultFile, salt)
      Result(Some(vault), isCreated)
    else Result(None, isCreated = false)

class Vault private (
        initMasterPassword: String,
        masterPasswordFile: String,  // TODO used when impl. change mpw
        vaultFile:          String,
        private var salt:   String):

  import scala.collection.mutable.ArrayBuffer

  type Secrets = ArrayBuffer[Secret]
  type Data = ArrayBuffer[(Map[String,String], Long)]

  private var mpw = initMasterPassword  // TODO change master password
  private val secrets: Secrets = loadSecrets()
  private def key = mpw + salt

  private def saveSecrets(ss: Secrets): Unit =
    val data: Data = ss.map(s => (s.data, s.timestamp))
    val encrypted = Crypto.AES.encryptObjectToString(data, key)
    Terminal.put(s"Saving ${ss.size} secrets in vault.")
    Disk.saveString(encrypted, vaultFile)

  private def loadSecrets(): Secrets =
    if Disk.isExisting(vaultFile) then
      val encrypted = Disk.loadString(vaultFile)
      val dataOpt: Option[Data] = Crypto.AES.decryptObjectFromString(encrypted, key)
      if dataOpt.isEmpty then
        Main.abort("Inconsistency between master password and encrypted vault!")
      val secretsOpt: Option[Secrets] = dataOpt.map(p => p.map{case (d,t) => Secret(d, t)})
      Terminal.put(s"Loaded ${secretsOpt.get.size} secrets.")
      secretsOpt.get
    else
      Terminal.put("No vault found. Empty vault will be created after verification.")
      AppController.abortIfUnableToVerifyMasterPassword()
      val emptySecrets: Secrets = ArrayBuffer.empty
      Terminal.put(s"Creating new empty vault.")
      saveSecrets(emptySecrets)
      emptySecrets

  def toVector: Vector[Secret] = secrets.toVector
  def toSet: Set[Secret] = secrets.toSet
  def apply(index: Int): Secret = secrets(index)
  def size: Int = secrets.size
  def valuesOf(field: String): Seq[String] = secrets.map(s => s.get(field)).toSeq
  def isExisting(field: String, value: String): Boolean = valuesOf(field) contains value

  def indexOf(field: String, value: String): Int = valuesOf(field) indexOf value
  def indexStartsWith(field: String, valueStartsWith: String): Int =
    valuesOf(field).indexWhere(_.startsWith(valueStartsWith))
  def indexWhere(field: String, value: String): Int =
    valuesOf(field).indexWhere(_ == value)

  def add(xs: Secret*): Int =
    xs.foreach(s => secrets.append(s))
    saveSecrets(secrets)
    secrets.size

  def update(index: Int, newSecret: Secret): Unit =
    secrets(index) = newSecret
    saveSecrets(secrets)

  def remove(index: Int, n: Int = 1): Unit =
    secrets.remove(index, n)
    saveSecrets(secrets)

  def removeValuesOfField(values: Seq[String], field: String): Unit =
    values.foreach{ v =>
      var i = indexWhere(field, v)
      while i >= 0 do
        secrets.remove(i)
        i = indexWhere(field, v)
    }
    saveSecrets(secrets)

