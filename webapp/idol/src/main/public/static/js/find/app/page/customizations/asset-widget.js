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
    'jquery',
    'backbone',
    'dropzone',
    'find/app/page/customizations/asset-viewer',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/customizations/asset-widget.html'
], function(_, $, Backbone, Dropzone, AssetViewer, i18n, template) {
    'use strict';

    const DEFAULT_MESSAGE = _.template('<i class="hp-icon hp-3x hp-document-upload"></i><p><%-message%></p>')({
        message: i18n['dropzone.dictDefaultMessage']
    });

    return Backbone.View.extend({

        className: 'col-md-12 col-lg-4',

        template: _.template(template),

        initialize: function(options) {
            this.description = options.description;
            this.height = options.height;
            this.title = options.title;
            this.width = options.width;

            this.imageClass = options.imageClass || '';

            this.url = _.result(this.collection, 'url');

            this.assetViewer = new AssetViewer({
                collection: this.collection,
                defaultImage: options.defaultImage,
                height: this.height,
                imageClass: this.imageClass,
                type: options.type,
                width: this.width
            });
        },

        render: function() {
            this.$el.html(this.template({
                description: this.description,
                height: this.height,
                i18n: i18n,
                imageClass: this.imageClass,
                title: this.title,
                width: this.width
            }));

            this.assetViewer.setElement(this.$('.asset-viewer')).render();

            var width = this.width;
            var height = this.height;

            var self = this;

            this.dropzone = new Dropzone(this.$('.dropzone')[0], {
                acceptedFiles: 'image/*',
                addRemoveLinks: false,
                autoProcessQueue: true,
                // matches Spring Boot setting
                maxFilesize: 1,
                url: this.url,
                dictDefaultMessage: DEFAULT_MESSAGE,
                dictFallbackMessage: i18n['dropzone.dictFallbackMessage'],
                dictFallbackText: i18n['dropzone.dictFallbackText'],
                dictFileTooBig: i18n['dropzone.dictFileTooBig'],
                dictInvalidFileType: i18n['dropzone.dictInvalidFileType'],
                dictResponseError: i18n['dropzone.dictResponseError'],
                dictCancelUpload: i18n['dropzone.dictCancelUpload'],
                dictCancelUploadConfirmation: i18n['dropzone.dictCancelUploadConfirmation'],
                dictRemoveFile: i18n['dropzone.dictRemoveFile'],
                dictRemoveFileConfirmation: i18n['dropzone.dictRemoveFileConfirmation'],
                // check file dimensions before upload
                // see https://github.com/enyo/dropzone/wiki/FAQ#reject-images-based-on-image-dimensions
                accept: function(file, done) {
                    file.acceptDimensions = done;
                    file.rejectDimensions = function() {
                        done(i18n['customizations.fileDimensionsInvalid']);
                    };
                },
                init: function() {
                    // Register for the thumbnail callback
                    // When the thumbnail is created the image dimensions are set
                    this.on('thumbnail', function(file) {
                        if(file.width === width && file.height === height) {
                            file.acceptDimensions();
                        } else {
                            file.rejectDimensions()
                        }
                    });

                    this.on('success', function(file) {
                        self.collection.add({id: file.name});
                    });

                    // default error handling displays the server response as is, which is no good
                    this.on('error', function(file, response) {
                        var errorMessage = i18n['customizations.error.' + response] || i18n['customizations.error.default'];
                        $(file.previewElement).find('.dz-error-message').text(errorMessage);
                    })
                }
            });
        },

        remove: function() {
            this.assetViewer.remove();

            Backbone.View.prototype.remove.apply(this, arguments);
        }
    });
});
