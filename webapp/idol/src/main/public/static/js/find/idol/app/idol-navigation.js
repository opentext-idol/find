/*
 * Copyright 2016-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
