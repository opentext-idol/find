/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/util/model-any-changed-attribute-listener',
    'text!find/templates/app/page/search/snapshots/snapshot-data-view.html'
], function(Backbone, _, addChangeListener, template) {

    var DATE_FORMAT = 'YYYY/MM/DD HH:mm';

    // The saved search model attributes that we are interested in
    var SAVED_SEARCH_MODEL_ATTRIBUTES = [
        'queryText',
        'relatedConcepts',
        'indexes',
        'parametricValues',
        'minDate',
        'maxDate'
    ];

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function(options) {
            this.savedSearchModel = options.savedSearchModel;
            addChangeListener(this, this.savedSearchModel, SAVED_SEARCH_MODEL_ATTRIBUTES, this.render);
        },

        render: function() {
            this.$el.html(this.template({
                data: this.savedSearchModel.pick(SAVED_SEARCH_MODEL_ATTRIBUTES),
                dateFormat: DATE_FORMAT
            }));
        }
    });

});
