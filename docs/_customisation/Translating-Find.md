---
title: Translating Find
layout: default
---

If you want to translate/localize/internationalize Find into another language, this article is for you!

# How language bundles in Find work

Find used to use [Require.js](http://requirejs.org/)'s `i18n` loader plugin for translating web applications, which picked the browser's locale and loaded the matching bundle at runtime.  Find no longer uses RequireJS - all locale bundles are now bundled by webpack, and locale selection happens with a small helper, [`find/nls/select-locale`](https://github.com/hpe-idol/find/tree/master/webapp/core/src/main/public/static/js/find/nls/select-locale.js), that reimplements the same merge behaviour statically at runtime from bundles that are all already loaded (as build-time `require()`s).

Each translatable area of Find has a "master bundle" module (e.g. `find/nls/bundle`, `find/nls/errors`, `find/nls/indexes`, `find/idol/nls/comparisons`, `find/idol/nls/snapshots`) that `require()`s `find/nls/select-locale` plus one module per known locale, and calls `selectLocale({ root: rootBundle, 'en-gb': enGbBundle, ... })` to pick and merge the right one for the current browser locale.

# Adding a new language bundle

Let's translate Find into French.

1. Navigate to the Find i18n bundles directory: [webapp/core/src/main/public/static/js/find/nls](https://github.com/hpe-idol/find/tree/master/webapp/core/src/main/public/static/js/find/nls) (IDOL-specific bundles - `comparisons` and `snapshots` - live under [webapp/idol/src/main/public/static/js/find/idol/nls](https://github.com/hpe-idol/find/tree/master/webapp/idol/src/main/public/static/js/find/idol/nls) instead).
2. Open `bundle.js` - this is the master list of all translations - `root` is the default language, and an `en-gb` dummy translation has been provided as an example.
3. Add a new `require()` for your locale's bundle module to `bundle.js`, and add a matching key to the object passed to `selectLocale()`.  The key should be the [RFC 1766](https://www.ietf.org/rfc/rfc1766.txt) language tag (e.g. `fr-ca` for French (Canadian), `nv` for Navajo, etc).
4. Do the same for `errors.js` and `indexes.js` (and, for IDOL-specific translations, `find/idol/nls/comparisons.js` and `find/idol/nls/snapshots.js`).
5. Copy the `root` directory and give your copy the same name as the language tag you have added to the JavaScript files, e.g. `fr-ca`.
6. Open and edit all of the JavaScript files in your new folder.  Replace the values of each key-value pair with your translations.  Make sure that you match up all `"` and `'` characters.
7. Build and deploy Find.  Your translation is now available!

# Replacing the default language bundle

The `root` language bundle is used as a default.  Changing the language of the default is as simple as editing the root bundle.

1. Follow the steps for "Adding a new language bundle", to add a new `en-us` language bundle, using the existing `root` bundle.
2. Instead of editing your new language bundle, edit the JavaScript files in the `root` directory.