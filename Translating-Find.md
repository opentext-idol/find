If you want to translate/localize/internationalize Find into another language, this article is for you!

# How language bundles in Find work

Find uses [Require.js](http://requirejs.org/) for loading JavaScript modules.  Require has a `i18n` (short for "internationalization") module for translating web applications.  The documentation is [here](http://requirejs.org/docs/api.html#i18n).

When a user navigates to the Find web application, the Find code "requires" the internationization bundle.  Internally, the `i18n` plugin reads the browser's locale and checks to see if there is a translation for that language - if there isn't, the default language bundle is used.

# Adding a new language bundle

Let's translate Find into French.

1. Navigate to the Find i18n bundles directory: [find-core/src/main/public/static/js/find/nls](https://github.com/hpe-idol/find/tree/master/find-core/src/main/public/static/js/find/nls)
2. Open `bundle.js` - this is the master list of all translations - `root` is the default language, and an `en-gb` dummy translation has been provided as an example.
3. Add a new key-value pair to `bundle.js`.  The key should be the [RFC 1766](https://www.ietf.org/rfc/rfc1766.txt) language tag (e.g. `fr-ca` for French (Canadian), `nv` for Navajo, etc).  The value should be `true`, to enable the new translation.
4. Add the same key-value pair to `errors.js` and `indexes.js`.
5. Copy the `root` directory and give you copy the same name as the language tag you have added to the JavaScript files, e.g. `fr-ca`.
6. Open and edit all of the JavaScript files in your new folder.  Replace the values of each key-value pair with your translations.  Make sure that you match up all `"` and `'` characters - using a tool like [JSLint](http://www.jslint.com/) to check your syntax would be a good idea here (expect it to produce warnings, but keep an eye out for `Unclosed string.` messages)
7. Build and deploy Find.  Your translation is now available!

# Replacing the default language bundle

The `root` language bundle is used as a default.  Changing the language of the default is as simple as editing the root bundle.

1. Follow the steps for "Adding a new language bundle", to add a new `en-us` language bundle, using the existing `root` bundle.
2. Instead of editing your new language bundle, edit the JavaScript files in the `root` directory.