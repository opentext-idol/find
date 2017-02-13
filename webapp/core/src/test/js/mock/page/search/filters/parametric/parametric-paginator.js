/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    function MockParametricPaginator() {
        MockParametricPaginator.instances.push(this);

        this.stateModel = new Backbone.Model({
            empty: false,
            loading: false,
            error: null
        });

        this.valuesCollection = new Backbone.Collection();

        this.fetchNext = jasmine.createSpy('fetchNext');
        this.toggleSelection = jasmine.createSpy('toggleSelection');
    }

    MockParametricPaginator.reset = function() {
        MockParametricPaginator.instances = [];
    };

    MockParametricPaginator.reset();
    return MockParametricPaginator;

});
