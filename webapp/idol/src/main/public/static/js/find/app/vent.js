/*
 * Copyright 2016-2017 Open Text.
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

define([
    'underscore',
    './core-vent',
    'find/app/configuration',
    'find/app/router'
], function(_, CoreVent, configuration, router) {

    'use strict';

    function IdolVent(router) {
        CoreVent.call(this, router);
    }

    IdolVent.prototype = Object.create(CoreVent.prototype);

    _.extend(IdolVent.prototype, {
        constructor: IdolVent,

        addSuffixForDocument: function(model) {
            return [model.get('index'), model.get('reference')]
                .map(encodeURIComponent)
                .join('/');
        }
    });

    return new IdolVent(router);

});
