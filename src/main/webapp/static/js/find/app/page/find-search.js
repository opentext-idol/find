/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'find/app/model/backbone-query-model',
    'find/app/model/query-model',
    'find/app/model/indexes-collection',
    'find/app/page/input-view',
    'find/app/page/service-view',
    'find/app/router',
    'find/app/vent',
    'underscore',
    'text!find/templates/app/page/find-search.html'
], function(BasePage, BackboneQueryModel, QueryModel, IndexesCollection, InputView, ServiceView, router, vent, _, template) {

    return BasePage.extend({

        template: _.template(template),

        initialize: function() {
            var backboneQueryModel = new BackboneQueryModel();
            this.queryModel = new QueryModel(backboneQueryModel);

            var indexesCollection = new IndexesCollection();
            indexesCollection.fetch();

            // Because the queryModel doesn't fire with the empty string, we listen to the unadulterated model
            // underlying the main model for the change in queryText.
            this.listenTo(backboneQueryModel, 'change:queryText', function(model, text) {
                this.$('.find-input').val(text); //when clicking one of the suggested search links

                this.uiStateChange(text);

                vent.navigate('find/search/' + encodeURIComponent(text), {trigger: false});
            });

            this.inputView = new InputView({
                indexesCollection: indexesCollection,
                queryModel: this.queryModel
            });

            this.serviceView = new ServiceView({
                indexesCollection: indexesCollection,
                queryModel: this.queryModel
            });

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

            this.inputView.setElement(this.$('.input-view-container')).render();
            this.serviceView.setElement(this.$('.service-view-container')).render();

            this.reducedState();
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

            this.$('.service-view-container').show();
        },

        reducedState: function() {
            /*fancy reverse animation*/
            this.$('.find').removeClass('animated-container').addClass('reverse-animated-container');

            this.$('.service-view-container').hide();

            vent.navigate('find/search', {trigger: false});
        }
    });
});
