/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/document/authors-tab.html'
], function(Backbone, _, i18n, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                authors: this.model.get('authors')
            }));
        }
    });
});
