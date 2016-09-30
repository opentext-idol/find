/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'dropzone',
    'find/app/page/customisations/collection-dropdown-view',
    'find/app/util/confirm-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/customisations/asset-widget.html'
], function(Backbone, Dropzone, CollectionDropdownView, Confirm, i18n, template) {
    'use strict';

    const BASE_URL = '/api/admin/customisation/assets/';

    return Backbone.View.extend({

        className: 'col-sm-4',

        template: _.template(template),
        headerTemplate: _.template('<option value=""><%-i18n["customisations.selectFile"]%></option>'),

        events: {
            'click .delete-asset': function() {
                var file = this.dropdownView.getValue();

                new Confirm({
                    cancelClass: 'btn-white',
                    cancelIcon: '',
                    cancelText: i18n['app.cancel'],
                    hiddenEvent: 'hidden.bs.modal',
                    message: i18n['customisations.delete.message'](file),
                    okText: i18n['app.button.delete'],
                    okClass: 'btn-danger',
                    okIcon: '',
                    title: i18n['customisations.delete.title'],
                    okHandler: _.bind(function () {
                        this.collection.get(file).destroy({
                            wait: true
                        });
                    }, this)
                });
            }
        },

        initialize: function(options) {
            this.description = options.description;
            this.height = options.height;
            this.title = options.title;
            this.type = options.type;
            this.width = options.width;

            this.imageClass = options.imageClass || '';

            this.url = _.result(this.collection, 'url');

            this.dropdownView = new CollectionDropdownView({
                collection: this.collection,
                headerHtml: this.headerTemplate({i18n: i18n}),
                textAttribute: 'id',
                valueAttribute: 'id'
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

            this.$image = this.$('.asset-image');
            this.$deleteAsset = this.$('.delete-asset').tooltip();

            var width = this.width;
            var height = this.height;

            var self  = this;

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

                    this.on('success', function(file) {
                        self.collection.add({id: file.name});
                    });

                    // default error handling displays the server response as is, which is no good
                    this.on('error', function(file, response) {
                        var errorMessage = i18n['customisations.error.' + response] || i18n['customisations.error.default'];
                        $(file.previewElement).find('.dz-error-message').text(errorMessage);
                    })
                }
            });

            this.dropdownView.render();
            this.$('.asset-dropdown').prepend(this.dropdownView.el);

            this.listenTo(this.dropdownView, 'change', function(value) {
                if (value === '') {
                    this.$image.css('background-image', '');
                    this.$deleteAsset.addClass('hidden');
                }
                else {
                    this.$image.css('background-image', 'url(' + this.url + '/' + encodeURIComponent(value) + ')');
                    this.$deleteAsset.removeClass('hidden');
                }
            });
        },

        remove: function() {
            this.$deleteAsset.tooltip('destroy');

            Backbone.View.prototype.remove.apply(this, arguments);
        }

    });

});