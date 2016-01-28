/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'find/app/model/query-model',
    'find/app/model/query-text-model',
    'find/app/page/search/input-view',
    'find/app/page/search/service-view',
    'find/app/router',
    'find/app/vent',
    'underscore',
    'text!find/templates/app/page/find-search.html'
], function(BasePage, QueryModel, QueryTextModel, InputView, ServiceView, router, vent, _, template) {

    var reducedClasses = 'reverse-animated-container col-sm-offset-1 col-md-offset-2 col-lg-offset-3 col-xs-12 col-sm-10 col-md-8 col-lg-6';
    var expandedClasses = 'animated-container col-sm-offset-1 col-md-offset-2 col-xs-12 col-sm-10 col-md-7';

    return BasePage.extend({
        className: 'search-page',
        template: _.template(template),

        // will be overridden
        constructServiceView: function (model, queryTextModel) {
            return new ServiceView({
                queryModel: model,
                queryTextModel: queryTextModel
            });
        },

        initialize: function() {
            this.queryModel = new QueryModel();
            this.queryTextModel = new QueryTextModel();

            this.listenTo(this.queryModel, 'change:queryText', this.expandedState);

            this.listenTo(this.queryTextModel, 'change', function() {
                this.queryModel.set({
                    autoCorrect: true,
                    queryText: this.queryTextModel.makeQueryText()
                });
            });

            this.inputView = new InputView({model: this.queryTextModel});

            this.serviceView = this.constructServiceView(this.queryModel, this.queryTextModel);

            router.on('route:search', function(text, concepts) {
                this.queryTextModel.set({
                    inputText: text || '',
                    relatedConcepts: concepts ? concepts.split('/') : []
                });
            }, this);
        },

        render: function() {
            this.$el.html(this.template);

            this.inputView.setElement(this.$('.input-view-container')).render();
            this.serviceView.setElement(this.$('.service-view-container')).render();

            this.reducedState();
        },

        generateURL: function() {
            var inputQuery = this.queryTextModel.get('inputText');

            if (inputQuery){
                inputQuery = encodeURIComponent(inputQuery) + '/';
            }

            var relatedConcepts = this.queryTextModel.get('relatedConcepts');

            return inputQuery + relatedConcepts.join('/');
        },

        expandedState: function() {
            /*fancy animation*/
            this.$('.find').removeClass(reducedClasses).addClass(expandedClasses);

            this.$('.service-view-container').show();
            this.$('.app-logo').hide();
            this.$('.hp-logo-footer').addClass('hidden');

            // TODO: somebody else needs to own this
            $('.find-banner-container').removeClass('reduced navbar navbar-static-top').find('>').show();
            $('.container-fluid, .find-logo-small').removeClass('reduced');

            vent.navigate('find/search/' + this.generateURL(), {trigger: false});
        },

        reducedState: function() {
            /*fancy reverse animation*/
            this.$('.find').removeClass(expandedClasses).addClass(reducedClasses);

            this.$('.service-view-container').hide();
            this.$('.app-logo').show();
            this.$('.hp-logo-footer').removeClass('hidden');

            // TODO: somebody else needs to own this
            $('.find-banner-container').addClass('reduced navbar navbar-static-top').find('>').hide();
            $('.container-fluid, .find-logo-small').addClass('reduced');

            vent.navigate('find/search', {trigger: false});
        }
    });
});
