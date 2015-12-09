/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/public/pages',
    'find/idol/app/page/idol-find-search',
    'find/idol/app/page/find-about-page'
], function(Pages, FindSearch, AboutPage) {
    'use strict';

    return Pages.extend({
        initializePages: function() {
            Pages.prototype.initializePages.call(this);

            this.pages = this.pages.concat([
                {
                    constructor: AboutPage,
                    pageName: 'about'
                }
            ]);
        }
    });
});
