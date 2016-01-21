# What are Java System Properties?

Java System Properties are command line arguments that are provided to a Java application.  They are identified by a `-D`.  If you run the command `java -jar myprogram.jar -Dsomeproperty=somevalue`, you are setting the value of `someproperty` to `somevalue` for the application.

Find uses Java System Properties to configure some runtime settings.

# Find Java System Properties

## Required

- `hp.find.home` - Path to the "home" directory for Find to store config files and logs in, e.g `-Dhp.find.home=/opt/find/home`, or `-Dhp.find.home=C:\HPE\Find`.  The Find process needs read+write permissions on the home directory specified here.

## Optional

- `server.port` - The port to run Find on (defaults to 8080).

### Clustering

-` hp.find.persistentState` - possible values are `INMEMORY` (which is the default) or `REDIS`.  We've not documented how to use Find with Redis yet, so ignore this for now.

### HTTP Proxy Configuration
- `find.https.proxyHost` - the hostname of the proxy server that Find needs to use to contact havenondemand.com over HTTPS, e.g `proxy.corp.example.com`
- `find.https.proxyPort` - the port that the proxy server specified in `find.https.proxyHost` uses for proxy requests, e.g. `8080`