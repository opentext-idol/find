define([
    'backbone',
    'underscore',
    'find/app/util/topic-map-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/precision-recall/precision-recall-slider-view.html',
    'slider/bootstrap-slider'
], function (Backbone, _, TopicMapView, i18n, template) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'slideStop .precision-recall-slider': function(event) {
                this.minScoreModel.set('minScore', Number(event.value));
            }
        },

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.minScoreModel = options.queryState.minScoreModel;

            this.listenTo(this.queryModel, 'change:minScore', this.updateSlider);
        },

        updateSlider: function () {
            var minScore = this.queryModel.get('minScore');
            
            if (this.$precisionSlider) {
                this.$precisionSlider
                    .slider('setValue', minScore);
            }
            
            this.minScoreModel.set('minScore', minScore);
        },

        render: function () {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$precisionSlider = this.$('.precision-recall-slider')
                .slider({
                    min: 0,
                    max: 96, // Maximum weight returned by IDOL is 96
                    tooltip: 'hide',
                    value: this.queryModel.get('minScore')
                });
        }
    });
});