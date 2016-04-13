/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/search/results/add-links-to-summary',
    'find/app/util/document-mime-types',
    'text!find/templates/app/page/search/results/results-container.html'
], function(_, addLinksToSummary, documentMimeTypes, resultsTemplate) {

    function getContentTypeClass(model) {
        var contentType = model.get('contentType') || '';

        var matchedType = _.find(documentMimeTypes, function(mimeType) {
            return Boolean(_.find(mimeType.typeRegex, function(regex) {
                return regex().test(contentType);
            }));
        });

        return matchedType.className;
    }

    var defaultData = function(model, isPromotion) {
        return {
            contentType: getContentTypeClass(model),
            date: model.has('date') ? model.get('date').fromNow() : null,
            highlightedSummary: addLinksToSummary(this.entityCollection, model.get('summary')),
            isPromotion: isPromotion,
            staticPromotion: model.get('promotionCategory') === 'STATIC_CONTENT_PROMOTION'
        };
    };

    return [
        {
            template: _.template(resultsTemplate),
            data: defaultData,
            predicate: _.constant(true)
        }
    ];
});