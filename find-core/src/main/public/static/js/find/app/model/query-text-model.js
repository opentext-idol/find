/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    return Backbone.Model.extend({
        defaults: {
            inputText: '',
            relatedConcepts: []
        },

        makeQueryText: function(){
            var inputText = this.get('inputText');

            if (!inputText){
                return '';
            }

            if (_.isEmpty(this.get('relatedConcepts'))){
                return inputText;
            }

            return '(' + inputText + ') AND ' + this.get('relatedConcepts').join(' AND ');
        }
    });

});
