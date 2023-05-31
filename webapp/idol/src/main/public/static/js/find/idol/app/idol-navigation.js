/*
 * Copyright 2016-2018 Open Text.
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
    'find/app/navigation',
    'text!find/idol/templates/navigation-menu-items.html',
    'find/idol/app/util/selection-entity-search',
    'underscore'
], function(Navigation, menuItems, SelectionEntitySearch, _) {
    'use strict';

    return Navigation.extend({
        menuItems: _.template(menuItems),

        onEntitySearchSelect: function(group){
            SelectionEntitySearch.setUserSelectedDatabaseGroup(group);
        }
    });
});
