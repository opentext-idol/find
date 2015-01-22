define([
    'js-whatever/js/vent-constructor',
    'find/app/router'
], function(Vent, router) {

    return new Vent(router);

});