/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const Backbone = require('backbone');
const i18n = require('find/nls/bundle');
const snapshotsI18n = require('find/idol/nls/snapshots');
const Collapsible = require('find/app/util/collapsible');
const DataPanelView = require('./data-panel-view');
const snapshotDetail = require('./snapshot-detail');
const snapshotRestrictions = require('./snapshot-restrictions');

/**
 * Contents of the left side panel for a saved snapshot.
 * @name SnapshotDataView
 * @constructor
 */

module.exports = Backbone.View.extend({
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

