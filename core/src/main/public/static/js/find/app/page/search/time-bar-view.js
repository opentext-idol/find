define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'find/app/model/bucketed-parametric-collection',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-date-parametric-field-view.html'
], function(Backbone, _, $, vent, i18n, NumericParametricFieldView, BucketedParametricCollection, loadingSpinnerTemplate, numericDateTemplate) {

    var FIELD_NAME = 'autn_date';
    var PIXELS_PER_BUCKET = 20;

    return Backbone.View.extend({
        graphView: null,
        loadingSpinnerHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),
        errorHtml: _.template('<p class="p-t-xl text-center"><%-i18n["search.timeBar.error"]%></p>')({i18n: i18n}),

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.queryState.selectedParametricValues;
            this.dateParametricFieldsCollection = options.dateParametricFieldsCollection;

            this.listenTo(vent, 'vent:resize', this.render);
        },

        render: function() {
            this.destroyGraph();
            this.abortActiveRequest();

            if (this.bucketModel) {
                this.stopListening(this.bucketModel);
            }

            var $loadingSpinner = $(this.loadingSpinnerHtml);
            this.$el.empty().append($loadingSpinner);

            this.bucketModel = new BucketedParametricCollection.Model({id: FIELD_NAME, name: FIELD_NAME});

            this.activeRequest = this.bucketModel
                .fetch({
                    data: {
                        targetNumberOfBuckets: Math.floor(this.$el.width() / PIXELS_PER_BUCKET)
                    }
                })
                .fail(function() {
                    this.$el.append(this.errorHtml);
                }.bind(this))
                .done(function() {
                    this.graphView = new NumericParametricFieldView({
                        buttonsEnabled: true,
                        pixelsPerBucket: PIXELS_PER_BUCKET,
                        queryModel: this.queryModel,
                        selectionEnabled: true,
                        selectedParametricValues: this.selectedParametricValues,
                        template: numericDateTemplate,
                        zoomEnabled: true,
                        model: this.bucketModel,
                        stringFormatting: NumericParametricFieldView.dateFormatting,
                        // 20px for padding
                        viewWidth: this.$el.width() - 20
                    });

                    this.$el.append(this.graphView.$el);
                    this.graphView.render();
                    this.listenTo(this.bucketModel, 'change', this.graphView.render.bind(this.graphView));
                }.bind(this))
                .always(function() {
                    $loadingSpinner.remove();
                }.bind(this));
        },

        abortActiveRequest() {
            if (this.activeRequest) {
                this.activeRequest.abort();
            }
        },

        destroyGraph: function() {
            if (this.graphView) {
                this.graphView.remove();
                this.graphView = null;
            }
        },

        remove: function() {
            this.abortActiveRequest();
            this.destroyGraph();
            Backbone.View.prototype.remove.call(this);
        }
    });

});
