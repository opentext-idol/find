---
layout: default
level: advanced
title: Understanding the Code Structure
---

# Tools

- [Maven](https://maven.apache.org/) is used for Java compilation and constructing the .war files
- [webpack](https://webpack.js.org/) bundles the frontend's JavaScript and CSS, run automatically as part of the Maven `compile` phase (see `idol/webpack.config.js`).
- [NPM](https://www.npmjs.com/) is used to manage frontend dependencies (i.e. JavaScript libraries, mostly). There is no Bower - `bower.json` was retired along with RequireJS/AMD.

# Maven Modules

Find consists of three modules:

- `core` contains the core application components. When built, it creates a jar file that is imported by the `idol` module.
- `idol` contains the IDOL-specific code.  When built, it creates an executable war file for running against IDOL.
- `on-prem-dist` is responsible for packaging the IDOL artifacts into a zip file for distribution.

# Maven Profiles

By default, `mvn install` runs webpack in `development` mode. Running with the `production` profile builds webpack in `production` mode, which minifies the JavaScript and CSS.

# Frontend Development Loop

Running `npm run watch` from the `idol` module runs webpack in watch mode, recompiling the JavaScript bundles as source files change, and copying non-JS static assets straight into `target/classes/static`. See [[Running-a-Development-Copy-of-Find]] for the full dev-loop workflow, including the caveats around LESS compilation and `.mustache` templates.
