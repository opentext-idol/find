---
layout: default
level: basic
title: Building Find
---

Requirements
------------

Building Find requires the following to be installed:

* [Git](https://git-scm.com/) | [Alternative for Windows only](https://git-for-windows.github.io/)
* [Apache Maven 3](http://maven.apache.org) (Hint: make sure the `mvn` executable is in your `PATH` environment variable)
* [NodeJS](http://nodejs.org)
* [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

Get the code
------------

First, we use Git to clone Find from GitHub.  From your command line (hint: Git Bash is better than the Windows Command Prompt), run:

`git clone https://github.com/hpe-idol/find.git`

By default, this will create a folder called `find` with the `master` branch checked out. Inside this folder will be a webapp directory containing the Maven project.

Change your current directory to be the new `webapp` folder:

`cd find/webapp`

Store the Git Commit Hash
-------------------------

Find uses the Git commit hash as a "cache-buster" - all static files (CSS, JavaScript, HTML templates, etc) are "stamped" with the Git Commit hash as part of the build process.  This means that with every new build, all the file names (apart from index.html - the entry point to the application) change.  This is to make sure that browsers don't use old, cached versions of the code.

We need to get the Git commit hash and pass it to Maven.

`git rev-parse --short HEAD` will give you the hash of the latest commit on your current branch.

From Bash, run the following command:

    GIT_COMMIT=`git rev-parse --short HEAD`

You can check that this worked by running `echo $GIT_COMMIT` - this should print out the hash.


Build the Application
---------------------

Run the following command:

    mvn package -pl <module> -am -Dapplication.buildNumber=$GIT_COMMIT

where <module> is either "idol" or "hod" depending on the version of Find you're interested in.

(`$GIT_COMMIT` assumes that you followed the steps in the "Store the Git Commit Hash" section and chose to call your variable `GIT_COMMIT`)

You should see lots of scrolling text, followed by a large `BUILD SUCCESS` message.  This might take a few minutes!

If this doesn't work, check that you've installed everything correctly:
- `mvn -version` should give you a version higher than 3 (e.g. "3.2.3")
- `java -version` should give you a version higher than 1.8.0 (e.g. "1.8.0_65")
- `node -v` should give you a version higher than 0.12 (e.g. "v0.12.0")

Output Files
------------

- `hod/target/find-hod.war` - this is a build of Find with the Haven OnDemand modules included.  Use this if you want to run Find against Haven OnDemand
- `idol/target/find.war` - this is a build of Find with the HPE IDOL modules included.  Use this if you want to run Find against HPE IDOL (note: this only works with IDOL 10 - IDOL 7 is **not supported**).
- `on-prem-dist/target/find-*.zip` - this is a zip archive containing the IDOL build of Find (find.war) and some scripts for running Find as a service.  You probably don't want to use this - just ignore it.

Build Profiles
--------------

To optimize the JavaScript and CSS resources, run Maven with the `production` profile, like so:

`mvn package -Pproduction`