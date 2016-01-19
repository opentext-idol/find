/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/public/pages',
    'find/idol/app/page/idol-find-search',
    'find/idol/app/page/find-about-page',
    'i18n!find/nls/bundle'
], function(Pages, FindSearch, AboutPage, i18n) {
    'use strict';

    return Pages.extend({
        initializePages: function() {
            Pages.prototype.initializePages.call(this);

            this.pages = this.pages.concat([
                {
                    constructor: AboutPage,
                    icon: 'fa fa-cog',
                    pageName: 'about',
                    title: i18n['app.about']
                }
            ]);
        }
    });
});
