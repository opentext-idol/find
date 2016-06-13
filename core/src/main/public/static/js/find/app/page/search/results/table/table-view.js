/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection',
    'find/app/page/search/results/parametric-results-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/table/table-view.html',
    'datatables.net-bs',
    'datatables.net-fixedColumns'
], function(BaseCollection, ParametricResultsView, i18n, tableTemplate) {
    'use strict';

    // As this is mixed case, it can't match an IDOL field or a HOD field
    var NONE_COLUMN = 'defaultColumn';

    var strings = {
        info: i18n['search.resultsView.table.info'],
        infoFiltered: i18n['search.resultsView.table.infoFiltered'],
        lengthMenu: i18n['search.resultsView.table.lengthMenu'],
        search: i18n['search.resultsView.table.searchInResults'],
        zeroRecords: i18n['search.resultsView.table.zeroRecords'],
        paginate: {
            next: i18n['search.resultsView.table.next'],
            previous: i18n['search.resultsView.table.previous']
        }
    };

    var TableCollection = BaseCollection.extend({
        url: '../api/public/parametric/dependent-values',

        parse: function(data) {
            this.columnNames = _.chain(data)
                // take all the field arrays
                .pluck('field')
                // flatten into a single array so we can pluck the values
                .flatten()
                .pluck('value')
                // make unique and sort
                .uniq()
                .sort()
                .value();

            if (_.contains(this.columnNames, '')) {
                // remove '' and replace it with our magic name at the front if it exists as a value
                this.columnNames = _.without(this.columnNames, '');

                this.columnNames.unshift(NONE_COLUMN);
            }

            if (_.isEmpty(this.columnNames)) {
                return _.map(data, function(datum) {
                    return {
                        count: Number(datum.count),
                        text: datum.value
                    }
                });
            }
            else {
                return _.map(data, function(datum) {
                    var columns = _.chain(datum.field)
                        .map(function(field) {
                            var value = {};
                            value[field.value || NONE_COLUMN] = Number(field.count);

                            return value;
                        })
                        .reduce(function(memo, fieldAndCount) {
                            return _.extend(memo, fieldAndCount);
                        }, {})
                        .value();

                    return _.extend({
                        text: datum.value
                    }, columns);
                }, this);
            }
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

                // DataTables doesn't like tables that already have data...
                this.$table.empty();
            }

            // columnNames will be empty if only one field is selected
            if (_.isEmpty(this.dependentParametricCollection.columnNames)) {
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
                    language: strings
                });
            }
            else {
                var columns = _.map(this.dependentParametricCollection.columnNames, function(name) {
                    return {
                        data: name,
                        defaultContent: 0,
                        title: name === NONE_COLUMN ? i18n['search.resultsView.table.noneHeader'] : name
                    }
                });

                this.$table.dataTable({
                    autoWidth: false,
                    data: this.dependentParametricCollection.toJSON(),
                    deferRender: true,
                    fixedColumns: true,
                    scrollX: true,
                    columns: [{
                        data: 'text',
                        title: this.fieldsCollection.at(0).get('field')
                    }].concat(columns),
                    language: strings
                });
            }

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