#!/bin/bash

echo -n "Enter new version number: "
read vers

echo "$vers" | cat >version.txt
sed -i -- "s/version = \"[0-9]\.[0-9]\"/version = \"$vers\"/g" src/main/scala/keehive/Main.scala
sed -i -- "s/versnum = \"[0-9]\.[0-9]\"/versnum = \"$vers\"/g" build.sbt
sed -i -- "s/keehive-[0-9]\.[0-9]\.jar/keehive-$vers.jar/g" run.sh

sbt assembly
mkdir -p bin
cp "target/scala-2.11/keehive-$vers.jar" bin/.
