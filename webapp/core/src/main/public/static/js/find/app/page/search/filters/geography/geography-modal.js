/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/util/modal',
    'find/app/page/search/filters/geography/geography-editor-view',
    'i18n!find/nls/bundle',
    'underscore'
], function(Backbone, Modal, GeographyEditorView, i18n, _) {
    'use strict';

    return Modal.extend({
        className: Modal.prototype.className + ' fixed-height-modal geography-modal',

        initialize: function(options) {
            const shapes = options.shapes || [];
            const actionButtonCallback = options.actionButtonCallback;

            this.geographyEditorView = new GeographyEditorView({
                shapes: shapes,
                geospatialUnified: options.geospatialUnified
            });

            Modal.prototype.initialize.call(this, {
                actionButtonClass: 'button-primary',
                actionButtonText: i18n['app.apply'],
                secondaryButtonText: i18n['app.cancel'],
                contentView: this.geographyEditorView,
                title: i18n['search.geography.modal.title'],
                actionButtonCallback: _.bind(function() {
                    const shapes = this.geographyEditorView.getShapes();
                    actionButtonCallback && actionButtonCallback(shapes);
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
