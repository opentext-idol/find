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
                    build: 'Build',
                    copyright: 'Copyright HP 2015',
                    foss: 'FOSS Acknowledgements',
                    fossVersion: 'Version',
                    library: 'Library',
                    license: 'License',
                    tagLine: 'We did it',
                    title: 'About',
                    version: 'Version'
                }
            });
        }

    });

});
