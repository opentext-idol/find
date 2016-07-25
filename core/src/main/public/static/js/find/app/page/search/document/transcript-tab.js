/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore'
], function (Backbone, _) {
    'use strict';

    return Backbone.View.extend({
        template: _.template("<p><%-transcript%></p>"),

        render: function () {

            this.$el.html(
                _.chain(this.model.get('transcript').split('\n'))
                    .filter(function (line) {
                        return /\S/.test(line)
                    })
                    .map(function (line) {
                        return '<p>' + _.escape(line) + '</p>';
                    })
                    .value()
            );
        }
    });
});
