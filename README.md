# HP Find
[![Build Status](https://travis-ci.org/hpautonomy/find.svg?branch=master)](https://travis-ci.org/hpautonomy/find)

HP Find is a web application backed by [Haven OnDemand](https://www.idolondemand.com)

A live preview of HP Find can be found at [find.idolondemand.com](http://find.idolondemand.com).

## Key Features
* Querying Haven OnDemand indexes
* Viewing Haven OnDemand results
* Suggested related searches from Haven OnDemand

## Building HP Find
Building HP Find requires the following to be installed

* [Git](https://git-scm.com/)
* [Apache Maven 3](http://maven.apache.org)
* [NodeJS](http://nodejs.org)

[NPM](https://www.npmjs.com/) and [Bower](http://bower.io/) are used to manage dependencies. These are automatically
run in the maven process-sources phase.

The maven jetty:run goal will stand up a local web server for development. The package goal will build a war file.

Running with the production profile will minify the Javascript and CSS, and bless the CSS for older versions of Internet
Explorer.

When developing the develop profile should be used. You will need to create a copy of src/main/filters/filter-dev.properties.example in the same directory.
This should be named filter-dev.properties.

## HP Find setup
You'll need to install [Tomcat](http://tomcat.apache.org) to run the HP Find war file.

HP Find requires some Java system properties to be set in order to work.
On Linux, one way to do this is by modifying JAVA_OPTS in /etc/default/tomcat7.
On Windows, this can be done with the Tomcat Manager (if installed), or by modifying the JAVA_OPTS environment variable.
If using the jetty:run goal, the properties can be set on the command line
The properties you'll need to set are:

* -Dhp.find.home . This is the directory where the webapp will store log files and the config.json file.
* -Dhp.find.persistentState . Optional property. The persistence mode for the application, which determines where 
sessions, token proxies and caches are stored. Possible options are REDIS or INMEMORY. Defaults to INMEMORY.
* -Dfind.https.proxyHost . Optional property. The host for the https proxy. Set this if you need a proxy server to talk 
to Haven OnDemand.
* -Dfind.https.proxyPort . Optional property. The port for the https proxy. Set this if you need a proxy server to talk 
to Haven OnDemand. Defaults to 80 if find.https.proxyHost is defined.

## Vagrant
HP Find includes a Vagrant file, which will provision an Ubuntu 12.04 VM running a Redis server, which will by default 
be used to store sessions. 

The Vagrantfile requires several plugins, which will be installed if they are not installed already.

The VM has the IP address 192.168.242.242, and can be accessed via DNS with the name hp-find-backend.

The Redis runs on port 6379.

## Configuring HP Find
Earlier versions of Find had a settings page, but this is currently unavailable. To configure Find, create a config.json
file in your Find home directory.

Below is an example config file:

    {
        "login": {
            "method": "singleUser",
            "singleUser": {
                "username": "admin",
                "hashedPassword": "",
                "passwordRedacted" : false
            },
            "name": "SingleUserAuthentication"
        },
        "iod": {
            "apiKey": "YOUR API KEY",
            "application": "YOUR APPLICATION",
            "domain": "YOUR DOMAIN",
            "activeIndexes": [{
                "domain": "PUBLIC_INDEXES",
                "name": "wiki_eng"
            }]
        },
        "allowedOrigins": [
            "http://mydomain.example.com:8080"
        ],
        "redis": {
            "address": {
                "host": "hp-find-backend",
                "port": 6379
            },
            "database": 0,
            "sentinels": []
        }
    }

## Hard Coded fields
Find looks for the following fields in Idol/HoD documents:
* content_type
* url
* offset
* author
* category
* date_created or created_date
* date_modified or modified_date

If content_type is audio or video, the document is treated as an audio/video file using the url and offset fields

## Is it any good?
Yes.

## License
Copyright 2014-2015 Hewlett-Packard Development Company, L.P.

Licensed under the MIT License (the "License"); you may not use this project except in compliance with the License.
