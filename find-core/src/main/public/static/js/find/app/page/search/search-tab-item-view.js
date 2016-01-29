/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/list-item-view',
    'underscore',
    'text!find/templates/app/page/search/search-tab-item-view.html'
], function(ListItemView, _, template) {

    var templateFunction = _.template(template);

    return ListItemView.extend({
        tagName: 'li',

        initialize: function(options) {
            ListItemView.prototype.initialize.call(this, _.defaults({
                template: templateFunction,
                templateOptions: {
                    searchCid: this.model.cid
                }
            }, options));
        }
    });

});
