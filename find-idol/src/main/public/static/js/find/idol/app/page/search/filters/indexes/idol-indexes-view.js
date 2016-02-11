/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/indexes/indexes-view'
], function (IndexesView) {
    'use strict';

    return IndexesView.extend({
        getIndexCategories: function () {
            return null;
        },

        setInitialSelection: function() {
            this.collection.each(function(index) {
                this.selectDatabase(index.get('name'), true);
            }, this);
        },

        findDatabaseNode: function (database) {
            return this.$('[data-id="' + database + '"]');
        },

        selectDatabaseFromNode: function ($target, checked) {
            var index = $target.attr('data-name');
            this.selectDatabase(index, checked);
        },

        /**
         * @desc Selects the database with the given name and domain
         * @param {string} database The name of the database
         * @param {boolean} checked The new state of the database
         */
        selectDatabase: function(database, checked) {
            if (checked) {
                this.currentSelection.push({
                    name: database
                });

                this.currentSelection = _.uniq(this.currentSelection, function (item) {
                    // uniq uses reference equality on the transform
                    return item.name;
                });
            } else {
                this.currentSelection = _.reject(this.currentSelection, function (selectedItem) {
                    return selectedItem.name === database;
                });
            }

            this.updateCheckedOptions();
            this.updateSelectedDatabases();
        }
    });
});
