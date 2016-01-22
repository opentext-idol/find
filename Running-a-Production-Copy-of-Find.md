# Getting Started

So you've made some changes to Find and want to deploy them.  This is the page for you!

You need to understand that before you **start** the Find webapp, you need to choose whether you want to run it against HPE IDOL or Haven OnDemand.

# Building Find

Follow the steps on the [[Building Find]] guide to compile your copy of the application.  Choose one of the output files mentioned at the end of the guide - either `find-hod.war` for Haven OnDemand, or `find.war` for IDOL.  Take a copy of the `.war` file - this is the application that you're going to run

When building, you should specify the Maven `production` profile with `-Pproduction` to run the JavaScript and CSS optimizations.

# Run the application
## Easy way

From a command prompt, run:

`java -jar find.war -Dhp.find.home=<path_to_find_home_directory>`

## Better way

TODO document how to use the startup scripts that we've written.

# Step Three: Open Find in your web browser

- Navigate to http://localhost:8080 and you should see Find.  A config file has been generated in the Find home directory that you specified on the command line.

You now want to read either [[Configuring Find for IDOL]] or [[Configuring Find for Haven OnDemand]] to get Find configured.