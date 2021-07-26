##!/bin/bash

#java -jar target/scala-2.13/keehive-0.6.jar "$@"
echo "Running keehive in test mode. Use password: asdf"
java -jar target/scala-2.13/keehive-0.6.jar -v test