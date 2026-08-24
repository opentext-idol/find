---
layout: default
level: basic
title: Running a Development Copy of Find
---

# Getting Started

So you've made some changes to Find and want to test them.  This is the page for you!

You need to understand that before you **start** the Find webapp, you need to choose whether you want to run it against HPE IDOL or Haven OnDemand.

For the purposes of this guide, we're assuming that you've already cloned Find from GitHub and installed all of the dependencies.  If you haven't, go and follow the steps on the [Building Find](https://github.com/hpautonomy/find/wiki/Building-Find) Wiki page.

## Step One: Compile and Install the modules

- `mvn install`

It's important to run the `install` step, as this "installs" the modules to your local Maven repository.  This makes the `core` module available for `idol` and `hod` to use as a dependency.  This step also runs `npm ci` and webpack (in `development` mode), producing the JavaScript/CSS bundles.

## Step Two: Use Spring Boot to run a local server

- `cd` into either `hod` or `idol`, depending on whether you want to run Find against Haven OnDemand or IDOL.
- Run `mvn spring-boot:run -Dhp.find.home=<path_to_find_home_directory>`.  You will need to create a home directory for Find - have a look at [[Home-directory]].

Any changes you make to Java files will be recompiled and redeployed automatically.

## Step Three: Keep JavaScript, CSS, and other static files up to date

From your `idol` folder, run:

- `npm run watch`

This runs webpack in watch mode, which recompiles the JavaScript bundles (`public.js`, `config.js`, `login.js`, `themetracker.js`) as soon as you save a change to any source file, and copies non-JS static assets (fonts, images, favicon) straight into `target/classes/static`. Keep this running in a terminal alongside `mvn spring-boot:run`.

If you're editing files in the `core` module rather than `idol`, run `mvn install -pl core` first so `idol`'s webpack build picks up the change (webpack watches idol's own merged `target/classes/static/js` tree, which is repopulated from `core`'s jar).

**Two things `npm run watch` does *not* cover:**

- **LESS/CSS**: LESS is compiled once, on server startup (`@PostConstruct`), and cached for the life of the process.  If you edit a `.less` file, you'll need to restart `mvn spring-boot:run` to see the change (webpack's dev-mode asset copy only handles already-compiled `.css`, not `.less` compilation).
- **`.mustache` templates**: these live in `core` and are not watched or auto-copied by webpack.  After editing one, run `mvn install -pl core` and restart `mvn spring-boot:run`.

## Step Four: Open Find in your web browser

- Navigate to http://localhost:8080 and you should see Find.  A config file has been generated in the Find home directory that you specified on the command line in step two.

JavaScript/CSS/static-asset changes will appear after a short delay (usually less than a couple of seconds while `npm run watch` recompiles) - refresh your browser to pick up client side changes.

You now want to read either [[Configuring Find for IDOL]] or [[Configuring Find for Haven OnDemand]] to get Find configured.
