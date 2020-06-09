/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
