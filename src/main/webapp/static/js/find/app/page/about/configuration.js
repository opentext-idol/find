define([
    'text!find/app/page/about/version.txt'
], function(version){
    return {
        about: {
            version: 'Find 1.0.0',
            build: version
        }
    };
});