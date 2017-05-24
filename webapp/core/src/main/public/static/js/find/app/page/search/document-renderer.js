/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'handlebars',
    'find/app/page/search/results/add-links-to-summary',
    'find/app/util/document-mime-types',
    'find/app/util/url-manipulator',
    'text!find/templates/app/page/search/default-custom-templates/search-result.handlebars',
    'text!find/templates/app/page/search/default-custom-templates/preview-mode-metadata.handlebars',
    'text!find/templates/app/page/search/default-custom-templates/promotion.handlebars',
    './template-helpers/equal-helper',
    './template-helpers/has-field-helper',
    './template-helpers/has-field-value-helper',
    './template-helpers/get-field-value-helper',
    './template-helpers/with-field-helper',
    './template-helpers/i18n-helper'
], function(Backbone, _, Handlebars, addLinksToSummary, documentMimeTypes, urlManipulator, defaultResultTemplate,
            defaultPreviewTemplate, defaultPromotionTemplate, equalHelper, hasFieldHelper, hasFieldValueHelper, getFieldValueHelper,
            withFieldHelper, i18nHelper) {

    function DocumentRenderer() {
        const handlebars = Handlebars.create();

        handlebars.registerHelper({
            equal: equalHelper,
            i18n: i18nHelper,
            hasField: hasFieldHelper,
            hasFieldValue: hasFieldValueHelper,
            getFieldValue: getFieldValueHelper,
            withField: withFieldHelper
        });

        this.defaultTemplates = {
            promotion: handlebars.compile(defaultPromotionTemplate),
            previewMetadata: handlebars.compile(defaultPreviewTemplate),
            result: handlebars.compile(defaultResultTemplate)
        };
    }

    function getContentTypeClass(model) {
        const contentType = model.get('contentType') || '';

        const matchedType = _.find(documentMimeTypes, function(mimeType) {
            return Boolean(_.find(mimeType.typeRegex, function(regex) {
                return regex().test(contentType);
            }));
        });

        return matchedType.className;
    }

    function buildContext(model) {
        const url = model.get('url');
        const date = model.get('date');
        const promotionCategory = model.get('promotionCategory');

        let thumbnail;

        if (model.has('thumbnail')) {
            thumbnail = 'data:image/jpeg;base64,' + model.get('thumbnail');
        } else if (model.has('thumbnailUrl')) {
            thumbnail = model.get('thumbnailUrl');
        }

        return {
            reference: model.get('reference'),
            title: model.get('title'),
            date: date.format('LLLL'),
            database: model.get('index'),
            promotionName: model.get('promotionName'),
            summary: addLinksToSummary(model.get('summary')),
            url: url ? urlManipulator.addSpecialUrlPrefix(model.get('contentType'), url) : null,
            icon: 'icomoon-file-' + getContentTypeClass(model),
            thumbnail: thumbnail,
            age: date.fromNow(),
            fields: model.get('fields').map(_.partial(_.pick, _, ['id', 'displayName', 'advanced', 'values']))
        };
    }

    _.extend(DocumentRenderer.prototype, {
        renderResult: function(model) {
            return this.defaultTemplates.result(buildContext(model));
        },

        renderPromotion: function(model) {
            return this.defaultTemplates.promotion(buildContext(model));
        },

        renderPreviewMetadata: function(model) {
            return this.defaultTemplates.previewMetadata(buildContext(model));
        }
    });

    return DocumentRenderer;

});
