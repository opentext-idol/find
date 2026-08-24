/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const $ = require('jquery');
const Modal = require('find/app/util/modal');
const SearchToCompare = require('find/idol/app/page/search/comparison/search-to-compare-view');
const ComparisonModel = require('find/idol/app/model/comparison/comparison-model');
const SavedSearchModel = require('find/app/model/saved-searches/saved-search-model');
const compareModalFooter = require('find/idol/templates/comparison/compare-modal-footer.html');
const loadingSpinnerTemplate = require('find/templates/app/page/loading-spinner.html');
const comparisonsI18n = require('find/idol/nls/comparisons');
const i18n = require('find/nls/bundle');

function getSearchModelWithDefault(savedSearchCollection, sharedSavedSearchCollection, queryStates) {
    return function(cid) {
        let search = savedSearchCollection.get(cid) || sharedSavedSearchCollection.get(cid);

        const queryState = queryStates.get(cid);
        if (search.isNew() || queryState && !search.equalsQueryState(queryState)) {
            search = new SavedSearchModel(_.extend({},
                search.pick('type', 'title', 'queryStateTokens', 'promotionsStateTokens'),
                SavedSearchModel.attributesFromQueryState(queryState)
            ));
        }

        return search;
    };
}

module.exports = Modal.extend({
        footerTemplate: _.template(compareModalFooter),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),

        initialize: function(options) {
            this.comparisonSuccessCallback = options.comparisonSuccessCallback;
            const savedSearchCollection = options.savedSearchCollection;
            const queryStates = options.queryStates;
            const getSearchModel = getSearchModelWithDefault(
                savedSearchCollection, options.sharedSavedSearchCollection, queryStates);

            this.selectedId = null;

            const initialSearch = getSearchModel(options.cid);

            this.searchToCompare = new SearchToCompare({
                savedSearchCollection: savedSearchCollection,
                selectedSearch: initialSearch,
                originalSelectedModelCid: options.cid
            });

            Modal.prototype.initialize.call(this, {
                actionButtonClass: 'button-primary disabled not-clickable',
                actionButtonText: comparisonsI18n['compare'],
                secondaryButtonText: i18n['app.cancel'],
                contentView: this.searchToCompare,
                title: comparisonsI18n['search.compare.compareSaved'],
                actionButtonCallback: _.bind(function() {
                    this.$errorMessage.text('');
                    this.$loadingSpinner.removeClass('hide');
                    this.$confirmButton.prop('disabled', true);

                    const secondSearch = getSearchModel(this.selectedId);

                    const searchModels = {
                        first: initialSearch,
                        second: secondSearch
                    };

                    const comparisonModel = ComparisonModel.fromModels(searchModels.first, searchModels.second);

                    this.xhr = comparisonModel.save({}, {
                        success: _.bind(function() {
                            this.comparisonSuccessCallback(comparisonModel, searchModels);
                            this.hide();
                        }, this),
                        error: _.bind(function() {
                            this.$errorMessage.text(comparisonsI18n['error.default']);
                            this.$loadingSpinner.addClass('hide');
                            this.$confirmButton.prop('disabled', false);
                        }, this)
                    });
                }, this)
            });

            this.listenTo(this.searchToCompare, 'selected', function(selectedId) {
                this.selectedId = selectedId;
                this.$('.modal-action-button').toggleClass('disabled not-clickable', !this.selectedId);
            });
        },

        render: function() {
            Modal.prototype.render.call(this);

            this.$('.modal-footer').prepend(this.footerTemplate);

            this.$confirmButton = this.$('.modal-action-button');
            this.$errorMessage = this.$('.comparison-create-error-message');
            this.$loadingSpinner = this.$('.compare-modal-error-spinner');

            $(this.loadingTemplate)
                .addClass('inline-block')
                .appendTo(this.$loadingSpinner);
        },

        remove: function() {
            if(this.xhr) {
                this.xhr.abort();
            }

            if(this.searchToCompare) {
                this.searchToCompare.remove();
            }

            Modal.prototype.remove.call(this);
        }
    });

