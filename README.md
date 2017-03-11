# keehive

## What is keehive?

* keehive is terminal password manager that can...

  - find and copy your hidden password so that you can paste it with Ctrl+V as you login
  - store your passwords and related information (user id, site url, etc) in an  encrypted file using [AES encryption](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)
  - create, list, edit, delete your passwords through an effective Command Line Interface with TAB-completion and line editing powered by [jline](https://jline.github.io/jline2)


* keehive is open source software created in [Scala 2.11.8](www.scala-lang.org) using  encryption utilities in Java JDK8. The code-base is small (less than thousand source lines in one package in less than ten files) and is written to be understandable to anyone with basic programming skills in Scala. The code is kept simple without advanced Scala features, such as implicits and type-level programming.

* Contributions are welcome! See below under 'How to contribute to keehive?'

## How to run keehive?

* You need `java` on your path; if you don't have it then first [install JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

* Download the [latest released jar-file](https://github.com/bjornregnell/keehive/releases/)

* Open a terminal and run the jar-file using this comman (replace x.y with version number):

```
java -jar keehive-x.y.jar
```

## How to use keehive?

```
keehive> help

Press TAB for completion of these commands (alphabetical order):

add       add a new record, enter each field after prompt
add id    add a new record with id, enter each field after prompt

copy s    copy password of record with id starting with s
          example: c<TAB> s<TAB>      copy password of id starting with s
copy s f  copy field f of record with id starting with s
          example: c<TAB> myid url    copy the url field of id:myid

del 42    delete the record at index 42
del id    delete the record with id

edit 42   edit the record at index 42
edit id   edit the record with id
edit i f  edit/add the field f of record with id/index i

help      show this message; also ?

list      list all records, hide password
list 42   list fileds of record with index 42, hide password
list s    list fields of record with id that starts with s, hide password

quit      quit keehive; also Ctrl+D

show      list all records, show password
show 42   list fileds of record with index 42, show password
show s    list fields of record with id that starts with s, show password

xport     export all records to clipboard as plain tex
```


## How to build?

* The easy way to build keehive is to use `sbt`; if you don't have it then first [install sbt]()
  - to manually compile the files, then you need `scalac` on your path (but if you install sbt this is not needed) [download and install Scala 2.11.8](http://scala-lang.org/download/2.11.8.html)

* You need `git` on your path to clone this repo; if you don't have it then first [install git](). Otherwise just download the repo as a [zip-file](https://github.com/bjornregnell/keehive/archive/master.zip) and unpack.

Do this to build keehive:

1. Clone this repo: `git clone git@github.com:bjornregnell/keehive.git`

2. Build the jar-file: `sbt assembly`

3. Run the jar-file: `source run.sh`

As keehive uses a more recent version of jline than sbt, it does not work to start the app with `sbt run`, which will give the error below; therefor launch keehive using `source run.sh` or `java -jar target/scala-2.11/keehive-x.y.jar` instead.
```
[error] (run-main-0) java.lang.NoSuchMethodError: jline.console.ConsoleReader.readLine(Ljava/lang/String;Ljava/lang/Character;Ljava/lang/String;)Ljava/lang/String;
java.lang.NoSuchMethodError: jline.console.ConsoleReader.readLine(Ljava/lang/String;Ljava/lang/Character;Ljava/lang/String;)Ljava/lang/String;
	at keehive.Terminal$.get(Terminal.scala:11)
	at keehive.AppController$.cmdLoop(AppController.scala:71)
	at keehive.AppController$.start(AppController.scala:65)
	at keehive.Main$.main(Main.scala:11)
	at keehive.Main.main(Main.scala)

```

## How to contribute to keehive?

### Fork and clone

* Learn the basics about git, especially the "Getting Started" and "Git Basics" sections in this book: https://git-scm.com/book/en/v2

* Get an account at github if you don't have one already. Recommended user name if in doubt: `firstnamefamilyname` with no capital letters and no hyphens.

* Install git: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git

* Make a **fork** of this repo in GitHub to your own GitHub account: https://help.github.com/articles/fork-a-repo/

* **Clone** your fork to your local computer: https://help.github.com/articles/cloning-a-repository/

### Keep your fork in synch

* This is how to pull changes from upstream to your fork with git commands in the terminal: https://help.github.com/articles/syncing-a-fork/

* If you install a git GUI client, you can keep your fork in synch with the upstream repo by a single click in the GUI:
 - For Linux, Windows and MacOS: https://www.gitkraken.com/
 - For Windows and MacOS: https://desktop.github.com/

 * Before you change locally, make sure your fork is in synch (see above). Frequently do `git pull` or synch using a git GUI client.


### Making contributions

* If you find a minor issue that is straight-forward to fix you are very welcome to create a pull request directly as explained below. But if your contribution is more significant you should open an issue first and start a discussion about your proposal. In the latter case, click the issue tab at the top of this page.

* You must check that your contribution compile and run without errors before you commit.

* Whenever you are ready with an incremental change, do `git commit -m "msg"` and then `git push`, or commit and synch in a git GUI client. Think *carefully* about your commit message, as discussed in the next section.

* When you are ready with a complete, atomic contribution that is good enough to be incorporated in upstream, then create a pull request: https://help.github.com/articles/creating-a-pull-request/

* Keep your pull requests minimal and coherent to create a small change sets that will be easy to merge as a single unit. Don't pack a lot of unrelated changes in the same pull request.


### Writing commit messages

* Write concise and informative [commit messages](http://chris.beams.io/posts/git-commit/) that explains why the commit was made.

* Start each commit message with a direct verb, preferably one of the following:
  * `add` when you have created new stuff that was not there before
  * `update` when you have changed existing stuff
  * `fix` when you have corrected a bug or fixed a typo etc.
  * `remove` when you have removed stuff
  * `rename` when you have renamed files or other stuff without changing appearance/meaning
  * `refactor` when you have changed things structurally but not changed actual appearance/meaning

* Make small commits and commit often. Try to keep commits atomic and only within one file if meaningful.

* Make sure your change compiles before committing. Do *not* push code that does not compile!


### Coding style

Pragmatically follow these style guides:

* Scala style:
  * The Scala style guide: http://docs.scala-lang.org/style/
  * A Scala best practice guide: https://github.com/alexandru/scala-best-practices
