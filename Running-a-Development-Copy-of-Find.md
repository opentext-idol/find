# Getting Started

So you've made some changes to Find and want to test them.  This is the page for you!

You need to understand that before you **start** the Find webapp, you need to choose whether you want to run it against HPE IDOL or Haven OnDemand.

For the purposes of this guide, we're assuming that you've already cloned Find from GitHub and installed all of the dependencies.  If you haven't, go and follow the steps on the [Building Find](https://github.com/hpautonomy/find/wiki/Building-Find) Wiki page.

## Step One: Compile and Install the modules

- `mvn install`

It's important to run the `install` step, as this "installs" the modules to your local Maven repository.  This makes the `core` module available for `idol` and `hod` to use as a dependency.

## Step Two: Use Spring Boot to run a local server

- `cd` into either `hod` or `idol`, depending on whether you want to run Find against Haven OnDemand or IDOL.
- Run `mvn spring-boot:run -Dhp.find.home=<path_to_find_home_directory>`.  You will need to create a home directory for Find - have a look at [[Home-directory]].

Any changes you make to Java files will be recompiled and redeployed automatically.

## Step Three: Use Grunt to keep HTML, CSS, and JavaScript files up to date

From your `hod` or `idol` folder (as appropriate), run the following command:

- `grunt copy-resources`

This will take any changes you make to static files (HTML, CSS, JavaScript, images, etc) in `core` and deploy them into your running Spring Boot server from step two.

## Step Four: Open Find in your web browser

- Navigate to http://localhost:8080 and you should see Find.  A config file has been generated in the Find home directory that you specified on the command line in step two.

Any code changes you make will appear after a short delay (should be less than a couple of seconds) - refresh your browser to pick up client side changes.

You now want to read either [[Configuring Find for IDOL]] or [[Configuring Find for Haven OnDemand]] to get Find configured.