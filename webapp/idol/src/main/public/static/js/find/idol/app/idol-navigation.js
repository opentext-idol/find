/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/navigation',
    'text!find/idol/templates/navigation-menu-items.html',
    'underscore'
], function(Navigation, menuItems, _) {
    'use strict';

    return Navigation.extend({
        menuItems: _.template(menuItems)
    });
});
