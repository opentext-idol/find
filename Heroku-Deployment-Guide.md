# Heroku Deployment Guide

This guide assumes that you have experience of building Java applications with maven and deploying to Heroku. For more information, see https://devcenter.heroku.com/articles/getting-started-with-java.

## Create the application on Heroku
Before you continue, you must download and install the Heroku toolbelt from https://toolbelt.heroku.com/.

After the installer has completed, log in with the credentials that you used when you created your Heroku account (currently on Windows you must do this from cmd.exe rather than the git bash terminal).

After you have authenticated, navigate to the directory where you cloned the Find repository, and create the Heroku application by using the following terminal command:

```bash
$ heroku create
Creating young-mesa-1439... done, stack is cedar-14
https://young-mesa-1439.herokuapp.com/ | https://git.heroku.com/young-mesa-1439.git
Git remote heroku added
```
The output is the name of the application in the Heroku system (in this case, ```young-mesa-1439```) and the address of the git repository created for the project. Take a note of this information, because it is required later.

## Configure the build
Because Find uses both Java and NodeJS modules, specify that Heroku must use the appropriate buildpacks by running the following terminal commands:

```bash
$ heroku buildpacks:add heroku/nodejs
$ heroku buildpacks:add heroku/java
```
Run the following command to ensure that both buildpacks have been set:

```bash
$ heroku buildpacks
=== young-mesa-1439 Buildpack URLs
1. heroku/nodejs
2. heroku/java
```
You want to build only the Find maven modules required in a hosted environment, so you should alter the maven targets to build only ```hod``` and its dependencies by using the following configuration commands:

```bash
$ heroku config:set MAVEN_CUSTOM_GOALS="clean install -pl hod -am"
```
This command tells the Java buildpack to run only the ```clean``` and ```install``` maven targets on the ```hod``` module and its dependencies.

## Configure Find
Find requires you to specify the configuration file location at the command line as a system property. Heroku does not allow persistent storage on its file system, so you must keep the configuration file under the root of the application source. See the Find documentation for more information on how to configure the application.

Now you need the application name: in the configuration file, alter the ```allowedOrigins``` setting to include the Heroku host (in this case, ```http://young-mesa-1439.herokuapp.com```). If you do not specify this setting, the SSO process does not redirect you to the Find home page, and the login attempt fails.

```json
"allowedOrigins": ["http://young-mesa-1439.herokuapp.com"]
```
## Run Find
To run find on Heroku, you need a Procfile. This is a file which tells Heroku how to run the application. For more information, see https://devcenter.heroku.com/articles/procfile.

The Procfile for Find needs to include only the following line:

```
web: java -Dserver.port=${PORT} -Dhp.find.home=. -Dfind.iod.api=https://api.havenondemand.com -Dfind.hod.sso=https://dev.havenondemand.com/sso.html -Dhp.find.persistentState=INMEMORY -jar hod/target/find-hod.war
```
You must set ```-Dserver.port=$(PORT)``` to specify that Spring-Boot should run on the port that Heroku is providing via ```${PORT}```. Currently the persistence is set to *INMEMORY*, because Redis is not currently tested on Heroku.

## Deploy to Heroku
After you have made all your configuration changes, push the code to the remote Heroku repository by using the standard git commands:

```bash
$ git push heroku master
```
This can take several minutes to complete. After the process has completed,run the following command:

```bash
$ heroku open
```
This opens the application root in your default browser (you must add ```/find``` to see the login page).
