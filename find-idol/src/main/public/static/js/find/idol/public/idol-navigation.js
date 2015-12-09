/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/public/navigation',
    'text!find/idol/templates/menuItems.html'
], function(Navigation, menuItems) {

    return Navigation.extend({

        menuItems: _.template(menuItems)

    })

});