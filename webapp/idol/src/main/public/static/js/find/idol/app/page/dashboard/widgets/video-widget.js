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
    'underscore',
    'jquery',
    './saved-search-widget',
    'find/app/configuration',
    'find/app/model/documents-collection',
    'i18n!find/nls/bundle'
], function(_, $, SavedSearchWidget, configuration, DocumentsCollection, i18n) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'list',

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            this.searchResultNumber = this.widgetSettings.searchResultNumber || 1;
            this.restrictSearch = !!(this.widgetSettings.restrictSearch);

            this.documentsCollection = new DocumentsCollection();

            this.$video = $('<video>')
                .prop('autoplay', true)
                .prop('loop', this.widgetSettings.loop !== false)
                .prop('muted', !this.widgetSettings.audio);

            if (this.widgetSettings.crossOrigin) {
                this.$video.prop('crossOrigin', this.widgetSettings.crossOrigin);
            }

            this.listenTo(this.documentsCollection, 'add', function(model) {
                // Re-creates DOM on every update. If this changes, the onHide() method must
                // be adjusted to pause the video rather than remove it.
                if(this.$content && model.get('media') === 'video') {
                    const offset = model.get('offset');

                    this.$video.attr('src', model.get('url') + (offset
                            ? '#t=' + offset
                            : ''));

                    const $container = $('<div class="video-container"></div>');
                    $container.append(this.$video);

                    this.$content.html($container);
                }
            });
        },

        getData: function() {
            let fieldText = this.queryModel.get('fieldText');

            if(this.restrictSearch) {
                const restrictToVideo = 'MATCH{video}:' + configuration().fieldsInfo.contentType.names[0];

                fieldText = restrictToVideo + (fieldText
                        ? ' AND ' + fieldText
                        : '');
            }

            return this.documentsCollection.fetch({
                data: {
                    start: this.searchResultNumber,
                    text: this.queryModel.get('queryText'),
                    max_results: this.searchResultNumber,
                    indexes: this.queryModel.get('indexes'),
                    field_text: fieldText,
                    min_date: this.queryModel.getIsoDate('minDate'),
                    max_date: this.queryModel.getIsoDate('maxDate'),
                    sort: 'relevance',
                    summary: 'context',
                    queryType: 'MODIFIED'
                },
                reset: false
            });
        },

        onHide: function() {
            // Stop video so that it does not play in the background after leaving the dashboard (FIND-1132).
            // Removing video is equivalent to pausing it, as it will get recreated when the page is shown again
            this.$('.video-container').remove();
        },

        isEmpty: function() {
            return this.documentsCollection.isEmpty();
        },

        exportData: function() {
            const videoEl = this.$('video');

            if(!videoEl.length) {
                return null;
            }

            const videoDom = videoEl[0];
            // Compensate for the video element's auto-crop to preserve aspect ratio, jQuery doesn't include this.
            const aspectRatio = videoDom.videoWidth / videoDom.videoHeight;
            const width = videoEl.width();
            const height = videoEl.height();
            const actualWidth = Math.min(width, height * aspectRatio);
            const actualHeight = Math.min(height, width / aspectRatio);

            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');
            canvas.width = actualWidth;
            canvas.height = actualHeight;

            ctx.drawImage(videoDom, 0, 0, canvas.width, canvas.height);

            let dataUrl;

            try {
                // Note: this might not work if the video is hosted elsewhere
                dataUrl = canvas.toDataURL('image/jpeg');
            } catch(e) {
                // If there's an error, e.g. if the video is external and we're not allowed access
                // Print warning in the ppt file to inform the user what happened
                return {
                    data: {
                        text: [
                            {
                                text: i18n['export.powerpoint.videoWidget.exportFailure.CORS'],
                                fontSize: 12
                            }
                        ],
                    },
                    type: 'text'
                };
            }

            return {
                data: {
                    image: dataUrl,
                    markers: []
                },
                type: 'map'
            };
        }
    });
});
