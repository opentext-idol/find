/*
 * (c) Copyright 2014-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'about-page/js/about-page',
    'find/idol/app/page/about/lib-list',
    'find/app/configuration',
    'i18n!find/nls/bundle'
], function(AboutPage, libList, configuration, i18n) {
    'use strict';

    return AboutPage.extend({
        className: 'container-fluid',

        initialize: function() {
            const config = configuration();

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
