/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection',
    'find/app/page/search/results/parametric-results-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/table/table-view.html',
    'datatables.net-bs'
], function(BaseCollection, ParametricResultsView, i18n, tableTemplate) {
    'use strict';

    var TableCollection = BaseCollection.extend({
        url: '../api/public/parametric/dependent-values',

        parse: function(data) {
            return _.map(data, function(datum) {
                return {
                    count: Number(datum.count),
                    text: datum.value
                }
            });
        }
    });

    return ParametricResultsView.extend({

        tableTemplate: _.template(tableTemplate),

        initialize: function(options) {
            ParametricResultsView.prototype.initialize.call(this, _.defaults({
                dependentParametricCollection: new TableCollection(),
                emptyDependentMessage: i18n['search.resultsView.table.error.noDependentParametricValues'],
                emptyMessage: i18n['search.resultsView.table.error.noParametricValues'],
                errorMessage: i18n['search.resultsView.table.error.query']
            }, options))
        },

        render: function() {
            ParametricResultsView.prototype.render.apply(this, arguments);

            this.$content.html(this.tableTemplate());

            this.$table = this.$('table');
        },

        update: function () {
            if (this.dataTable) {
                this.dataTable.destroy();
            }

            this.$table.dataTable({
                autoWidth: false,
                data: this.dependentParametricCollection.toJSON(),
                columns: [{
                    data: 'text',
                    title: this.fieldsCollection.at(0).get('field')
                }, {
                    data: 'count',
                    title: i18n['search.resultsView.table.count']
                }],
                language: {
                    search: i18n['search.resultsView.table.searchInResults']
                }
            });

            this.dataTable = this.$table.DataTable();
        },

        remove: function() {
            if (this.dataTable) {
                this.dataTable.destroy();
            }

            ParametricResultsView.prototype.remove.apply(this, arguments);
        }

    })

});