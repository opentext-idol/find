/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/page/search/document/tab-content-view'
], function(Backbone, i18n, TabContentView) {
    'use strict';

    // TODO: for demonstration, replace & move this when implementing proper tabs
    var MetaDataTabContent = Backbone.View.extend({
        render: function () {
            this.$el.html('<p>Empty Tab</p>');
        }
    });

    var SecondTabContent = Backbone.View.extend({
        render: function () {
            this.$el.html('<h2>Placeholder</h2>');
        }
    });

    return [
        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: MetaDataTabContent}),

            title: i18n['search.document.detail.tabs.metadata'],

            shown: function (documentModel) {
                return true;
            }
        },

        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: SecondTabContent}),

            title: 'Placeholder',

            shown: function (documentModel) {
                return true;
            }
        }
    ];
});
