/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/search/results/parametric-results-view',
    'find/app/page/search/results/table/table-collection',
    'find/app/util/generate-error-support-message',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/table/table-view.html',
    'datatables.net-bs',
    'datatables.net-fixedColumns'
], function(_, ParametricResultsView, TableCollection, generateErrorHtml, i18n, tableTemplate) {
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
                emptyDependentMessage: i18n['search.resultsView.table.error.noDependentParametricValues'],
                emptyMessage: generateErrorHtml({errorLookup: 'emptyTableView'}),
                errorMessageArguments: {messageToUser: i18n['search.resultsView.table.error.query']}
            }, options))
        },

        render: function() {
            ParametricResultsView.prototype.render.apply(this);

            this.$('.col-md-12').prepend('<a class="btn btn-default pull-right table-pptx" href="#"><i class="hp-icon hp-document-download"></i> PPTX</a>');

            this.$el.on('click', '.table-pptx', _.bind(function(evt){
                evt.preventDefault();

                var $form = $('<form class="hide" enctype="multipart/form-data" method="post" target="_blank" action="api/bi/export/ppt/table"><input name="title"><textarea name="data"></textarea><input type="submit"></form>');
                $form[0].title.value = i18n['search.resultsView.table.breakdown.by'](this.fieldsCollection.at(0).get('displayValue'));

                var rows = this.$table.find('tr'), nCols = 0;

                var cells = [];

                rows.each(function(idx, el){
                    var tds = $(el).find('th,td');
                    nCols = tds.length;

                    tds.each(function (idx, el) {
                        cells.push($(el).text());
                    })
                });

                $form[0].data.value = JSON.stringify({
                    rows: rows.length,
                    cols: nCols,
                    cells: cells
                });

                $form.appendTo(document.body).submit().remove();
            }, this))

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
                            }, {
                                data: 'count',
                                title: i18n['search.resultsView.table.count']
                            }
                        ],
                        language: strings
                    });
                } else {
                    var columns = _.map(this.dependentParametricCollection.columnNames, function(name) {
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
