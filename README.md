# HPE Find - Fork by [Yuntaz](http://www.yuntaz.com)
[![Build Status](https://travis-ci.org/hpe-idol/find.svg?branch=master)](https://travis-ci.org/hpe-idol/find)
HPE Find is an end user search interface for [Haven OnDemand](https://www.havenondemand.com) and [HPE IDOL](http://www8.hp.com/uk/en/software-solutions/information-data-analytics-idol/index.html)
## How do I get it?
- The latest version of Find is used in [Haven Search OnDemand](https://search.havenondemand.com/)
- If you are an HPE IDOL Express or HPE IDOL Premium customer, Find is available to you in the HPE Big Data Download Center
## Documentation is on the Wiki
Questions about building, running, and configuring Find, plus many more subjects, are all covered on the HPE Idol [GitHub Wiki](https://github.com/hpe-idol/find/wiki)
## How to compile it?
### Prerequisites to build a version
You will need: 
- [GitHub](https://desktop.github.com/) - Use the GitShell, better control ;-)
- [Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) - JDK for your platform
- [Maven](https://maven.apache.org/download.cgi) - Use the last stable version
- [NodeJs 5.1](https://github.com/coreybutler/nvm-windows) - Install the NVM binaries instead the default NodeJs so you can manage versions.
### What to check?
#### Java
Java must be on the PATH, you must be able to run in any terminal
```sh
$ java -version
```
Also, don't forget to set the Environment variable JAVA_HOME
##### Linux
```sh
$ echo $JAVA_HOME
```
##### Windows
```sh
c:\>echo %JAVA_HOME% (Windows)
```
#### NodeJs
Download the NVM (Node version manager). Install it, and in a terminal, run
```sh
$ nvm install 5.1
$ node -v
```
It should be the 5.1 version on the system.
### Build recipe
After testing all the prerequisites, you will be able to build. 
Open a GitShell terminal and run the following recipe
```sh
$ git clone https://github.com/hpe-idol/find.git
$ cd find/webapps
$ mvn clean package -Pproduction -pl idol,hod -am
```
It will take some minutes, and you will get a JAR to use.
## Translation to Spanish
This version has all the Spanish version translations. 
## License
Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
Copyright 2015-2016 Hewlett Packard Enterprise Development LP
Licensed under the MIT License (the "License"); you may not use this project except in compliance with the License.
