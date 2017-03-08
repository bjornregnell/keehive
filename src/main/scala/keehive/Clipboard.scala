package sesam

object Clipboard {
  def put(s: String): Unit = {
    val stringSelection = new java.awt.datatransfer.StringSelection(s)
    val clipboard = java.awt.Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(stringSelection, null);
  }

  def get(): String = {
    val clipboard = java.awt.Toolkit.getDefaultToolkit.getSystemClipboard
    val data = clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor)
    data.asInstanceOf[String]
  }
}
