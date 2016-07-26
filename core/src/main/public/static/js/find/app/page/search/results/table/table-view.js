/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/results/parametric-results-view',
    'find/app/page/search/results/table/table-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/table/table-view.html',
    'datatables.net-bs',
    'datatables.net-fixedColumns'
], function(ParametricResultsView, TableCollection, i18n, tableTemplate) {
    'use strict';

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

            // if field is null then nothing has loaded and datatables will fail
            if (this.fieldsCollection.at(0).get('field')) {
                // columnNames will be empty if only one field is selected
                if (_.isEmpty(this.dependentParametricCollection.columnNames)) {
                    this.$table.dataTable({
                        autoWidth: false,
                        data: this.dependentParametricCollection.toJSON(),
                        columns: [
                            {
                                data: 'text',
                                title: this.fieldsCollection.at(0).get('displayValue')
                            }, {
                                data: 'count',
                                title: i18n['search.resultsView.table.count']
                            }
                        ],
                        language: strings
                    });
                }
                else {
                    var columns = _.map(this.dependentParametricCollection.columnNames, function(name) {
                        return {
                            data: name,
                            defaultContent: 0,
                            title: name === TableCollection.noneColumn ? i18n['search.resultsView.table.noneHeader'] : name
                        }
                    });

                    this.$table.dataTable({
                        autoWidth: false,
                        data: this.dependentParametricCollection.toJSON(),
                        deferRender: true,
                        fixedColumns: true,
                        scrollX: true,
                        columns: [
                            {
                                data: 'text',
                                title: this.fieldsCollection.at(0).get('displayValue')
                            }
                        ].concat(columns),
                        language: strings
                    });
                }

                this.dataTable = this.$table.DataTable();
            }
        },

        remove: function() {
            if (this.dataTable) {
                this.dataTable.destroy();
            }

            ParametricResultsView.prototype.remove.apply(this, arguments);
        }

    })

});