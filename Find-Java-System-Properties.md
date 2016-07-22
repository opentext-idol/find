# What are Java System Properties?

Java System Properties are command line arguments that are provided to a Java application.  They are identified by a `-D`.  If you run the command `java -jar myprogram.jar -Dsomeproperty=somevalue`, you are setting the value of `someproperty` to `somevalue` for the application.

Find uses Java System Properties to configure some runtime settings.

# Find Java System Properties

## Required

- `hp.find.home` - Path to the "home" directory for Find to store config files and logs in, e.g `-Dhp.find.home=/opt/find/home`, or `-Dhp.find.home=C:\HPE\Find`.  Have a look at [[Home-directory]] for details on how to create the home directory.

## Optional

- `server.port` - The port to run Find on (defaults to 8080).
- `server.session.timeout` - The session timeout in seconds (defaults to 3600).

### Database

Find uses a relational database to store users' saved searches. By default, this is an embedded [H2](http://www.h2database.com/) database which persists data to disk in the Find home directory. The application runs schema [migration scripts](https://github.com/hpe-idol/find/tree/develop/core/src/main/resources/db/migration) on connection using [Flyway](https://flywaydb.org/). You can use the system properties in this section to configure Find to connect to an external database to allow the application to be clustered.

- `spring.datasource.url` - A JDBC URL for the database, eg "jdbc:mariadb://my-maria-db:3306/find". The Find jar is built with H2 and MariaDB/MySQL connectors included. The application uses a database/schema called "find" which it will attempt to create if running the automatic migration scripts.
- `spring.datasource.username` - Database username. Must have access to the "find" database or permission to create it.
- `spring.datasource.password` - Database user password.
- `flyway.enabled` - Set to false to disable automatic schema migration. Automatic migration may not be desirable in a clustered deployment.
- `spring.datasource.platform` - If Flyway is enabled, this is used to choose which migration script dialect to use. Can be "h2" (the default) or "mysql".

### Clustering

-` hp.find.persistentState` - possible values are `INMEMORY` (which is the default) or `REDIS`.  We've not documented how to use Find with Redis yet, so ignore this for now.

### HTTP Proxy Configuration
- `find.https.proxyHost` - the hostname of the proxy server that Find needs to use to contact havenondemand.com over HTTPS, e.g `proxy.corp.example.com`
- `find.https.proxyPort` - the port that the proxy server specified in `find.https.proxyHost` uses for proxy requests, e.g. `8080`