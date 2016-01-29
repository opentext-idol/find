define([
    'backbone',
    'moment',
    'i18n!find/nls/bundle',
    'find/app/model/dates-filter-model',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/search/filters/date/dates-filter-view.html',
    'text!find/templates/app/page/search/filters/date/custom-datepicker.html',
    'text!find/templates/app/page/search/filters/date/date-item.html',
    'bootstrap-datetimepicker'
], function(Backbone, moment, i18n, DatesFilterModel, ListView, template, datepicker, dateItemTemplate) {

    var DATES_DISPLAY_FORMAT = 'YYYY/MM/DD HH:mm';

    function dateUpdater(attribute) {
        return function() {
            var display = '';
            var value = this.queryModel.get(attribute);

            if (value) {
                display = value.format(DATES_DISPLAY_FORMAT);
            }

            this['$' + attribute].find('input').val(display);
        };
    }

    return Backbone.View.extend({
        template: _.template(template),
        datepickerTemplate: _.template(datepicker),
        itemTemplate: _.template(dateItemTemplate),

        events: {
            'click tr': function(e) {
                var $targetRow = $(e.currentTarget);
                var selected = $targetRow.find('[data-id]').data('id');
                var previous = this.datesFilterModel.get('dateRange');

                if(selected === previous) {
                    this.datesFilterModel.setDateRange(null);
                } else {
                    this.datesFilterModel.setDateRange(selected);
                }
            }
        },

        initialize: function(options) {
            this.datesFilterModel = options.datesFilterModel;
            this.queryModel = options.queryModel;

            this.dateFiltersCollection = new Backbone.Collection([
                {
                    id: DatesFilterModel.dateRange.week,
                    label: i18n['search.dates.timeInterval.' + DatesFilterModel.dateRange.week]
                },
                {
                    id: DatesFilterModel.dateRange.month,
                    label: i18n['search.dates.timeInterval.' + DatesFilterModel.dateRange.month]
                },
                {
                    id: DatesFilterModel.dateRange.year,
                    label: i18n['search.dates.timeInterval.' + DatesFilterModel.dateRange.year]
                },
                {
                    id: DatesFilterModel.dateRange.custom,
                    label: i18n['search.dates.timeInterval.'  + DatesFilterModel.dateRange.custom]
                }
            ]);

            this.listView = new ListView({
                collection: this.dateFiltersCollection,
                itemOptions: {
                    tagName: 'tr',
                    className: 'clickable',
                    template: this.itemTemplate
                }
            });

            this.listenTo(this.queryModel, 'change:minDate', this.updateMinDate);
            this.listenTo(this.queryModel, 'change:maxDate', this.updateMaxDate);
            this.listenTo(this.datesFilterModel, 'change:dateRange', this.updateForDateRange);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.listView.setElement(this.$('table')).render();

            this.$el.append(this.datepickerTemplate({
                i18n:i18n
            }));

            this.$minDate = this.$('.results-filter-min-date');
            this.$maxDate = this.$('.results-filter-max-date');

            this.$minDate.datetimepicker({
                format: DATES_DISPLAY_FORMAT,
                icons: {
                    time: 'hp-icon hp-fw hp-clock',
                    date: 'hp-icon hp-fw hp-calendar',
                    up: 'hp-icon hp-fw hp-chevron-up',
                    down:'hp-icon hp-fw hp-chevron-down',
                    next: 'hp-icon hp-fw hp-chevron-right',
                    previous: 'hp-icon hp-fw hp-chevron-left'
                }
            }).on('dp.change', _.bind(function(ev) {
                this.datesFilterModel.setMinDate(ev.date);
            }, this));

            this.$maxDate.datetimepicker({
                format: DATES_DISPLAY_FORMAT,
                icons: {
                    time: 'hp-icon hp-fw hp-clock',
                    date: 'hp-icon hp-fw hp-calendar',
                    up: 'hp-icon hp-fw hp-chevron-up',
                    down:'hp-icon hp-fw hp-chevron-down',
                    next: 'hp-icon hp-fw hp-chevron-right',
                    previous: 'hp-icon hp-fw hp-chevron-left'
                }
            }).on('dp.change', _.bind(function(ev) {
                this.datesFilterModel.setMaxDate(ev.date);
            }, this));

            this.updateForDateRange();
            this.updateMinDate();
            this.updateMaxDate();
        },

        updateMinDate: dateUpdater('minDate'),
        updateMaxDate: dateUpdater('maxDate'),

        updateForDateRange: function() {
            var dateRange = this.datesFilterModel.get('dateRange');

            // Clear all checkboxes, check selected
            this.$('.date-filters-list i').addClass('hide');
            this.$('[data-id=\'' + dateRange + '\'] i').removeClass('hide');

            // If custom show custom options
            this.$('.search-dates-wrapper').toggleClass('hide', dateRange !== DatesFilterModel.dateRange.custom);
        }
    });

});
