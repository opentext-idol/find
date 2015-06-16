/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'find/app/model/backbone-query-model',
    'find/app/model/query-model',
    'find/app/model/entity-collection',
    'find/app/model/indexes-collection',
    'find/app/page/indexes/indexes-view',
    'find/app/model/parametric-collection',
    'find/app/page/parametric/parametric-controller',
    'find/app/page/date/dates-filter-view',
    'find/app/page/results/results-view',
    'find/app/page/related-concepts/related-concepts-view',
    'find/app/util/collapsible',
    'find/app/router',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'moment',
    'jquery',
    'underscore',
    'text!find/templates/app/page/find-search.html',
    'text!find/templates/app/page/index-popover.html',
    'text!find/templates/app/page/index-popover-contents.html',
    'text!find/templates/app/page/top-results-popover-contents.html',
    'text!find/templates/app/util/filter-header.html',
    'colorbox'
], function(BasePage, BackboneQueryModel, QueryModel, EntityCollection, IndexesCollection, IndexesView, ParametricCollection,
            ParametricController, DateView, ResultsView, RelatedConceptsView, Collapsible, router, vent, i18n, moment,
            $, _, template, indexPopover, indexPopoverContents, topResultsPopoverContents, filterHeader) {

    var filterHeaderTemplate = _.template(filterHeader);

    var collapseView = function(title, collapseParameter, view) {
        return new Collapsible({
            header: filterHeaderTemplate({
                title: i18n[title]
            }),
            name: collapseParameter,
            view: view
        });
    };

    return BasePage.extend({

        template: _.template(template),
        topResultsPopoverContents: _.template(topResultsPopoverContents),

        events: {
            'keyup .find-input': _.debounce(function() {
                var findInput = this.$('.find-input').val();
                this.queryModel.set('queryText', findInput);
            }, 500),
            'mouseover .entity-to-summary': function(e) {
                var title = $(e.currentTarget).find('a').html();
                this.$('[data-title="'+ title +'"]').addClass('label label-primary entity-to-summary').removeClass('label-info');
            },
            'mouseleave .entity-to-summary': function() {
                this.$('.suggestions-content li a').removeClass('label label-primary entity-to-summary');
                this.$('.main-results-content .entity-to-summary').removeClass('label-primary').addClass('label-info');
            }
        },

        initialize: function() {
            this.queryModel = new QueryModel(new BackboneQueryModel());

            // The query model fires and triggers results only when it has both queryText and indexes
            this.listenTo(this.queryModel, 'change', function() {
                this.searchRequest();
            });

            // Because the queryModel doesn't fire with the empty string, we listen to the unadulterated model
            // underlying the main model for the change in queryText.
            this.listenTo(this.queryModel.model, 'change:queryText', function(model, text){
                this.uiStateChange(text);
            });

            this.entityCollection = new EntityCollection();

            this.indexesView = new IndexesView({
                queryModel: this.queryModel
            });

            this.resultsView = new ResultsView({
                queryModel: this.queryModel,
                entityCollection: this.entityCollection
            });

            // Left Collapsed Views
            this.parametricController = new ParametricController({
                queryModel: this.queryModel
            });

            this.dateView = new DateView({
                queryModel: this.queryModel
            });

            this.relatedConceptsView = new RelatedConceptsView({
                queryModel: this.queryModel,
                entityCollection: this.entityCollection
            });

            // Collapse wrappers

            this.parametricViewWrapper = collapseView('parametric.title', 'parametric-filter', this.parametricController.view);
            this.dateViewWrapper = collapseView('search.dates', 'dates-filter', this.dateView);
            this.relatedConceptsViewWrapper = collapseView('search.relatedConcepts', 'related-concepts', this.relatedConceptsView);

            router.on('route:search', function(text) {
                if (text) {
                    this.$('.find-input').val(text); //when clicking one of the suggested search links
                    this.queryModel.set('queryText', text);
                } else {
                    this.queryModel.set('queryText', ''); //when clicking the small 'find' logo
                }
            }, this);
        },

        render: function() {
            this.$el.html(this.template);

            this.$('.find-form').submit(function(e){ //preventing input form submit and page reload
                e.preventDefault();
            });

            this.indexesView.setElement(this.$('.indexes-container')).render();

            this.parametricViewWrapper.setElement(this.$('.parametric-container')).render();
            this.dateViewWrapper.setElement(this.$('.date-container')).render();

            this.relatedConceptsViewWrapper.render();

            this.$('.related-concepts-container').append(this.relatedConceptsViewWrapper.$el);

            this.resultsView.setElement(this.$('.results-container')).render();

            this.reducedState();
        },

        searchRequest: function() {
            this.$('.find-input').val(this.queryModel.get('queryText')); //when clicking one of the suggested search links

            this.entityCollection.fetch({
                data: {
                    text: this.queryModel.get('queryText'),
                    index: this.queryModel.get('indexes'),
                    field_text: this.queryModel.getFieldTextString()
                }
            });

            vent.navigate('find/search/' + encodeURIComponent(this.queryModel.get('queryText')), {trigger: false});
        },

        uiStateChange: function(text) {
            if(text.length) { // input has at least one non whitespace character
                this.expandedState();
            } else {
                this.reducedState();
            }
        },

        expandedState: function() {
            /*fancy animation*/
            this.$('.find').addClass('animated-container').removeClass('reverse-animated-container');

            this.$('.main-results-content').show();
            this.$('.related-concepts-container').show();
            this.$('.parametric-container').show();
            this.$('.date-container').show();
        },

        reducedState: function() {
            /*fancy reverse animation*/
            this.$('.find').removeClass('animated-container').addClass('reverse-animated-container');

            this.$('.main-results-content').hide();
            this.$('.related-concepts-container').hide();
            this.$('.parametric-container').hide();
            this.$('.date-container').hide();

            vent.navigate('find/search', {trigger: false});
        }
    });
});
