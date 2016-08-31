/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/nls/errors',
    'text!find/templates/app/util/error-custom-contact-support.html'
], function(configuration, i18n, i18nErrors, errorCustomContactSupportTemplate) {
    "use strict";
    var customContactSupportTemplate = _.template(errorCustomContactSupportTemplate);

    // TODO: document me???
    return function(argumentHash) {
        var options = _.extend({
            errorHeader: i18n['error.message.default'],
            messageToUser: "",
            errorDetails: i18n['error.unknown'],
            errorLookup: "",
            errorDetailsFallback: i18n['error.unknown']
        }, argumentHash);

        var detailsTemplate = i18n['error.details'];
        var needTechSupport;

        // TODO: only show lookup string if lookup fails ???
        // TODO: always display UUID if lookup fails ???

        if(options.errorDetails) {
            if(i18nErrors["error.code." + options.errorLookup]) {
                options.errorDetails = i18nErrors["error.code." + options.errorLookup];
                // Errors in the bundle are user-created errors, so we don't want them to contact support
                needTechSupport = false;
            } else {
                needTechSupport = true;
            }
        } else {
            options.errorDetails = options.errorDetailsFallback;
            needTechSupport = true;
        }

        var messageContactSupport = needTechSupport ? (configuration().errorCallSupportString || i18n['error.default.contactSupport']) : "";

        return customContactSupportTemplate({
            errorHeader: options.errorHeader,
            messageToUser: options.messageToUser,
            messageContactSupport: messageContactSupport,
            errorDetails: detailsTemplate(options.errorDetails),
            errorLookup: options.errorLookup
        });
    }
});
