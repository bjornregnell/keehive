package keehive

object AppController {
  val welcomeBanner = s"""
    |***********************************************
    |***          WELCOME TO keehive!            ***
    |***  keehive is a terminal password manager ***
    |***    PROTECT YOUR KEYBOARD FROM PEEKERS!  ***
    |***           Type '?' for help             ***
    |***        Press TAB for completion         ***
    |***********************************************
    """.stripMargin

    def start(nonDefaultPath: String = ""): Unit  = {
      println(welcomeBanner)
    }
}
