/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * Show details about a fact in a side panel (see `ResultsViewAugmentation`).
 *
 * Dependencies:
 *  - factsView
 *  - previewModeModel
 *  - documentRenderer
 */

define([
    'underscore',
    'backbone',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/document/preview-mode-fact-view.html'
], function(_, Backbone, i18n, template) {
    'use strict';

    return Backbone.View.extend({
        className: 'well flex-column m-b-nil full-height preview-mode-fact',

        template: _.template(template),

        events: {
            'click .close-preview-mode': 'triggerClose',

            'click .preview-mode-fact-container .document-url [data-docref]': function (e) {
                this.$('.preview-mode-fact-loading').removeClass('hide');
                this.$('.preview-mode-fact-container').addClass('hide');
                const $link = $(e.target);
                this.factsView.previewDoc($link.data('docindex'), $link.data('docref'));
            }
        },

        initialize: function (options) {
            this.factsView = options.factsView;
            this.previewModeModel = options.previewModeModel;
            this.documentRenderer = options.documentRenderer;
        },

        triggerClose: function () {
            this.previewModeModel.set({ mode: null });
        },

        render: function () {
            this.$el.html(this.template({
                i18n: i18n
            }));
            const document = new Backbone.Model();
            document.set('facts', [this.model.toJSON()]);
            document.set('fields', []);
            this.$('.preview-mode-fact-container')
                .html(this.documentRenderer.renderEntityFactsDetail(document));
        }

    });
});
