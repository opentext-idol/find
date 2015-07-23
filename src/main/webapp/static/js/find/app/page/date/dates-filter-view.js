define([
    'backbone',
    'moment',
    'i18n!find/nls/bundle',
    'find/app/model/backbone-query-model',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/date/dates-filter-view.html',
    'text!find/templates/app/page/date/custom-datepicker.html',
    'text!find/templates/app/page/date/date-item.html',
    'bootstrap-datetimepicker'
], function(Backbone, moment, i18n, QueryModel, ListView, template, datepicker, dateItemTemplate) {

    var DateRangeDescription = {
        year:   {maxDate: moment(), minDate: moment().subtract(1, 'years')},
        month: {maxDate: moment(), minDate: moment().subtract(1, 'months')},
        week:  {maxDate: moment(), minDate: moment().subtract(1, 'weeks')}
    };

    return Backbone.View.extend({
        template: _.template(template),
        datepickerTemplate: _.template(datepicker),
        itemTemplate: _.template(dateItemTemplate),

        events: {
            'click tr': function(e) {
                var $targetRow = $(e.currentTarget);
                this.queryModel.set('dateRange', $targetRow.find('[data-id]').data('id'));
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;

            this.customMinDate = null;
            this.customMaxDate = null;

            this.dateFiltersCollection = new Backbone.Collection([
                {
                    id: QueryModel.DateRange.week,
                    label: i18n['search.dates.timeInterval.' + QueryModel.DateRange.week]
                },
                {
                    id: QueryModel.DateRange.month,
                    label: i18n['search.dates.timeInterval.' + QueryModel.DateRange.month]
                },
                {
                    id: QueryModel.DateRange.year,
                    label: i18n['search.dates.timeInterval.' + QueryModel.DateRange.year]
                },
                {
                    id: QueryModel.DateRange.custom,
                    label: i18n['search.dates.custom']
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
                    if (!value) {
                        // datepicker doesn't like undefined, so pass in null
                        this['$' + date].data('DateTimePicker').date(null);
                    }
                });
            }, this);

            this.listenTo(this.queryModel, 'change:dateRange', function() {
                var dateRange = this.queryModel.get('dateRange');

                this.$('.date-filters-list i').addClass('hide');
                this.$("[data-id='" + dateRange + "'] i").removeClass('hide');

                this.$('.search-dates-wrapper').toggleClass('hide', dateRange !== QueryModel.DateRange.custom);

                if(dateRange === QueryModel.DateRange.custom) {
                    this.queryModel.set({
                        minDate: this.customMinDate,
                        maxDate: this.customMaxDate,
                        dateRange: QueryModel.DateRange.custom
                    })
                } else {
                    this.queryModel.set({
                        minDate: DateRangeDescription[dateRange]['minDate'],
                        maxDate: DateRangeDescription[dateRange]['maxDate'],
                        dateRange: dateRange
                    });
                }
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
                format: 'YYYY/MM/DD hh:mm',
                icons: {
                    time: 'icon-time',
                    date: 'icon-calendar',
                    up: 'icon-chevron-up',
                    down:'icon-chevron-down',
                    next: 'icon-chevron-right',
                    previous: 'icon-chevron-left'
                }
            }).on('dp.change', _.bind(function(ev) {
                this.setMinDate(ev.date);
            }, this));

            this.$maxDate.datetimepicker({
                format: 'YYYY/MM/DD hh:mm',
                icons: {
                    time: 'icon-time',
                    date: 'icon-calendar',
                    up: 'icon-chevron-up',
                    down:'icon-chevron-down',
                    next: 'icon-chevron-right',
                    previous: 'icon-chevron-left'
                }
            }).on('dp.change', _.bind(function(ev) {
                this.setMaxDate(ev.date);
            }, this));

        },

        setMinDate: function(date) {
            this.customMinDate = date;

            this.queryModel.set({
                minDate: date,
                dateRange: QueryModel.DateRange.custom
            });
        },

        setMaxDate: function(date) {
            this.customMaxDate = date;

            this.queryModel.set({
                maxDate: date,
                dateRange: QueryModel.DateRange.custom
            });
        },

        humanDates: function(row) {
            this.queryModel.set({
                minDate: moment(row.find('[data-min]').data('min')),
                maxDate: moment(row.find('[data-max]').data('max')),
                dateRange: row.find('[data-id]').data('id')
            });
        }
    });

});
