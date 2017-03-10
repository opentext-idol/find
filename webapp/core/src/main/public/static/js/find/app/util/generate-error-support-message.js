/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/nls/errors',
    'text!find/templates/app/util/error-custom-contact-support.html',
    'underscore'
], function(configuration, i18n, i18nErrors, errorCustomContactSupportTemplate, _) {
    'use strict';

    const customContactSupportTemplate = _.template(errorCustomContactSupportTemplate);

    /**
     * @typedef {Object} GenerateErrorSupportMessageArgument
     * @property {string} errorHeader is meant for generic headers, such as 'An error has occurred'
     * @property {string} messageToUser some extra, though generic information, e.g. 'Failed to retrieve parametric fields for Sunburst View'
     * @property {string} errorLookup is the error code/type. Used for lookup in errors.js to determine if it is a user-caused error which should be prettified
     * @property {string} errorUUID is the unique identifier of the error instance
     * @property {string} errorDetails all additional information about the error
     */
    /**
     * @param {GenerateErrorSupportMessageArgument} argumentHash
     */
    function generateErrorSupportMessage(argumentHash) {
        const options = _.extend({
            errorHeader: i18n['error.message.default'],
            messageToUser: "",
            errorDetails: "",
            errorLookup: "",
            errorUUID: ""
        }, argumentHash);

        const detailsTemplate = i18n['error.details'];
        const uuidTemplate = i18n['error.UUID'];
        let needTechSupport;

        if(!options.errorDetails && options.errorLookup) {
            if(i18nErrors["error.code." + options.errorLookup]) {
                options.errorDetails = i18nErrors["error.code." + options.errorLookup];
                // Errors in the bundle are user-created errors, so we don't want them to contact support
                needTechSupport = false;
            } else {
                needTechSupport = true;
            }
        } else {
            needTechSupport = true;
        }

        // If app's config.json contains a custom "call support" string, print it. Otherwise fall back on bundle.js
        const messageContactSupport = needTechSupport ? (configuration().errorCallSupportString || i18n['error.default.contactSupport']) : "";

        return customContactSupportTemplate({
            errorHeader: options.errorHeader,
            messageToUser: options.messageToUser,
            messageContactSupport: messageContactSupport,
            errorDetails: options.errorDetails ? detailsTemplate(options.errorDetails) : detailsTemplate(i18n['error.unknown']),
            errorUUID: (needTechSupport && options.errorUUID) ? uuidTemplate(options.errorUUID) : "",
            errorLookup: needTechSupport ? options.errorLookup : ""
        });
    }

    return generateErrorSupportMessage;
});
