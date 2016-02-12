/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'js-whatever/js/base-page',
    'find/app/model/document-model',
    'find/app/model/indexes-collection',
    'find/app/model/query-model',
    'find/app/model/query-text-model',
    'find/app/page/search/input-view',
    'find/app/router',
    'find/app/vent',
    'underscore',
    'text!find/templates/app/page/find-search.html'
], function ($, Backbone, BasePage, DocumentModel, IndexesCollection, QueryModel, QueryTextModel, InputView, router, vent, _, template) {
    "use strict";

    var reducedClasses = 'reverse-animated-container col-sm-offset-1 col-md-offset-2 col-lg-offset-3 col-xs-12 col-sm-10 col-md-8 col-lg-6';
    var expandedClasses = 'animated-container col-sm-offset-1 col-md-offset-2 col-xs-12 col-sm-10 col-md-7';

    var DocumentContentModel = DocumentModel.extend({
        url: '../api/public/search/get-document-content'
    });

    return BasePage.extend({
        className: 'search-page',
        template: _.template(template),

        // will be overridden
        QueryServiceView: null,
        SuggestServiceView: null,
        suggestCallback: null,

        initialize: function () {
            this.queryModel = new QueryModel();
            this.queryTextModel = new QueryTextModel();

            this.listenTo(this.queryModel, 'change:queryText', this.expandedState);

            this.listenTo(this.queryTextModel, 'change', function () {
                this.queryModel.set({
                    autoCorrect: true,
                    queryText: this.queryTextModel.makeQueryText()
                });

                var searchUrl = 'find/search/query/' + this.generateURL();
                vent.navigate(searchUrl);
            });

            this.listenTo(this.queryTextModel, 'refresh', function () {
                var searchUrl = 'find/search/query/' + this.generateURL();
                vent.navigate(searchUrl);
            });

            this.inputView = new InputView({model: this.queryTextModel});

            this.indexesCollection = new IndexesCollection();
            this.indexesCollection.fetch();

            this.queryServiceView = new this.QueryServiceView({
                queryModel: this.queryModel,
                queryTextModel: this.queryTextModel,
                indexesCollection: this.indexesCollection
            });

            router.on('route:emptySearch', this.reducedState, this);

            router.on('route:search', function(text, concepts) {
                this.queryTextModel.set({
                    inputText: text || '',
                    relatedConcepts: concepts ? concepts.split('/') : []
                });

                this.$('.query-service-view-container').removeClass('hide');
                this.$('.suggest-service-view-container').addClass('hide');
            }, this);

            router.on('route:suggest', function () {
                this.expandedState();
                var suggestOptions = this.suggestOptions.apply(this, arguments);
                this.suggest(suggestOptions);
            }, this);
        },

        render: function () {
            this.$el.html(this.template);

            this.inputView.setElement(this.$('.input-view-container')).render();
            this.queryServiceView.setElement(this.$('.query-service-view-container')).render();

            this.reducedState();
        },

        generateURL: function () {
            var inputQuery = this.queryTextModel.get('inputText');

            if (inputQuery) {
                inputQuery = encodeURIComponent(inputQuery) + '/';
            }

            var relatedConcepts = this.queryTextModel.get('relatedConcepts');

            return inputQuery + relatedConcepts.join('/');
        },

        expandedState: function () {
            /*fancy animation*/
            this.$('.find').removeClass(reducedClasses).addClass(expandedClasses);

            this.$('.query-service-view-container').show();
            this.$('.app-logo').hide();
            this.$('.hp-logo-footer').addClass('hidden');

            // TODO: somebody else needs to own this
            $('.find-banner-container').removeClass('reduced navbar navbar-static-top').find('>').show();
            $('.container-fluid, .find-logo-small').removeClass('reduced');
        },

        reducedState: function () {
            /*fancy reverse animation*/
            this.$('.find').removeClass(expandedClasses).addClass(reducedClasses);

            this.$('.query-service-view-container').hide();
            this.$('.suggest-service-view-container').addClass('hide');
            this.$('.app-logo').show();
            this.$('.hp-logo-footer').removeClass('hidden');

            // TODO: somebody else needs to own this
            $('.find-banner-container').addClass('reduced navbar navbar-static-top').find('>').hide();
            $('.container-fluid, .find-logo-small').addClass('reduced');
        },

        suggest: function (suggestOptions) {
            var self = this;

            var model = new DocumentContentModel();

            model
                .fetch({
                    data: {
                        reference: suggestOptions.reference,
                        database: suggestOptions.database
                    }
                })
                .done(function () {
                    var queryModel = new QueryModel();

                    queryModel.set(_.extend({
                        document: model
                    }, suggestOptions.suggestParams));

                    var queryPathSuffix = self.generateURL();
                    var suggestServiceView = new self.SuggestServiceView({
                        queryModel: queryModel,
                        indexesCollection: self.indexesCollection,
                        backUrl: queryPathSuffix ? 'find/search/query/' + queryPathSuffix : 'find/search/query'
                    });

                    self.$('.query-service-view-container').addClass('hide');

                    var container = self.$('.suggest-service-view-container');
                    container.removeClass('hide');
                    suggestServiceView.setElement(container).render();
                });
        }
    });
});
