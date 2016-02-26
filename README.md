# HPE BI for Human Information
[![Build Status](https://travis-ci.org/hpautonomy/find.svg?branch=master)](https://travis-ci.org/hpautonomy/find)

HPE BI for Human Information is a web application which is backed by HPE IDOL.

**Please note that BI for Human Information does not yet support integration with Haven OnDemand (HOD).**

## Key Features
* Querying HPE IDOL text indexes
* Viewing search results
* Suggested related searches

## Building HPE BI for Human Information
Building HPE BI for Human Information requires the following to be installed

* [Git](https://git-scm.com/)
* [Apache Maven 3](http://maven.apache.org)
* [NodeJS](http://nodejs.org)

Git and NPM must be on the PATH.

The project consists of four maven modules:

* **find-core** builds a jar file which is imported by the find-idol and find-hod modules, containing the core application
 components which are not specific to HOD or IDOL.
* **find-idol** builds an executable war file for running against IDOL.
* **find-hod** builds an executable war file for running against HOD.
* **find-dist** is responsible for packaging the IDOL artifacts into a zip file for distribution.

Running mvn install from the root of the project will build each module in turn. The build artifacts can be found in the
target directories of the modules.

The maven spring-boot:run goal can be used from either the find-idol or find-hod modules to stand up a web server for local
development. [Grunt](http://gruntjs.com/) tasks are provided in these modules for watching and refreshing static resources
without having to restart the server.

[NPM](https://www.npmjs.com/) and [Bower](http://bower.io/) are used to manage frontend dependencies. These are automatically
run in the maven process-sources phase.

During development, the develop profile should be used. Running with the production profile will minify the Javascript 
and CSS, and bless the CSS for older versions of Internet Explorer.

## HPE BI for Human Information setup
Separate executable war files are provided for running against IDOL or HOD. These can either be deployed on an instance
of [Tomcat](http://tomcat.apache.org) or run as an executable jar file, for example:

    java -Dhp.find.home=/opt/find -jar find.war

HPE BI for Human Information requires some Java system properties to be set in order to work.

* With Tomcat on Linux, you can modify JAVA_OPTS in /etc/default/tomcat7.
* With Tomcat on Windows, this can be done with the Tomcat Manager (if installed), or by modifying the JAVA_OPTS environment variable.
* When running from the command line or using the spring-boot:run goal, use the -D flag.

The properties you'll need to set are:

* **-Dhp.find.home** This is the directory where the webapp will store log files and the config.json file.
* **-Dhp.find.persistentState** Optional property. The persistence mode for the application, which determines where 
sessions, token proxies and caches are stored. Possible options are REDIS or INMEMORY. Defaults to INMEMORY.
* **-Dhp.find.databaseType** Optional property. The database implementation used to persist non-transient data in the application.
Possible options are H2PERSISTENT or H2INMEMORY (see http://www.h2database.com). Defaults to H2PERSISTENT.

When running against HOD, you may also need:

* **-Dfind.https.proxyHost** Optional property. The host for the https proxy. Set this if you need a proxy server to talk 
to Haven OnDemand.
* **-Dfind.https.proxyPort** Optional property. The port for the https proxy. Set this if you need a proxy server to talk 
to Haven OnDemand. Defaults to 80 if find.https.proxyHost is defined.

## Vagrant
HPE BI for Human Information includes a Vagrant file, which will provision an Ubuntu 12.04 VM running a Redis server, which will by default
be used to store sessions. 

The Vagrantfile requires several plugins, which will be installed if they are not installed already.

The VM has the IP address 192.168.242.242, and can be accessed via DNS with the name hp-find-backend.

The Redis runs on port 6379.

## Configuring HPE BI for Human Information
To configure BI for Human Information, create a config.json file in your BI for Human Information home directory.

Below is an example config file for running against HOD:

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

And for running against IDOL:

    {
      "login" : {
        "name": "CommunityAuthentication",
        "community" : {
          "host" : "find-idol"
        }
      },
      "content": {
        "host": "find-idol",
        "port": 9000
      },
      "queryManipulation": {
        "server": {
          "host": "find-idol",
          "port": 16000,
        },
        "expandQuery": true,
        "blacklist": "ISO_BLACKLIST",
        "enabled": true
      },
      "view": {
        "host": "find-idol",
        "port": 9080,
        "referenceField": "URL"
      }
    }

## Hard Coded fields
BI for Human Information looks for the following fields in Idol/HoD documents:
* content_type
* url
* offset
* author
* category
* date_created or created_date
* date_modified or modified_date

If content_type is audio or video, the document is treated as an audio/video file using the url and offset fields.
The url field must point to a video/audio file in a supported format (browser-dependent).
The offset field determines the time (in seconds from the start) after which playback should commence.
All other fields are metadata only, and are displayed, if present, when the document is viewed.

## On Premise implementation details
Document titles are retrieved from the title tag in the Idol response (retrieved via Idol "SetTitleFields" configuration)
The dates on results on the main page are retrieved from the date tag of the Idol response (configured via "SetDateFields" in the Idol configuration file)
Summary is a context summary (see Idol documentation).
The list of databases is retrieved by querying idol using GetStatus (internal databases are not displayed).
Parametric fields are retrieved using GetQueryTagValues. They are configured in the Idol configuration file.

For viewing documents, BI for Human Information requires that a reference field be configured in the BI for Human Information config file.
When a document is selected to be viewed, the content for the document in question is read (using the GetContent action).
The configured field is then read from the document content (first occurrence if the field appears multiple times) and is used as the reference in the subsequent view action.
An error is thrown if the reference field is not configured or could not be found.
Hard-coded parameters on the view action include:
EmbeddedImages=true
StripScript=true
OriginalBaseUrl=true

## Location
Maps of document location metadata can be displayed in the UI if enabled in the configuration file. A sample "map" configuration
is shown below:

    {
        "enabled": true,
        "attribution": "Copyright 2014-2016 Global Maps Incorporated",
        "tileUrlTemplate": "http://tile.osm.org/{z}/{x}/{y}.png"
    }

Set the "enabled" property to true to display maps in the UI. A "tileUrlTemplate" property must be supplied. This must
be the URL for an Open Street Map (OSM) tile server, where the {z}, {x} and {y} parameters are integer coordinates describing
the tile position.

Although any OSM-compatible tile server should be compatible with HPE BI for Human Information, beware of license and
usage restrictions. In particular, the tile server provided by OpenStreetMap itself should not be used except for limited 
testing purposes.

## Is it any good?
Yes.

## License
Copyright 2014-2016 Hewlett-Packard Development Company, L.P.

Licensed under the MIT License (the "License"); you may not use this project except in compliance with the License.
