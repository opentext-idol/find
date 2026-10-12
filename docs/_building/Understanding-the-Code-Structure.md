---
layout: default
level: advanced
title: Understanding the Code Structure
---

# Tools

- [Maven](https://maven.apache.org/) is used for Java compilation and constructing the .war files
- [webpack](https://webpack.js.org/) bundles the frontend's JavaScript, run automatically as part of the Maven `compile` phase (see `idol/webpack.config.js`).
- [NPM](https://www.npmjs.com/) is used to manage frontend dependencies (i.e. JavaScript libraries, mostly).
- [less4j](https://github.com/SomMeri/less4j) compiles the LESS in `core/src/main/resources/less` into CSS, in the running application at startup rather than at build time.

# Maven Modules

Find consists of four modules:

- `frontend` owns the NPM dependencies.  When built, it installs `node_modules` and packages it as a jar, which `core` and `idol` consume as a build input.  The jar is installed locally but never deployed.
- `core` contains the core application components. When built, it creates a jar file that is imported by the `idol` module, plus a `frontend`-classified jar holding its Javascript for webpack to bundle.
- `idol` contains the IDOL-specific code.  When built, it creates an executable war file for running against IDOL.
- `on-prem-dist` is responsible for packaging the IDOL artifacts into a zip file for distribution.

# Maven Profiles

By default, `mvn install` runs webpack in `development` mode. Running with the `production` profile builds webpack in `production` mode, which minifies the JavaScript.
