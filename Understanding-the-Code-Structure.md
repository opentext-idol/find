# Maven Modules

Find consists of four [Maven](https://maven.apache.org/) modules:

- `find-core` contains the core application components which are not specific to either Haven OnDemand or IDOL.  When built, it created a jar file that is imported by the `find-idol` and `find-hod` modules
- `find-idol` contains the IDOL-specific code.  When built, it creates an executable war file for running against IDOL.
- `find-hod` contains the Haven OnDemand-specific code.  When built, it creates an executable war file for running against Haven OnDemand.
- `find-dist` is responsible for packaging the IDOL artifacts into a zip file for distribution.
