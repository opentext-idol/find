/*
 * Copyright 2015-2017 Open Text.
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

const Backbone = require('backbone');
const _ = require('underscore');
const Handlebars = require('handlebars');
const $ = require('jquery');
const vent = require('find/app/vent');
const addLinksToSummary = require('find/app/page/search/results/add-links-to-summary');
const documentMimeTypes = require('find/app/util/document-mime-types');
const urlManipulator = require('find/app/util/url-manipulator');
const defaultResultTemplate = require('find/templates/app/page/search/default-custom-templates/search-result.handlebars');
const defaultEntitySearchTemplate = require('find/templates/app/page/search/default-custom-templates/entity-search.handlebars');
const defaultPreviewTemplate = require('find/templates/app/page/search/default-custom-templates/preview-mode-metadata.handlebars');
const defaultDocumentFactsTemplate = require('find/templates/app/page/search/default-custom-templates/document-facts.handlebars');
const defaultPromotionTemplate = require('find/templates/app/page/search/default-custom-templates/promotion.handlebars');
const defaultEntityFactsTemplate = require('find/templates/app/page/search/default-custom-templates/entity-facts.handlebars');
const defaultEntityFactsDetailTemplate = require('find/templates/app/page/search/default-custom-templates/entity-facts-detail.handlebars');
const capitaliseHelper = require('./template-helpers/capitalise-helper');
const equalHelper = require('./template-helpers/equal-helper');
const hasFieldHelper = require('./template-helpers/has-field-helper');
const hasFieldValueHelper = require('./template-helpers/has-field-value-helper');
const getFieldValueHelper = require('./template-helpers/get-field-value-helper');
const getFieldValuesHelper = require('./template-helpers/get-field-values-helper');
const jsonStringifyHelper = require('./template-helpers/json-stringify-helper');
const percentageHelper = require('./template-helpers/percentage-helper');
const placeholderTemplateHelper = require('./template-helpers/placeholder-template-helper');
const prettyPrintNumberHelper = require('./template-helpers/pretty-print-number-helper');
const regexIfHelper = require('./template-helpers/regex-if-helper');
const toExternalUrlHelper = require('./template-helpers/to-external-url-helper');
const toLowerCaseHelper = require('./template-helpers/to-lower-case-helper');
const toRelativeTimeHelper = require('./template-helpers/to-relative-time-helper');
const toUpperCaseHelper = require('./template-helpers/to-upper-case-helper');
const typeofHelper = require('./template-helpers/typeof-helper');
const wikiThumbnailHelper = require('./template-helpers/wiki-thumbnail-helper');
const withFieldHelper = require('./template-helpers/with-field-helper');
const i18nHelper = require('./template-helpers/i18n-helper');

function templatePredicate(triggers) {
    return function(model) {
        return _.every(triggers, function(trigger) {
            if (trigger.indexes) {
                const index = model.get('index');

                if (index) {
                    const indexCaps = index.toUpperCase();

                    if (!_.find(trigger.indexes, function(triggerIndex){
                            return triggerIndex.toUpperCase() === indexCaps;
                        })) {
                        return false
                    }
                    else if (!trigger.field) {
                        return true;
                    }
                }
            }

            const documentField = _.findWhere(model.get('fields'), {id: trigger.field});

            if (documentField) {
                return _.isEmpty(trigger.values) || _.some(trigger.values, _.partial(_.contains, documentField.values));
            } else {
                return false;
            }
        });
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

    let thumbnailSrc;

    if (model.has('thumbnail')) {
        thumbnailSrc = 'data:image/jpeg;base64,' + model.get('thumbnail');
    } else if (model.has('thumbnailUrl')) {
        thumbnailSrc = model.get('thumbnailUrl');
    }

    return {
        reference: model.get('reference'),
        title: model.get('title'),
        date: date && date.format('LLLL'),
        database: model.get('index'),
        promotionName: model.get('promotionName'),
        similarDocumentsUrl: vent.suggestUrlForDocument(model),
        summary: addLinksToSummary(model.get('summary')),
        url: url ? urlManipulator.addSpecialUrlPrefix(model.get('contentType'), url) : null,
        icon: 'icomoon-file-' + getContentTypeClass(model),
        intentRankedHit: model.get('intentRankedHit'),
        thumbnailSrc: thumbnailSrc,
        age: date && date.fromNow(),
        fields: model.get('fields').map(_.partial(_.pick, _, ['id', 'displayName', 'advanced', 'values'])),
        weight: model.get('weight'),
        facts: model.get('facts')
    };
}

function renderTemplate(key) {
    return function(model) {
        const template = _.find(this.templates[key], function(data) {
            return data.predicate(model);
        }).template;

        return template(buildContext(model));
    };
}

function DocumentRenderer(configuration) {
    const handlebars = Handlebars.create();

    handlebars.registerHelper({
        capitalise: capitaliseHelper,
        equal: equalHelper,
        i18n: i18nHelper,
        hasField: hasFieldHelper,
        hasFieldValue: hasFieldValueHelper,
        getFieldValue: getFieldValueHelper,
        getFieldValues: getFieldValuesHelper,
        jsonStringify: jsonStringifyHelper,
        percentage: percentageHelper,
        placeholderTemplate: placeholderTemplateHelper,
        prettyPrintNumber: prettyPrintNumberHelper,
        regexIf: regexIfHelper,
        toExternalUrl: toExternalUrlHelper,
        toLowerCase: toLowerCaseHelper,
        toRelativeTime: toRelativeTimeHelper,
        toUpperCase: toUpperCaseHelper,
        typeof: typeofHelper,
        wikiThumbnailHelper: wikiThumbnailHelper,
        withField: withFieldHelper
    });

    this.loadPromise = $.get('customization/result-templates')
        .done(function(templateFiles) {
            this.templates = _.chain([
                    {defaultTemplate: defaultResultTemplate, key: 'searchResult'},
                    {defaultTemplate: defaultPreviewTemplate, key: 'previewPanel'},
                    {defaultTemplate: defaultDocumentFactsTemplate, key: 'documentFacts'},
                    {defaultTemplate: defaultEntitySearchTemplate, key: 'entitySearch'},
                    {defaultTemplate: defaultPromotionTemplate, key: 'promotion'},
                    {defaultTemplate: defaultEntityFactsTemplate, key: 'entityFacts'},
                    {defaultTemplate: defaultEntityFactsDetailTemplate, key: 'entityFactsDetail'}
            ])
                .map(function(type) {
                    const configuredTemplates = (configuration[type.key] || [])
                        .map(function(templateConfig) {
                            return {
                                predicate: templatePredicate(templateConfig.triggers),
                                template: handlebars.compile(templateFiles[templateConfig.file])
                            };
                        });

                    const templateList = configuredTemplates.concat({
                        template: handlebars.compile(type.defaultTemplate),
                        predicate: _.constant(true)
                    });

                    return [type.key, templateList];
                })
                .object()
                .value();
        }.bind(this));
}

_.extend(DocumentRenderer.prototype, {
    renderResult: renderTemplate('searchResult'),
    renderPromotion: renderTemplate('promotion'),
    renderPreviewMetadata: renderTemplate('previewPanel'),
    renderDocumentFacts: renderTemplate('documentFacts'),
    renderEntity: renderTemplate('entitySearch'),
    renderEntityFacts: renderTemplate('entityFacts'),
    renderEntityFactsDetail: renderTemplate('entityFactsDetail')
});

module.exports = DocumentRenderer;

