/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'underscore',
    'jquery',
    'backbone',
    'moment',
    'i18n!find/nls/bundle',
    'find/app/model/dates-filter-model',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/util/date-picker',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/search/filters/date/dates-filter-view.html',
    'bootstrap-datetimepicker'
], function(_, $, Backbone, moment, i18n, DatesFilterModel, SavedSearchModel,
            datePicker, ListView, template) {
    'use strict';

    function dateUpdater(attribute) {
        return function() {
            const value = this.datesFilterModel.get(attribute);
            const display = value
                ? value.format(datePicker.DATE_WIDGET_FORMAT)
                : '';

            this.$('[data-date-attribute="' + attribute + '"] input')
                .val(display);
        };
    }

    return Backbone.View.extend({
        events: {
            'click tr': function(event) {
                const $targetRow = $(event.currentTarget);
                const selected = $targetRow.attr('data-filter-id');
                const previous = this.datesFilterModel.get('dateRange');

                this.datesFilterModel.set('dateRange', selected === previous
                    ? null
                    : selected);
            },
            'dp.change .results-filter-date[data-date-attribute]': function(event) {
                const attributes = {dateRange: DatesFilterModel.DateRange.CUSTOM};
                attributes[$(event.target).attr('data-date-attribute')] = event.date;
                this.datesFilterModel.set(attributes);
            }
        },

        initialize: function(options) {
            this.datesFilterModel = options.datesFilterModel;
            this.savedSearchModel = options.savedSearchModel;

            this.template = _.template(template);

            this.listenTo(this.datesFilterModel, 'change:dateRange', function() {
                this.updateForDateRange();
                this.updateDateNewDocsLastFetched();
            });
            this.listenTo(this.datesFilterModel, 'change:customMaxDate', this.updateMaxDate);
            this.listenTo(this.datesFilterModel, 'change:customMinDate', this.updateMinDate);

            this.listenTo(this.savedSearchModel, 'sync', this.render);

            this.updateDateNewDocsLastFetched();
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                customFilterData: [
                    {headingKey: 'app.from', targetAttribute: 'customMinDate'},
                    {headingKey: 'app.until', targetAttribute: 'customMaxDate'}
                ],
                filters: this.getFilters()
            }));

            const generateDatePickerCallback = function(attribute) {
                return function() {
                    const attributes = {dateRange: DatesFilterModel.DateRange.CUSTOM};
                    const stringValue = this.$('[data-date-attribute="' + attribute + '"]').find('input').val();
                    attributes[attribute] = moment(stringValue, datePicker.DATE_WIDGET_FORMAT);
                    this.datesFilterModel.set(attributes);
                }.bind(this)
            }.bind(this);

            datePicker.render(this.$el.find('.results-filter-date[data-date-attribute="customMinDate"]'), generateDatePickerCallback('customMinDate'));
            datePicker.render(this.$el.find('.results-filter-date[data-date-attribute="customMaxDate"]'), generateDatePickerCallback('customMaxDate'));

            this.$('.date-filters-list [data-filter-id="' + DatesFilterModel.DateRange.NEW + '"]').tooltip({
                title: i18n['search.dates.timeInterval.new.description'],
                placement: 'right'
            });

            this.updateForDateRange();
            this.updateMinDate();
            this.updateMaxDate();
        },

        updateMinDate: dateUpdater('customMinDate'),
        updateMaxDate: dateUpdater('customMaxDate'),

        updateForDateRange: function() {
            const dateRange = this.datesFilterModel.get('dateRange');

            // Clear all checkboxes, check selected
            this.$('.date-filters-list i').addClass('hide');
            this.$('.date-filters-list [data-filter-id="' + dateRange + '"] i').removeClass('hide');

            // If custom show custom options
            this.$('.search-dates-wrapper').toggleClass('hide', dateRange !== DatesFilterModel.DateRange.CUSTOM);
        },

        updateDateNewDocsLastFetched: function() {
            if(this.datesFilterModel.get('dateRange') === DatesFilterModel.DateRange.NEW && !this.savedSearchModel.isNew()) {
                this.datesFilterModel.set('dateNewDocsLastFetched', this.savedSearchModel.get('dateNewDocsLastFetched'));
                this.savedSearchModel.save({
                    dateNewDocsLastFetched: moment()
                });
            }
        },

        getFilters: function() {
            const filters = [
                DatesFilterModel.DateRange.WEEK,
                DatesFilterModel.DateRange.MONTH,
                DatesFilterModel.DateRange.YEAR,
                DatesFilterModel.DateRange.CUSTOM
            ];

            if(!this.savedSearchModel.isNew()) {
                filters.unshift(DatesFilterModel.DateRange.NEW);
            }

            return filters;
        }
    });
});
