define([
    'js-utils/js/autoload'
], function(Autoload) {
    var CurrentUser = Autoload.extend({

        defaults: {
            username: '',
            roles: []
        },

        url: function(){
            return /\bconfig\/[\/]*$/.test(window.location.pathname)
                ? '../api/config/user/current-user'
                : '../api/user/user/current-user';
        }
    });

    return new CurrentUser();
});