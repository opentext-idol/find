/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'js-whatever/js/modal',
    'find/app/page/search/filters/geography/geography-editor-view',
    'i18n!find/nls/bundle',
    'underscore'
], function(Backbone, Modal, GeographyEditorView, i18n, _) {
    'use strict';

    return Modal.extend({
        className: Modal.prototype.className + ' fixed-height-modal',

        initialize: function(options) {
            this.geographyEditorView = new GeographyEditorView({
                geography: options.geography
            });

            Modal.prototype.initialize.call(this, {
                actionButtonClass: 'button-primary',
                actionButtonText: i18n['app.apply'],
                secondaryButtonText: i18n['app.cancel'],
                contentView: this.geographyEditorView,
                title: i18n['search.geography'],
                actionButtonCallback: _.bind(function() {
                    // Update the search with new selected values on close
                    // TODO: edit the filters

                    this.hide();
                }, this)
            });

            this.$el.on('shown.bs.modal', _.bind(this.geographyEditorView.updateMapSize, this.geographyEditorView))
        },

        remove: function() {
            this.geographyEditorView.remove();
            Modal.prototype.remove.call(this);
        }
    });
});
