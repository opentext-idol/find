# Tools

- [Maven](https://maven.apache.org/)is used for Java compilation and constructing the .war files
- [Grunt](http://gruntjs.com/) tasks are used for watching and refreshing static resources (i.e. JavaScript, CSS, and HTML) without having to restart the Sprint Boot Java server.
- [NPM](https://www.npmjs.com/) and [Bower](http://bower.io/) are used to manage frontend dependencies (i.e. JavaScript libraries, mostly). These are automatically run in the Maven process-sources phase.

# Maven Modules

Find consists of four  modules:

- `find-core` contains the core application components which are not specific to either Haven OnDemand or IDOL.  When built, it created a jar file that is imported by the `find-idol` and `find-hod` modules
- `find-idol` contains the IDOL-specific code.  When built, it creates an executable war file for running against IDOL.
- `find-hod` contains the Haven OnDemand-specific code.  When built, it creates an executable war file for running against Haven OnDemand.
- `find-dist` is responsible for packaging the IDOL artifacts into a zip file for distribution.

# Maven Profiles

During development, the `develop` profile should be used. Running with the `production` profile will minify the JavaScript and CSS, and [bless](http://blesscss.com/) the CSS for older versions of Internet Explorer.
