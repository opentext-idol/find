/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'dropzone',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/customisations/asset-widget.html'
], function(Backbone, Dropzone, i18n, template) {
    'use strict';

    const BASE_URL = '/api/admin/customisation/assets/';

    return Backbone.View.extend({

        className: 'col-sm-4',

        template: _.template(template),

        initialize: function(options) {
            this.description = options.description;
            this.height = options.height;
            this.title = options.title;
            this.type = options.type;
            this.width = options.width;

            this.url = BASE_URL + this.type;
        },

        render: function() {
            this.$el.html(this.template({
                description: this.description,
                title: this.title
            }));

            var width = this.width;
            var height = this.height;

            this.dropzone = new Dropzone(this.$('.dropzone')[0], {
                acceptedFiles: 'image/*',
                addRemoveLinks: false,
                autoProcessQueue: true,
                // matches Spring Boot setting
                maxFilesize: 1,
                url: this.url,
                dictDefaultMessage: i18n['dropzone.dictDefaultMessage'],
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
                    file.rejectDimensions = function() { done(i18n['customisations.fileDimensionsInvalid']); };
                },
                init: function() {
                    // Register for the thumbnail callback
                    // When the thumbnail is created the image dimensions are set
                    this.on('thumbnail', function(file) {
                        if (file.width !== width || file.height !== height) {
                            file.rejectDimensions()
                        }
                        else {
                            file.acceptDimensions();
                        }
                    });

                    // default error handling displays the server response as is, which is no good
                    this.on('error', function(file, response) {
                        var errorMessage = i18n['customisations.error.' + response] || i18n['customisations.error.default'];
                        $(file.previewElement).find('.dz-error-message').text(errorMessage);
                    })
                }
            });
        }

    });

});