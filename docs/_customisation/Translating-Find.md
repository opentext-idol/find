---
title: Translating Find
layout: default
---

If you want to translate/localize/internationalize Find into another language, this article is for you!

# How language bundles in Find work

Locale bundles are bundled by webpack, and locale selection happens with a small helper, [`find/nls/select-locale`](https://github.com/hpe-idol/find/tree/master/webapp/core/src/main/public/static/js/find/nls/select-locale.js).

Each translatable area of Find has a "master bundle" module (e.g. `find/nls/bundle`, `find/nls/indexes`, `find/idol/nls/comparisons`) that uses `select-locale` to pick and merge the right locale module for the current browser locale.

# Adding a new language bundle

Let's translate Find into French.

1. Navigate to the Find i18n bundles directory: [webapp/core/src/main/public/static/js/find/nls](https://github.com/hpe-idol/find/tree/master/webapp/core/src/main/public/static/js/find/nls) (IDOL-specific bundles live under [webapp/idol/src/main/public/static/js/find/idol/nls](https://github.com/hpe-idol/find/tree/master/webapp/idol/src/main/public/static/js/find/idol/nls) instead).
2. Open the bundle file, e.g. `bundle.js` or `indexes.js` - `root` is the default language, and an `en-gb` dummy translation has been provided as an example.
3. Add a new `require()` for your locale's bundle module, and add a matching key to the object passed to `selectLocale()`.  The key should be the [RFC 1766](https://www.ietf.org/rfc/rfc1766.txt) language tag (e.g. `fr-ca` for French (Canadian), `nv` for Navajo, etc).
4. Copy the `root` directory and give your copy the same name as the language tag you have added to the JavaScript files, e.g. `fr-ca`.
5. Open and edit all of the JavaScript files in your new folder.  Replace the values of each key-value pair with your translations.  Make sure that you match up all `"` and `'` characters.
6. Build and deploy Find.  Your translation is now available!

# Replacing the default language bundle

The `root` language bundle is used as a default.  Changing the language of the default is as simple as editing the root bundle.

1. Follow the steps for "Adding a new language bundle", to add a new `en-us` language bundle, using the existing `root` bundle.
2. Instead of editing your new language bundle, edit the JavaScript files in the `root` directory.
