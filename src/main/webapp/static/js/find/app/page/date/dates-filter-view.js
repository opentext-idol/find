define([
    'backbone',
    'moment',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/date/dates-filter-view.html',
    'bootstrap-datetimepicker'
], function(Backbone, moment, i18n, template) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .clear-min-date': function() {
                this.setMinDate(null);
                this.$minDate.find('input').val('');
            },
            'click .clear-max-date': function() {
                this.setMaxDate(null);
                this.$maxDate.find('input').val('');
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$minDate = this.$('.results-filter-min-date');
            this.$maxDate = this.$('.results-filter-max-date');

            this.$minDate.datetimepicker({
                format: 'yyyy/MM/dd hh:mm'
            }).on('changeDate', _.bind(function(ev) {
                this.setMinDate(moment((ev.date)).toISOString());
            }, this));

            this.$maxDate.datetimepicker({
                format: 'yyyy/MM/dd hh:mm'
            }).on('changeDate', _.bind(function(ev) {
                this.setMaxDate(moment((ev.date)).toISOString());
            }, this));

        },

        setMinDate: function(date) {
            this.queryModel.set('minDate', date);
        },

        setMaxDate: function(date) {
            this.queryModel.set('maxDate', date);
        }
    });

});
