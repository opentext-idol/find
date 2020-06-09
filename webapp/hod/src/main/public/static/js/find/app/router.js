/*
 * (c) Copyright 2015-2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'find/app/router-constructor'
], function(_, RouterConstructor) {

    //noinspection LocalVariableNamingConventionJS
    var Router = RouterConstructor.extend({
        routes: _.extend({
            'search/document/:domain/:index/:reference': 'documentDetail',
            'search/suggest/:domain/:index/:reference': 'suggest'
        }, RouterConstructor.prototype.routes)
    });

    return new Router();

});
