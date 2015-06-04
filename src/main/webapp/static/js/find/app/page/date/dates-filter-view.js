define([
    'backbone',
    'moment',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/date/dates-filter-view.html',
    'bootstrap-datetimepicker'
], function(Backbone, moment, i18n, template) {

    return Backbone.View.extend({
        template: _.template(template),

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$minDate = this.$('.results-filter-min-date');
            this.$maxDate = this.$('.results-filter-max-date');

            this.$minDate.datetimepicker({
                format: 'yyyy/MM/dd hh:mm'
            }).on('changeDate', _.bind(function(ev) {
                this.trigger('change', {type: 'min', date: moment((ev.date)).toISOString()});
            }, this));

            this.$maxDate.datetimepicker({
                format: 'yyyy/MM/dd hh:mm'
            }).on('changeDate', _.bind(function(ev) {
                this.trigger('change', {type: 'max', date: moment((ev.date)).toISOString()});
            }, this));

        },

        setMinDate: function(date) {

        },

        setMaxDate: function(date) {

        }
    });

});
