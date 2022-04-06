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
    'find/app/page/search/results/parametric-results-view',
    'find/app/page/search/results/table/table-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/table/table-view.html',
    'datatables.net-bs',
    'datatables.net-fixedColumns'
], function(_, ParametricResultsView, TableCollection, i18n, tableTemplate) {
    'use strict';

    const strings = {
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
                emptyDependentMessage: i18n['search.resultsView.table.noDependentParametricValues'],
                emptyMessage: i18n['search.resultsView.table.noParametricValues'],
                errorMessageArguments: {messageToUser: i18n['search.resultsView.table.error.query']}
            }, options))
        },

        render: function() {
            ParametricResultsView.prototype.render.apply(this);

            this.$content.html(this.tableTemplate());

            this.$table = this.$('table');
        },

        update: function() {
            if(this.dataTable) {
                this.dataTable.destroy();

                // DataTables doesn't like tables that already have data
                this.$table.empty();
            }

            // if parametric collection is empty then nothing has loaded and datatables will fail
            if(!this.parametricCollection.isEmpty()) {
                // columnNames will be empty if only one field is selected
                if(_.isEmpty(this.dependentParametricCollection.columnNames)) {
                    this.$table.dataTable({
                        autoWidth: false,
                        data: this.dependentParametricCollection.toJSON(),
                        columns: [
                            {
                                data: 'text',
                                title: this.fieldsCollection.at(0).get('displayValue')
                            },
                            {
                                data: 'count',
                                title: i18n['search.resultsView.table.count']
                            }
                        ],
                        language: strings
                    });
                } else {
                    const columns = _.map(this.dependentParametricCollection.columnNames, function(name) {
                        return {
                            data: name,
                            defaultContent: 0,
                            title: name === TableCollection.noneColumn
                                ? i18n['search.resultsView.table.noneHeader']
                                : name
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
            if(this.dataTable) {
                this.dataTable.destroy();
            }

            ParametricResultsView.prototype.remove.apply(this);
        }
    })
});
