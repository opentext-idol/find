define([
    'backbone',
    'moment',
    'i18n!find/nls/bundle',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/date/dates-filter-view.html',
    'text!find/templates/app/page/date/custom-datepicker.html',
    'text!find/templates/app/page/date/date-item.html',
    'bootstrap-datetimepicker'
], function(Backbone, moment, i18n, ListView, template, datepicker, dateItemTemplate) {

    return Backbone.View.extend({
        template: _.template(template),
        datepickerTemplate: _.template(datepicker),
        itemTemplate: _.template(dateItemTemplate),

        events: {
            'click .clear-min-date': function() {
                this.setMinDate(null);
                this.$minDate.find('input').val('');
            },
            'click .clear-max-date': function() {
                this.setMaxDate(null);
                this.$maxDate.find('input').val('');
            },
            'click tr': function(e) {
                this.$('.date-filters-list i').addClass('hide');
                var $targetRow = $(e.currentTarget);
                $targetRow.find('i').toggleClass('hide');

                this.$('.search-dates-wrapper').toggleClass('hide', $targetRow.find('[data-id]').data('id') != 'Custom');

                if($targetRow.find('[data-id]').data('id') != 'Custom') {
                    this.changeDates($targetRow);
                }
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;

            this.dateFiltersCollection = new Backbone.Collection([
                {
                    label: 'Last week',
                    minDate: moment(new Date(Date.now() + -7*24*3600*1000)),
                    maxDate: moment(new Date())
                },
                {
                    label: 'Last month',
                    minDate: moment(new Date(Date.now() + -30*24*3600*1000)),
                    maxDate: moment(new Date())
                },
                {
                    label: 'Last year',
                    minDate: moment(new Date(Date.now() + -365*24*3600*1000)),
                    maxDate: moment(new Date())
                },
                {
                    label: 'Custom',
                    minDate: this.$minDate,
                    maxDate: this.$maxDate
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
            this.queryModel.set('minDate', date);
        },

        setMaxDate: function(date) {
            this.queryModel.set('maxDate', date);
        },

        changeDates: function(row) {
            var minDate = moment(row.find('[data-min]').data('min'));
            var maxDate = moment(row.find('[data-max]').data('max'));

            this.setMinDate(minDate);
            this.setMaxDate(maxDate);
        }
    });

});
