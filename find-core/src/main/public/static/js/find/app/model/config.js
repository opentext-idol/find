/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/autoload'
], function(Autoload) {
    var Config = Autoload.extend({

        autoload: false,

        url: function(){
            return /\bconfig$/.test(window.location.pathname)
                ? '../api/config/config/config'
                : '../api/useradmin/config/config';
        }
    });

    return new Config();
});
