/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
