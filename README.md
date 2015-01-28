# HP Find
HP Find is a web application backed by [IDOL OnDemand](https://www.idolondemand.com)

A live preview of HP Find can be found at [find.idolondemand.com](http://find.idolondemand.com).

## Key Features
* Querying IDOL OnDemand indexes
* Viewing IDOL OnDemand results
* Suggested related searches from IDOL OnDemand

## Building HP Find
Building HP Find requires the following to be installed

* [Apache Maven 3](http://maven.apache.org)
* [NodeJS](http://nodejs.org)
* [bower](http://bower.io)
* [r.js](http://requirejs.org/docs/optimization.html)
* [blessc](http://blesscss.com/)

The jetty:run goal will stand up a local web server for development. The package goal will build a war file. Running with
the production profile will minify the Javascript and CSS, and bless the CSS for older versions of Internet Explorer.

When developing the develop profile should be used. You will need to create a copy of src/main/filters/filter-dev.properties.example in the same directory.
This should be named filter-dev.properties. This file should be ignored by git.

## HP Find setup
You'll need to install [Tomcat](http://tomcat.apache.org) to run the HP Find war file.

HP Find requires some Java system properties to be set in order to work.
On Linux, one way to do this is by modifying JAVA_OPTS in /etc/default/tomcat7.
On Windows, this can be done with the Tomcat Manager (if installed), or by modifying the JAVA_OPTS environment variable.
If using the jetty:run goal, the properties can be set on the command line
The properties you'll need to set are:

* -Dhp.find.home . This is the directory where the webapp will store log files and the config.json file.
* -Dfind.https.proxyHost . Optional property. The host for the https proxy. Set this if you need a proxy server to talk to IDOL OnDemand.
* -Dfind.https.proxyPort . Optional property. The port for the https proxy. Set this if you need a proxy server to talk to IDOL OnDemand. Defaults to 80 if find.https.proxyHost is defined.

## Configuring HP Find
Once you've started HP Find, you'll need to configure HP Find. When run for the first time, a login screen will appear. The credentials for this are in the config file.

This will take you to the Settings Page. You'll need an IOD API key. Once provided, you can configure which IOD indexes you wish to allow searching against.

You can also configure a user to allow these settings to be changed later. The current password is the password you used to login.
The new password will be stored in the config file as a BCrypt hash.

Once you've configured HP Find, save the config file with the save changes button, and logout. You will be redirected to the Search page. You can access the settings page again by pointing your browser at /find/login (there is no link to this in the UI).

## Is it any good?
Yes.

## License
Copyright 2014-2015 Hewlett-Packard Development Company, L.P.

Licensed under the MIT License (the "License"); you may not use this project except in compliance with the License.