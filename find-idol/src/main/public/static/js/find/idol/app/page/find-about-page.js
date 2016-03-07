/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'about-page/js/about-page',
    'find/idol/app/page/about/lib-list',
    'find/app/configuration',
    'i18n!find/nls/bundle'
], function(AboutPage, libList, configuration, i18n) {

    return AboutPage.extend({
        className: 'col-md-12',

        initialize: function() {
            var config = configuration();

            AboutPage.prototype.initialize.call(this, {
                libraries: libList,
                icon: ' ',
                strings: {
                    copyright: i18n['about.copyright'],
                    foss: i18n['about.foss'],
                    fossVersion: i18n['about.lib.version'],
                    library: i18n['about.lib.name'],
                    license: i18n['about.lib.licence'],
                    search: i18n['about.search'],
                    tagLine: i18n['about.tagLine'],
                    title: i18n['app.about'],
                    version: i18n['about.app.version'],
                    versionString: i18n['about.versionString'](config.version, config.commit)
                }
            });
        }

    });

});
