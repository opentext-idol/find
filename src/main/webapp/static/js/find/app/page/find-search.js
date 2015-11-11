/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'find/app/model/backbone-query-model',
    'find/app/model/query-model',
    'find/app/page/input-view',
    'find/app/page/service-view',
    'find/app/router',
    'find/app/vent',
    'underscore',
    'text!find/templates/app/page/find-search.html'
], function(BasePage, BackboneQueryModel, QueryModel, InputView, ServiceView, router, vent, _, template) {

    return BasePage.extend({
        className: 'search-page',
        template: _.template(template),

        initialize: function() {
            var backboneQueryModel = new BackboneQueryModel();
            this.queryModel = new QueryModel(backboneQueryModel);

            // Because the queryModel doesn't fire with the empty string, we listen to the unadulterated model
            // underlying the main model for the change in queryText.
            this.listenTo(backboneQueryModel, 'change:queryText', function(model, text) {
                this.$('.find-input').val(text); //when clicking one of the suggested search links

                if(text.length) { // input has at least one non whitespace character
                    this.expandedState();
                }

            });

            this.inputView = new InputView({
                queryModel: this.queryModel
            });

            this.serviceView = new ServiceView({
                queryModel: this.queryModel
            });

            router.on('route:search', function(text) {
                if (text) {
                    this.$('.find-input').val(text); //when clicking one of the suggested search links
                    this.queryModel.set('queryText', text);
                } else {
                    this.queryModel.set('queryText', '');
                }
            }, this);
        },

        render: function() {
            this.$el.html(this.template);

            this.inputView.setElement(this.$('.input-view-container')).render();
            this.serviceView.setElement(this.$('.service-view-container')).render();

            this.reducedState();
        },

        expandedState: function() {
            /*fancy animation*/
            this.$('.find').addClass('animated-container col-md-offset-2').removeClass('reverse-animated-container col-md-offset-3');

            this.$('.service-view-container').show();
            this.$('.app-logo').hide();
            $('.find-navbar').addClass('visible');
            $('.find-banner').addClass('hidden');
            $('.hp-logo-footer').removeClass('hidden');

            vent.navigate('find/search/' + encodeURIComponent(this.queryModel.get('queryText')), {trigger: false});
        },

        reducedState: function() {
            /*fancy reverse animation*/
            this.$('.find').removeClass('animated-container col-md-offset-2').addClass('reverse-animated-container col-md-offset-3');

            this.$('.service-view-container').hide();
            this.$('.app-logo').show();
            $('.find-navbar').removeClass('visible');
            $('.find-banner').removeClass('hidden');
            $('.hp-logo-footer').addClass('hidden');

            vent.navigate('find/search', {trigger: false});
        }
    });
});
