define([
    'backbone'
], function(Backbone) {

    return Backbone.Model.extend({
        defaults: {
            selectedSearchCid: null,
            inputText: '',
            relatedConcepts: []
        }
    });

});
