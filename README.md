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


# How to build?

* You need `sbt` on your path; if you don't have it then first [install sbt]()
  - to manually compile the files, then you need `scalac` on your path (but if you install sbt this is not needed) [download and install Scala 2.11.8](http://scala-lang.org/download/2.11.8.html)

* You need `git` on your path to clone this repo; if you don't have it then first [install git](). Otherwise just download the repo as a [zip-file](https://github.com/bjornregnell/keehive/archive/master.zip) and unpack.

Do this to build keehive:

1. Clone this repo: `git clone git@github.com:bjornregnell/keehive.git`

2. Build the jar-file: `sbt assembly`

3. Run the jar-file: `source run.sh`
