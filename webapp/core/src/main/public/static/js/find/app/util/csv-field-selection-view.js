/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'underscore',
    'jquery',
    'js-whatever/js/list-view',
    'find/app/util/csv-field-selection-list-item',
    'find/app/configuration',
    'text!find/templates/app/util/csv-export-form-template.html'
], function(Backbone, _, $, ListView, ItemView, configuration, exportFormTemplate) {
    "use strict";

    return Backbone.View.extend({
        formTemplate: _.template(exportFormTemplate),

        events: {
            'ifClicked .csv-field-label': function(e) {
                var $currentTarget = $(e.currentTarget);
                var fieldName = $currentTarget.attr('data-field-id');

                var selectedFieldsModel = this.exportFieldCollection.get(fieldName);

                // checked is the old value
                var selected = !$(e.target).prop('checked');
                selectedFieldsModel.set('selected', selected);
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;

            var fieldsInfo = configuration().fieldsInfo;

            var metadataModels = _.values(configuration().metadataFieldIds).map(function(id) {
                return {id: id, selected: true};
            });

            var fieldModels = _.map(fieldsInfo, function(info) {
                return _.extend({selected: true}, info);
            });

            this.exportFieldCollection = new Backbone.Collection(metadataModels.concat(fieldModels));

            this.listView = new ListView({
                collection: this.exportFieldCollection,
                ItemView: ItemView,
                collectionChangeEvents: {
                    selected: 'updateSelected'
                }
            });

            this.listenTo(this.exportFieldCollection, 'update change', function() {
                // TODO: Inelegant, event is triggered every time a checkbox is clicked.
                if(this.exportFieldCollection.where({selected: true}).length === 0) {
                    this.trigger('primary-button-disable');
                } else {
                    this.trigger('primary-button-enable');
                }
            });
        },

        render: function() {
            this.listView.render();
            this.$el.empty().append(this.listView.el);
        },

        requestCsv: function() {
            var selectedFields = _.pluck(this.exportFieldCollection.where({selected: true}), 'id');

            //noinspection AmdModulesDependencies
            var searchRequest = JSON.stringify({
                queryRestrictions: {
                    text: this.queryModel.get('queryText'),
                    field_text: this.queryModel.get('fieldText'),
                    indexes: this.queryModel.get('indexes'),
                    min_date: this.queryModel.getIsoDate('minDate'),
                    max_date: this.queryModel.getIsoDate('maxDate'),
                    min_score: this.queryModel.get('minScore'),
                    anyLanguage: true
                },
                start: 1,
                max_results: 0x7fffffff, // 2^31 - 1
                summary: 'context',
                sort: this.queryModel.get('sort'),
                highlight: false,
                auto_correct: false,
                queryType: 'MODIFIED'
            });

            var $form = $(this.formTemplate({searchRequest: searchRequest, fields: selectedFields}));
            $form.appendTo('body').submit().remove();
        }
    });
});
