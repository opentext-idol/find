# Requirements

Building Find requires the following to be installed:

* [Git](https://git-scm.com/) | [Alternative for Windows only](https://git-for-windows.github.io/)
* [Apache Maven 3](http://maven.apache.org) (Hint: make sure the `mvn` executable is in your `PATH` environment variable)
* [NodeJS](http://nodejs.org)
* [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

# Get the code

First, we use Git to clone Find from GitHub.  From your command line (hint: Git Bash is better than the Windows Command Prompt), run:

`git clone https://github.com/hpautonomy/find.git`

By default, this will create a folder called `find` with the `master` branch checked out.

Change your current directory to be the new `find` folder:

`cd find`

# Build the Application

Run the following command:

`mvn package`

You should see lots of scrolling text, followed by a large `BUILD SUCCESS` message.  This might take a few minutes!

If this doesn't work, check that you've installed everything correctly:
- `mvn -version` should give you a version higher than 3 (e.g. "3.2.3")
- `java -version` should give you a version higher than 1.8.0 (e.g. "1.8.0_65")
- `node -v` should give you a version higher than 0.12 (e.g. "v0.12.0")

# Output Files

- `find-hod/target/find-hod.war` - this is a build of Find with the Haven OnDemand modules included.  Use this if you want to run Find against Haven OnDemand
- `find-idol/target/find.war` - this is a build of Find with the HPE IDOL modules included.  Use this if you want to run Find against HPE IDOL (note: this only works with IDOL 10 - IDOL 7 is **not supported**).
- `find-dist/target/find-*.zip` - this is a zip archive containing the IDOL build of Find (find.war) and some scripts for running Find as a service.  You probably don't want to use this - just ignore it.

# Build Profiles

To optimize the JavaScript and CSS resources, run Maven with the `production` profile, like so:

`mvn package -Pproduction`