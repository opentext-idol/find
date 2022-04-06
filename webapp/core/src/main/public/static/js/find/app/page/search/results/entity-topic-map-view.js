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
    'backbone',
    'find/app/util/topic-map-view',
    'find/app/util/range-input',
    'find/app/model/entity-collection',
    'i18n!find/nls/bundle',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/entity-topic-map-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'iCheck'
], function(_, $, Backbone, TopicMapView, RangeInput, EntityCollection, i18n, generateErrorHtml,
            template, loadingTemplate) {
    'use strict';

    const loadingHtml = _.template(loadingTemplate)({i18n: i18n, large: true});

    /**
     * @readonly
     * @enum {String}
     */
    const ViewState = {
        LOADING: 'LOADING',
        ERROR: 'ERROR',
        EMPTY: 'EMPTY',
        MAP: 'MAP'
    };

    const SPEED_SLIDER_MIN = 50;
    const DEFAULT_MAX_RESULTS = 300;

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function(options) {
            this.queryState = options.queryState;

            this.entityCollection = new EntityCollection([], {
                getSelectedRelatedConcepts: function() {
                    // Comparison topic view does not have queryState
                    return this.queryState
                        ? _.flatten(this.queryState.conceptGroups.pluck('concepts'))
                        : [];
                }.bind(this)
            });

            this.debouncedFetchRelatedConcepts = _.debounce(this.entityCollection.fetchRelatedConcepts.bind(this.entityCollection), 500);

            this.queryModel = options.queryModel;
            this.type = options.type;
            this.showSlider = _.isUndefined(options.showSlider) || options.showSlider;
            this.fixedHeight = _.isUndefined(options.fixedHeight) || options.fixedHeight;

            this.topicMap = new TopicMapView({
                clickHandler: options.clickHandler
            });

            this.errorTemplate = generateErrorHtml({messageToUser: i18n['search.topicMap.error']});

            this.viewModel = new Backbone.Model({
                state: ViewState.EMPTY
            });

            const configuredMaxResults = options.configuration && _.isNumber(options.configuration.topicMapMaxResults)
                ? Math.max(options.configuration.topicMapMaxResults, SPEED_SLIDER_MIN + 1)
                : 0;

            const constructorMaxResults = _.isNumber(options.maxResults)
                ? Math.max(options.maxResults, SPEED_SLIDER_MIN + 1)
                : 0;

            this.maximumMaxResults = constructorMaxResults || configuredMaxResults || DEFAULT_MAX_RESULTS;

            this.model = new Backbone.Model({
                maxCount: 10,
                value: constructorMaxResults || Math.min(this.maximumMaxResults, DEFAULT_MAX_RESULTS)
            });

            if(this.showSlider) {
                this.slider = new RangeInput({
                    leftLabel: i18n['search.topicMap.fast'],
                    max: this.maximumMaxResults,
                    min: SPEED_SLIDER_MIN,
                    model: this.model,
                    rightLabel: i18n['search.topicMap.accurate'],
                    step: 1
                })
            }

            this.listenTo(this.model, 'change:value', function() {
                this.debouncedFetchRelatedConcepts(this.queryModel, this.type, this.model.get('value'));
            });

            this.listenTo(this.queryModel, 'change', function() {
                this.entityCollection.fetchRelatedConcepts(this.queryModel, this.type, this.model.get('value'));
            });

            this.listenTo(this.entityCollection, 'sync', function() {
                this.viewModel.set('state', this.entityCollection.isEmpty()
                    ? ViewState.EMPTY
                    : ViewState.MAP);
                this.updateTopicMapData();
                this.update();
            });

            this.listenTo(this.entityCollection, 'request', function() {
                this.viewModel.set('state', ViewState.LOADING);
            });

            this.listenTo(this.entityCollection, 'error', function(collection, xhr) {
                this.generateErrorMessage(xhr);
                // Status of zero means the request has been aborted
                this.viewModel.set('state', xhr.status === 0
                    ? ViewState.LOADING
                    : ViewState.ERROR);
            });

            this.listenTo(this.viewModel, 'change', this.updateViewState);
            this.entityCollection.fetchRelatedConcepts(this.queryModel, this.type, this.model.get('value'));
        },

        render: function() {
            this.$el.html(this.template({
                cid: this.cid,
                errorTemplate: this.errorTemplate,
                i18n: i18n,
                loadingHtml: loadingHtml,
                showSlider: this.showSlider,
                fixedHeight: this.fixedHeight
            }));

            this.$error = this.$('.entity-topic-map-error');
            this.$empty = this.$('.entity-topic-map-empty');
            this.$loading = this.$('.entity-topic-map-loading');

            if(this.showSlider) {
                this.slider.setElement(this.$('.slider-block')).render();
            }

            this.topicMap.setElement(this.$('.entity-topic-map')).render();
            this.update();
            this.updateViewState();
        },

        remove: function() {
            if(this.showSlider) {
                this.slider.remove();
            }

            Backbone.View.prototype.remove.call(this);
        },

        update: function() {
            this.topicMap.draw();
        },

        updateTopicMapData: function() {
            this.topicMap.setData(this.entityCollection.processDataForTopicMap());
        },

        updateViewState: function() {
            const state = this.viewModel.get('state');
            this.topicMap.$el.toggleClass('hide', state !== ViewState.MAP);

            if(this.$error) {
                if(state === ViewState.ERROR) {
                    this.$error.html(this.errorTemplate);
                }
                this.$error.toggleClass('hide', state !== ViewState.ERROR);
            }

            if(this.$empty) {
                this.$empty.toggleClass('hide', state !== ViewState.EMPTY);
            }

            if(this.$loading) {
                this.$loading.toggleClass('hide', state !== ViewState.LOADING);
            }
        },

        generateErrorMessage: function(xhr) {
            this.errorTemplate = generateErrorHtml(
                _.extend(
                    {messageToUser: i18n['search.topicMap.error']},
                    xhr.responseJSON
                        ? {
                            errorDetails: xhr.responseJSON.message,
                            errorDetailsFallback: xhr.responseJSON.uuid,
                            errorUUID: xhr.responseJSON.uuid,
                            errorLookup: xhr.responseJSON.backendErrorCode,
                            isUserError: xhr.responseJSON.isUserError
                        }
                        : {}
                )
            );
        }
    });
});
