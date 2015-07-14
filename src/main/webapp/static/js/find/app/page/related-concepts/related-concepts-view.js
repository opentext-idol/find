define([
    'backbone',
    'find/app/model/documents-collection',
    'text!find/templates/app/page/suggestions-container.html',
    'text!find/templates/app/page/top-results-popover-contents.html',
    'text!find/templates/app/page/loading-spinner.html'
], function(Backbone, DocumentsCollection, suggestionsTemplate, topResultsPopoverContents, loadingSpinnerTemplate) {

    return Backbone.View.extend({

        className: 'suggestions-content',

        suggestionsTemplate: _.template(suggestionsTemplate),
        topResultsPopoverContents: _.template(topResultsPopoverContents),

        events: {
            'mouseover a': _.debounce(function(e) {
                this.$(' .popover-content').append(_.template(loadingSpinnerTemplate));

                this.topResultsCollection.fetch({
                    data: {
                        text: $(e.currentTarget).html(),
                        max_results: 3,
                        summary: 'context',
                        index: this.queryModel.get('indexes')
                    }
                });
            }, 800),
            'click .query-text' : function(e) {
                var $target = $(e.target);
                var queryText = $target.attr('data-title');
                this.queryModel.set('queryText', queryText);
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.entityCollection = options.entityCollection;

            this.topResultsCollection = new DocumentsCollection();
        },

        render: function() {
            this.listenTo(this.entityCollection, 'reset', function() {
                this.$el.empty();

                if (this.entityCollection.isEmpty()) {
                    this.$el.addClass('hide')
                }
                else {
                    var clusters = this.entityCollection.groupBy('cluster');

                    _.each(clusters, function(entities) {
                        this.$el.append(this.suggestionsTemplate({
                            entities: entities
                        }));

                        this.$('li a').popover({
                            html: true,
                            placement: 'bottom',
                            trigger: 'hover'
                        })
                    }, this);
                }
            });

            /*top 3 results popover*/
            this.listenTo(this.topResultsCollection, 'add', function(model){
                this.$('.popover-content .loading-spinner').remove();

                this.$('.popover-content').append(this.topResultsPopoverContents({
                    title: model.get('title'),
                    summary: model.get('summary').trim().substring(0, 100) + "..."
                }));
            });

            /*suggested links*/
            this.listenTo(this.entityCollection, 'request', function() {
                if(!this.$('ul').length) {
                    this.$el.append(_.template(loadingSpinnerTemplate));
                }

                this.$el.removeClass('hide');
            });
        }

    })

});
