/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/util/url-manipulator',
    'i18n!find/nls/bundle'
], function(Backbone, _, urlManipulator, i18n) {

    var ResultRenderer = function (options) {
        this.config = options.config;
    };

    _.extend(ResultRenderer.prototype, {
        getResult: function(model, isPromotion, enablePreview, directAccessLink) {
            var templateData = _.find(this.config, function(templateData) {
                return templateData.predicate.call(this, model, isPromotion, enablePreview, directAccessLink);
            });

            var href;
            if (model.get('url')) {
                href = urlManipulator.addSpecialUrlPrefix(model.get('contentType'), model.get('url'));
            }
            return templateData.template({
                i18n: i18n,
                data: templateData.data.call(this, model, isPromotion, enablePreview, directAccessLink),
                model: model,
                href: href
            });
        }
    });

    return ResultRenderer;
});
