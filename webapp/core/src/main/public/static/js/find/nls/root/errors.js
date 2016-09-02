/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'js-whatever/js/substitution'
], function(substitution) {
    "use strict";

    return substitution({
        // Do not use this file: it will be overwritten by its analogue from the hod or idol modules.
        // Its presence here is required by tests.
        'error.code.DUMMYERRORCODE123': 'Prettified dummy error message'// Dummy error for jasmine tests
    });
});
