define([
    'backbone',
    'moment',
    'i18n!find/nls/bundle',
    'find/app/model/dates-filter-model',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/date/dates-filter-view.html',
    'text!find/templates/app/page/date/custom-datepicker.html',
    'text!find/templates/app/page/date/date-item.html',
    'bootstrap-datetimepicker'
], function(Backbone, moment, i18n, DatesFilterModel, ListView, template, datepicker, dateItemTemplate) {

    var DATES_DISPLAY_FORMAT = 'YYYY/MM/DD HH:mm';

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

            _.each(['minDate', 'maxDate'], function(date) {
                this.listenTo(this.queryModel, 'change:' + date, function(model, value) {
                    var display = '';

                    if(value) {
                        display = value.format(DATES_DISPLAY_FORMAT);
                    }

                    this['$' + date].find('input').val(display);
                });
            }, this);

            this.listenTo(this.datesFilterModel, 'change:dateRange', function(datesFilterModel, dateRange) {
                // Clear all checkboxes, check selected
                this.$('.date-filters-list i').addClass('hide');
                this.$("[data-id='" + dateRange + "'] i").removeClass('hide');

                // If custom show custom options
                this.$('.search-dates-wrapper').toggleClass('hide', dateRange !== DatesFilterModel.dateRange.custom);
            });
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
                    time: 'fa fa-clock-o',
                    date: 'fa fa-calendar',
                    up: 'fa fa-chevron-up',
                    down:'fa fa-chevron-down',
                    next: 'fa fa-chevron-right',
                    previous: 'fa fa-chevron-left'
                }
            }).on('dp.change', _.bind(function(ev) {
                this.setMinDate(ev.date);
            }, this));

            this.$maxDate.datetimepicker({
                format: DATES_DISPLAY_FORMAT,
                icons: {
                    time: 'fa fa-clock-o',
                    date: 'fa fa-calendar',
                    up: 'fa fa-chevron-up',
                    down:'fa fa-chevron-down',
                    next: 'fa fa-chevron-right',
                    previous: 'fa fa-chevron-left'
                }
            }).on('dp.change', _.bind(function(ev) {
                this.setMaxDate(ev.date);
            }, this));

        },

        setMinDate: function(date) {
            this.datesFilterModel.setMinDate(date);
        },

        setMaxDate: function(date) {
            this.datesFilterModel.setMaxDate(date);
        }
    });

});
