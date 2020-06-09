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
    'backbone',
    'i18n!find/nls/bundle',
    'i18n!find/idol/nls/snapshots',
    'find/app/util/collapsible',
    './data-panel-view',
    './snapshot-detail',
    './snapshot-restrictions'
], function(_, Backbone, i18n, snapshotsI18n, Collapsible, DataPanelView,
            snapshotDetail, snapshotRestrictions) {
    'use strict';

    /**
     * Contents of the left side panel for a saved snapshot.
     * @name SnapshotDataView
     * @constructor
     */
    return Backbone.View.extend({
        initialize: function(options) {
            this.collapsibles = [
                new Collapsible({
                    title: options.savedSearchModel.get('type') === 'SNAPSHOT' ? snapshotsI18n['detailTitle.snapshot']: snapshotsI18n['detailTitle.readonly'],
                    view: new DataPanelView(_.extend({
                        model: options.savedSearchModel
                    }, snapshotDetail))
                }),
                new Collapsible({
                    title: snapshotsI18n['restrictionsTitle'],
                    view: new DataPanelView(_.extend({
                        model: options.savedSearchModel
                    }, snapshotRestrictions))
                })
            ];
        },

        render: function() {
            this.$el.empty();

            _.each(this.collapsibles, function(view) {
                this.$el.append(view.$el);
                view.render();
            }, this);
        },

        remove: function() {
            _.invoke(this.collapsibles, 'remove');
            Backbone.View.prototype.remove.call(this);
        }
    });
});
