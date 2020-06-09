/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'backbone',
    'jquery',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'find/app/util/popover',
    'find/app/util/search-data-util',
    'find/app/util/view-state-selector',
    'find/app/page/search/results/add-links-to-summary',
    'text!find/templates/app/page/search/related-concepts/related-concepts-view.html',
    'text!find/templates/app/page/search/related-concepts/related-concept-cluster.html',
    'text!find/templates/app/page/search/popover-message.html',
    'text!find/templates/app/page/search/results-popover.html',
    'text!find/templates/app/page/loading-spinner.html'
], function(Backbone, $, _, i18n, DocumentsCollection, popover, searchDataUtil, viewStateSelector, addLinksToSummary, viewTemplate, clusterTemplate,
            popoverMessageTemplate, popoverTemplate, loadingSpinnerTemplate) {
    'use strict';

    var html = _.template(viewTemplate)({
        i18n: i18n,
        loadingSpinnerHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false})
    });

    var clusterTemplateFunction = _.template(clusterTemplate);
    var popoverTemplateFunction = _.template(popoverTemplate);
    var popoverMessageTemplateFunction = _.template(popoverMessageTemplate);

    /**
     * @readonly
     * @enum {String}
     */
    var ViewState = {
        LIST: 'LIST',
        PROCESSING: 'PROCESSING',
        ERROR: 'ERROR',
        NONE: 'NONE',
        NOT_LOADING: 'NOT_LOADING'
    };

    function updateForViewState() {
        this.selectViewState([this.model.get('viewState')]);
    }

    function popoverHandler($content, $target) {
        var entityCluster = $target.data('entityCluster');
        var clusterEntities = _.isUndefined(entityCluster) ? [$target.data('entityText')] : _.flatten(this.entityCollection.getClusterEntities(entityCluster)).map(function(concept) {
            return '"' + concept + '"';
        });
        var relatedConcepts = _.union(this.conceptGroups.pluck('concepts'), [clusterEntities]);

        var queryText = searchDataUtil.makeQueryText(relatedConcepts);

        var topResultsCollection = new DocumentsCollection([], {
            indexesCollection: this.indexesCollection
        });

        topResultsCollection.fetch({
            reset: true,
            data: {
                field_text: this.queryModel.get('fieldText'),
                min_date: this.queryModel.getIsoDate('minDate'),
                max_date: this.queryModel.getIsoDate('maxDate'),
                text: queryText,
                max_results: 3,
                summary: 'context',
                indexes: this.queryModel.get('indexes'),
                queryType: 'MODIFIED'
            },
            error: _.bind(function() {
                $content.html(popoverMessageTemplateFunction({message: i18n['search.relatedConcepts.topResults.error']}));
            }, this),
            success: _.bind(function() {
                if(topResultsCollection.isEmpty()) {
                    $content.html(popoverMessageTemplateFunction({message: i18n['search.relatedConcepts.topResults.none']}));
                } else {
                    var oldHeight = $content.height();

                    $content.html('<ul class="list-unstyled"></ul>');
                    _.each(topResultsCollection.models, function(model) {
                        var listItem = $(popoverTemplateFunction({
                            title: model.get('title'),
                            summary: addLinksToSummary(model.get('summary')).trim().substring(0, 200) + '\u2026'
                        }));

                        $content.find('ul').append(listItem);
                    }, this);

                    var $popover = $content.closest('.popover');

                    if($popover.hasClass('top')) {
                        // we've changed the content, so the Bootstrap provided position is wrong for top positioning
                        // we need to adjust the top by the difference between the old height and the new height
                        var newHeight = $content.height();
                        var top = $popover.position().top;
                        var newTop = top - (newHeight - oldHeight);

                        $popover.css('top', newTop + 'px');
                    }
                }
            }, this)
        });
    }

    return Backbone.View.extend({
        className: 'p-l-sm suggestions-content',
        selectViewState: _.noop,

        events: {
            'click [data-entity-text]': function(e) {
                var $target = $(e.currentTarget);
                var text = $target.attr('data-entity-text');
                this.clickHandler([text]);
            },
            'click [data-entity-cluster]': function(e) {
                var $target = $(e.currentTarget);
                var queryCluster = Number($target.attr('data-entity-cluster'));
                this.clickHandler(this.entityCollection.getClusterEntities(queryCluster));
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.conceptGroups = options.queryState.conceptGroups;
            this.entityCollection = options.entityCollection;
            this.indexesCollection = options.indexesCollection;
            this.clickHandler = options.clickHandler;

            var initialViewState;

            if(this.indexesCollection.isEmpty()) {
                initialViewState = ViewState.NOT_LOADING;
            } else {
                initialViewState = this.entityCollection.isEmpty() ? ViewState.PROCESSING : ViewState.LIST;
            }

            this.model = new Backbone.Model({viewState: initialViewState});
            this.listenTo(this.model, 'change:viewState', updateForViewState);

            // Each instance of this view gets its own bound, de-bounced popover handler
            var handlePopover = _.debounce(_.bind(popoverHandler, this), 500);

            this.listenTo(this.entityCollection, 'reset update', function() {
                if(this.entityCollection.isEmpty()) {
                    if(this.indexesCollection.isEmpty()) {
                        this.model.set('viewState', ViewState.NOT_LOADING);
                    } else {
                        this.model.set('viewState', ViewState.NONE);
                    }
                } else {
                    this.model.set('viewState', ViewState.LIST);

                    var html = this.entityCollection.chain()
                        .groupBy(function(model) {
                            return model.get('cluster');
                        })
                        .map(function(models, cluster) {
                            return clusterTemplateFunction({
                                entities: _.map(models, function(model) {
                                    return model.get('text');
                                }),
                                cluster: cluster
                            });
                        })
                        .value()
                        .join('');

                    this.$list.html(html);

                    popover(this.$list.find('.entity-text'), 'hover', handlePopover);
                }
            });

            this.listenTo(this.entityCollection, 'request', function() {
                this.model.set('viewState', this.indexesCollection.isEmpty() ? ViewState.NOT_LOADING : ViewState.PROCESSING);
            });

            this.listenTo(this.entityCollection, 'error', function() {
                this.model.set('viewState', ViewState.ERROR);
            });
        },

        render: function() {
            this.$el.html(html);

            this.$list = this.$('.related-concepts-list');
            this.$error = this.$('.related-concepts-error');
            this.$none = this.$('.related-concepts-none');
            this.$notLoading = this.$('.related-concepts-not-loading');
            this.$processing = this.$('.related-concepts-processing');

            var viewStateElements = {};
            viewStateElements[ViewState.ERROR] = this.$error;
            viewStateElements[ViewState.PROCESSING] = this.$processing;
            viewStateElements[ViewState.NONE] = this.$none;
            viewStateElements[ViewState.NOT_LOADING] = this.$notLoading;
            viewStateElements[ViewState.LIST] = this.$list;

            this.selectViewState = viewStateSelector(viewStateElements);
            updateForViewState.call(this);
        }
    });
});
