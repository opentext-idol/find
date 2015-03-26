/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/vent-constructor',
    'find/app/router'
], function(Vent, router) {
    return new Vent(router);
});
