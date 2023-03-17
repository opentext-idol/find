/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'jquery',
    'find/app/page/search/abstract-section-view',
    './concept-cluster-view',
    'find/app/page/search/input-view',
    'find/app/page/search/input-view-concept-strategy',
    'find/app/util/filtering-collection',
    'i18n!find/nls/bundle',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/search/concept-view.html',
    'iCheck'
], function(_, $, AbstractSectionView, ConceptClusterView, InputView, conceptStrategy, FilteringCollection,
            i18n, ListView, template) {
    'use strict';

    /**
     * View for displaying the selected concept groups eg in the left side panel.
     * Expects to be given the queryState on construction.
     */
    return AbstractSectionView.extend({
        html: _.template(template)({i18n: i18n}),

        events: {
            'click .concept-remove-icon': function(event) {
                const cid = $(event.currentTarget).closest('.selected-related-concept').attr('data-cluster-cid');
                this.conceptGroups.remove(cid);
            },

            'ifChanged .concept-view-crosslingual-ontology': function (event) {
                this.queryState.crosslingualOntology.set({ enabled: event.target.checked });
            },

            'ifChanged .concept-view-crosslingual-index': function (event) {
                this.queryState.crosslingualIndex.set({ enabled: event.target.checked });
            }
        },

        initialize: function(options) {
            AbstractSectionView.prototype.initialize.apply(this, arguments);

            this.queryState = options.queryState;
            this.conceptGroups = options.queryState.conceptGroups;

            const optionalViews = [{
                enabled: options.configuration.hasBiRole,
                selector: '.concept-view-container',
                construct: function() {
                    return new InputView({
                        enableTypeAhead: options.configuration.enableTypeAhead,
                        strategy: conceptStrategy(options.queryState.conceptGroups)
                    });
                },
                onRender: function(view) {
                    view.focus();
                }
            }];

            this.optionalViews = _.where(optionalViews, {enabled: true});

            this.optionalViews.forEach(function(view) {
                view.instance = view.construct();
            });

            this.filteringCollection = new FilteringCollection([], {
                collection: this.conceptGroups,
                predicate: function(model) {
                    return !model.has('hidden');
                }
            });
            this.listView = new ListView({
                collection: this.filteringCollection,
                ItemView: ConceptClusterView,
                collectionChangeEvents: {
                    concepts: 'updateConcepts'
                }
            });
            this.crosslingualView = null;

            this.listenTo(this.conceptGroups, 'update reset', this.updateEmpty);
            this.listenTo(this.queryState.crosslingualOntology, 'change',
                this.updateCrosslingualOntology);
            this.listenTo(this.queryState.crosslingualIndex, 'change',
                this.updateCrosslingualIndex);
        },

        render: function() {
            AbstractSectionView.prototype.render.apply(this);

            this.getViewContainer().html(this.html);

            this.optionalViews.forEach(function(view) {
                view.instance.setElement(this.$(view.selector)).render();
                view.onRender(view.instance);
            }, this);

            this.listView.setElement(this.$('.concept-view-concepts')).render();

            this.$('.concept-view-crosslingual-ontology').iCheck({ checkboxClass: 'icheckbox-hp' });
            this.$('.concept-view-crosslingual-index').iCheck({ checkboxClass: 'icheckbox-hp' });

            this.updateEmpty();
            this.updateCrosslingualOntology();
            this.updateCrosslingualIndex();
        },

        updateEmpty: function() {
            const empty = this.filteringCollection.isEmpty();
            this.listView.$el.toggleClass('hide', empty);
            this.$('.concept-view-empty-message').toggleClass('hide', !empty);
        },

        updateCrosslingualOntology: function () {
            this.$('.concept-view-crosslingual-ontology').iCheck(
                this.queryState.crosslingualOntology.get('enabled') ? 'check' : 'uncheck');
        },

        updateCrosslingualIndex: function () {
            this.$('.concept-view-crosslingual-index').iCheck(
                this.queryState.crosslingualIndex.get('enabled') ? 'check' : 'uncheck');
        },

        remove: function() {
            _.chain(this.optionalViews).pluck('instance').invoke('remove');

            this.listView.remove();
            AbstractSectionView.prototype.remove.call(this);
        }
    });
});
