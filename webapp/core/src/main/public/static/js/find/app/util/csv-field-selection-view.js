/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const $ = require('jquery');
const Backbone = require('backbone');
const ListView = require('js-whatever/js/list-view');
const ItemView = require('find/app/util/csv-field-selection-list-item');
const configuration = require('find/app/configuration');
const exportFormTemplate = require('find/templates/app/util/csv-export-form-template.html');

module.exports = Backbone.View.extend({
        formTemplate: _.template(exportFormTemplate),

        events: {
            'ifClicked .csv-field-label': function(e) {
                const selectedFieldsModel = this.exportFieldCollection.get(
                    $(e.currentTarget).attr('data-field-id')
                );
                // checked is the old value
                selectedFieldsModel.set('selected', !$(e.target).prop('checked'));
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;

            const config = configuration();
            const fieldsInfo = config.fieldsInfo;

            const metadataModels = _.map(config.metadataFieldInfo, function(info) {
                return _.extend({selected: true}, info);
            });

            const whitelistedFieldsInfo = _.filter(fieldsInfo, { csvExport:true });

            // If there's fields which explicitly have csvExport: true, then we'll only show them, otherwise we'll
            //    hide any fields which explicitly have csvExport: false.
            const fieldsInfoToShow = whitelistedFieldsInfo.length ? whitelistedFieldsInfo : _.filter(fieldsInfo, function(field){
                return field.csvExport !== false;
            })

            const fieldModels = _.map(fieldsInfoToShow, function(info) {
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
            this.$el.html(this.listView.el);
        },

        requestCsv: function() {
            const selectedFields = _.pluck(this.exportFieldCollection.where({selected: true}), 'id');

            const queryRequest = JSON.stringify({
                queryRestrictions: {
                    text: this.queryModel.get('queryText'),
                    field_text: this.queryModel.get('fieldText')
                        ? this.queryModel.get('fieldText').toString()
                        : '',
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

            $(this.formTemplate({queryRequest: queryRequest, fields: selectedFields}))
                .appendTo('body').submit().remove();
        }
    });

