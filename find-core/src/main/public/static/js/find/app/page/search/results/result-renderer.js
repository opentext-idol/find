/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/page/search/results/add-links-to-summary',
    'find/app/util/document-mime-types',
    'text!find/templates/app/page/search/results/results-container.html'
], function(Backbone, _, i18n, addLinksToSummary, documentMimeTypes, resultsTemplate) {

    function getContentTypeClass(model) {
        var contentType = model.get('contentType') || '';

        var matchedType = _.find(documentMimeTypes, function(mimeType) {
            return Boolean(_.find(mimeType.typeRegex, function(regex) {
                return regex().test(contentType);
            }));
        });

        return matchedType.className;
    }

    var defaultData = function (model, isPromotion) {
        return {
            contentType: getContentTypeClass(model),
            date: model.has('date') ? model.get('date').fromNow() : null,
            highlightedSummary: addLinksToSummary(this.entityCollection, model.get('summary')),
            isPromotion: isPromotion,
            staticPromotion: model.get('promotionCategory') === 'STATIC_CONTENT_PROMOTION'
        };
    };

    var templates = [
        {
            template: _.template(resultsTemplate),
            data: defaultData,
            predicate: function(model) {
                return true
            }
        }
    ];

    var ResultRenderer = function (options) {
        this.entityCollection = options.entityCollection;
    };

    _.extend(ResultRenderer.prototype, {
        getResult: function(model, isPromotion) {
            var templateData = _.find(templates, function(templateData) {
                return templateData.predicate.call(this, model);
            });

            return templateData.template({
                i18n: i18n,
                data: templateData.data.call(this, model, isPromotion),
                model: model
            });
        }
    });

    return ResultRenderer;
});
