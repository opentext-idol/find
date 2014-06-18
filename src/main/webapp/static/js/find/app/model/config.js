define([
    'js-utils/js/autoload'
], function(Autoload) {
    var Config = Autoload.extend({

        autoload: false,

        url: function(){
            return /\bconfig\/[\/]*$/.test(window.location.pathname)
                ? '../api/config/config/config'
                : '../api/useradmin/config/config';
        }
    });

    return new Config();
});