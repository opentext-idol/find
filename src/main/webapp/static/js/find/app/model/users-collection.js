define([
    'users-page/js/users-collection'
], function(UsersCollection) {

    var urlPostfix = 'users';

    return new (UsersCollection.extend({
        url: /\bconfig\/[\/]*$/.test(window.location.pathname)
            ? '../api/config/config/' + urlPostfix
            : '../api/useradmin/config/' + urlPostfix
    }));

});
