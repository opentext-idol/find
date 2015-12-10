/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'about-page/js/about-page',
    'find/idol/app/page/about/lib-list',
    'i18n!find/nls/bundle'
], function(AboutPage, libList, i18n) {

    return AboutPage.extend({

        initialize: function() {
            // TODO: placeholder
            AboutPage.prototype.initialize.call(this, {
                libraries: libList,
                about: {
                    build: 'Awesome',
                    version: '42'
                },
                strings: {
                    build: i18n['about.app.build'],
                    copyright: i18n['about.copyright'],
                    foss: i18n['about.foss'],
                    fossVersion: i18n['about.lib.version'],
                    library: i18n['about.lib.name'],
                    license: i18n['about.lib.licence'],
                    tagLine: i18n['about.tagLine'],
                    title: i18n['app.about'],
                    version: i18n['about.app.version']
                }
            });
        }

    });

});
