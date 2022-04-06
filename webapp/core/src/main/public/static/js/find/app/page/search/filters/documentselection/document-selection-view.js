/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/documentselection/document-selection-view.html'
], function (_, Backbone, i18n, template) {

    /**
     * View representing the document selection filter.  Includes a button to toggle selection for
     * all documents.
     */
    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .toggle-all-button': function () {
                if (this.toggleWillSelect()) {
                    this.documentSelectionModel.selectAll();
                } else {
                    this.documentSelectionModel.excludeAll();
                }
            }
        },

        initialize: function (options) {
            this.documentSelectionModel = options.documentSelectionModel;
            this.savedSearchModel = options.savedSearchModel;

            this.listenTo(this.savedSearchModel, 'sync', this.render);
            this.listenTo(this.documentSelectionModel, 'change', this.render);
        },

        /**
         * Whether 'toggle all documents' currently means 'select all documents', else 'exclude all
         * documents'.
         */
        toggleWillSelect: function () {
            const allSelected = !this.documentSelectionModel.get('isWhitelist') &&
                this.documentSelectionModel.getReferencesCount() === 0;
            return !allSelected;
        },

        render: function () {
            this.$el.html(this.template({
                toggleMessage: this.toggleWillSelect() ?
                    i18n['search.documentSelection.selectAll'] :
                    i18n['search.documentSelection.excludeAll']
            }));
        }

    });
});
