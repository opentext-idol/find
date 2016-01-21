# Getting Started

So you've made some changes to Find and want to test them.  This is the page for you!

You need to understand that before you **start** the Find webapp, you need to choose whether you want to run it against HPE IDOL or Haven OnDemand.

For the purposes of this guide, we're assuming that you've already cloned Find from GitHub and installed all of the dependencies.  If you haven't, go and follow the steps on the [Building Find](https://github.com/hpautonomy/find/wiki/Building-Find) Wiki page.

## Step One: Compile and Install the modules

- `mvn clean install`

## Step Two: Use Spring Boot to run a local server

- `cd` into either `find-hod` or `find-idol`, depending on whether you want to run Find against Haven OnDemand or IDOL.
- Run `mvn spring-boot:run -Dhp.find.home=<path_to_find_home_directory>`

## Step Three: Open Find in your web browser

- Navigate to http://localhost:8080 and you should see Find.  A config file has been generated in the Find home directory that you specified on the command line.

You now want to read either [[Configuring Find for IDOL]] or [[Configuring Find for Haven OnDemand]] to get Find configured.