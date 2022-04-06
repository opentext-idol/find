/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'js-whatever/js/list-item-view',
    'i18n!find/nls/bundle',
    'find/app/model/saved-searches/saved-search-model',
    'text!find/templates/app/page/search/search-tab-item-view.html',
    'bootstrap'
], function(_, ListItemView, i18n, SavedSearchModel, template) {
    'use strict';

    const templateFunction = _.template(template);
    const NEW_DOCS_LIMIT = 1000;

    return ListItemView.extend({
        className: 'search-tab',
        tagName: 'li',
        queryState: null,

        initialize: function(options) {
            const cid = this.model.cid;
            this.queryStates = options.queryStates;

            ListItemView.prototype.initialize.call(this, _.defaults({
                template: templateFunction,
                templateOptions: {
                    i18n: i18n,
                    searchCid: cid,
                    icon: options.searchTypes[this.model.get('type')].icon
                }
            }, options));

            this.listenTo(this.model, 'change', this.updateSavedness);
            this.listenTo(this.model, 'change:newDocuments', this.updateTabBadge);
            this.listenTo(this.queryStates, 'change:' + cid, function() {
                this.updateQueryStateListeners();
                this.updateSavedness();
            });

            this.updateQueryStateListeners();
        },

        render: function() {
            ListItemView.prototype.render.apply(this);

            if(this.$tooltip) {
                this.$tooltip.tooltip('destroy');
            }

            this.updateSavedness();
            this.updateTabBadge();

            this.$tooltip = this.$('[data-toggle="tooltip"]');

            this.$tooltip.tooltip({
                container: 'body',
                placement: 'bottom'
            });
        },

        remove: function() {
            this.$tooltip.tooltip('destroy');
            ListItemView.prototype.remove.call(this);
        },

        updateTabBadge: function() {
            const newDocuments = this.model.get('newDocuments');

            if(newDocuments > 0) {
                this.$('.new-document-label')
                    .removeClass('hide')
                    .text(newDocuments > NEW_DOCS_LIMIT
                        ? NEW_DOCS_LIMIT + '+'
                        : newDocuments);
            } else {
                this.$('.new-document-label')
                    .addClass('hide');
            }
        },

        updateSavedness: function() {
            const changed = this.queryState
                ? !this.model.equalsQueryState(this.queryState)
                : false;
            const differentFromServer = this.model.isNew() || changed;
            this.$('.search-tab-anchor').toggleClass('bold', differentFromServer);
            this.$('.search-tab-anchor .hp-new').toggleClass('hide', !differentFromServer);
        },

        updateQueryStateListeners: function() {
            const newQueryState = this.queryStates.get(this.model.cid);

            if(this.queryState) {
                this.stopListening(this.queryState.selectedIndexes);
                this.stopListening(this.queryState.conceptGroups);
                this.stopListening(this.queryState.selectedParametricValues);
                this.stopListening(this.queryState.datesFilterModel);
                this.stopListening(this.queryState.geographyModel);
                this.stopListening(this.queryState.documentSelectionModel);
            }

            this.queryState = newQueryState;

            if(this.queryState) {
                this.listenTo(this.queryState.selectedIndexes, 'add remove', this.updateSavedness);
                this.listenTo(this.queryState.conceptGroups, 'update change', this.updateSavedness);
                this.listenTo(this.queryState.selectedParametricValues, 'add remove', this.updateSavedness);
                this.listenTo(this.queryState.datesFilterModel, 'change', this.updateSavedness);
                this.listenTo(this.queryState.documentSelectionModel, 'change', this.updateSavedness);
            }
        }
    });
});
