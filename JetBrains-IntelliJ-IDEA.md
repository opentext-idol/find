Notes on how to run Find from [JetBrains IntelliJ IDEA](https://www.jetbrains.com/idea/) Ultimate edition.

# IntelliJ 14 Ultimate

## Run Configuration for IDOL

- Create a new Run/Debug Configuration of the `Spring Boot` type
- The Main class is `com.hp.autonomy.frontend.find.IdolFindApplication`
- VM options are `-Dhp.find.home=<path_to_find_home_directory>` (see [[Find Java System Properties]] for other optional settings)
- "Use classpath of module" should be set to `find-idol`

You'll then want to have a look at [[Configuring Find for IDOL]].

## Run Configuration for Haven OnDemand

- Create a new Run/Debug Configuration of the `Spring Boot` type
- The Main class is `com.hp.autonomy.frontend.find.HodFindApplication`
- VM options are: `-Dhp.find.home=<path_to_find_home_directory> -Dfind.iod.api=https://api.havenondemand.com -Dfind.hod.sso=https://www.havenondemand.com/sso.html` (see [[Find Java System Properties]] for other optional settings)
- "Use classpath of module" should be set to `find-hod`

You'll then want to have a look at [[Configuring Find for Haven OnDemand]].