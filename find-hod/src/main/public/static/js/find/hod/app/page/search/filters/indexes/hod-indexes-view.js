/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/indexes/indexes-view',
    'js-whatever/js/escape-hod-identifier',
    'i18n!find/nls/indexes'
], function (IndexesView, escapeHodIdentifier, i18n) {
    'use strict';

    return IndexesView.extend({
        getIndexCategories: function () {
            return [
                {
                    name: 'private',
                    displayName: i18n['search.indexes.privateIndexes'],
                    className: 'list-unstyled',
                    filter: function(model) {
                    return model.get('domain') !== 'PUBLIC_INDEXES';
                }
            }, {
                    name: 'public',
                    displayName: i18n['search.indexes.publicIndexes'],
                    className: 'list-unstyled',
                    filter: function(model) {
                        return model.get('domain') === 'PUBLIC_INDEXES';
                    }
                }
            ];
        },

        setInitialSelection: function() {
            var privateIndexes = this.collection.reject({domain: 'PUBLIC_INDEXES'});

            if(privateIndexes.length > 0) {
                _.each(privateIndexes, function(index) {
                    this.selectDatabase(index.get('name'), index.get('domain'), true);
                }, this);
            }
            else {
                _.each(this.collection.where({domain: 'PUBLIC_INDEXES'}), function(index) {
                    this.selectDatabase(index.get('name'), index.get('domain'), true);
                }, this);
            }
        },

        findDatabaseNode: function (database) {
            return this.$('[data-id="' + database.domain + ':' + database.name + '"]');
        },

        selectDatabaseFromNode: function ($target, checked) {
            var index = $target.attr('data-name');
            var domain = $target.attr('data-domain');

            this.selectDatabase(index, domain, checked);
        },

        /**
         * @desc Selects the database with the given name and domain
         * @param {string} database The name of the database
         * @param {string} domain The domain of the database
         * @param {boolean} checked The new state of the database
         */
        selectDatabase: function(database, domain, checked) {
            if (checked) {
                this.currentSelection.push({
                    domain: domain,
                    name: database
                });

                this.currentSelection = _.uniq(this.currentSelection, function (item) {
                    // uniq uses reference equality on the transform
                    return escapeHodIdentifier(item.domain) + ':' + escapeHodIdentifier(item.name);
                });
            } else {
                this.currentSelection = _.reject(this.currentSelection, function (selectedItem) {
                    return selectedItem.name === database && selectedItem.domain === domain;
                });
            }

            this.updateCheckedOptions();
            this.updateSelectedDatabases();
        }
    });
});

