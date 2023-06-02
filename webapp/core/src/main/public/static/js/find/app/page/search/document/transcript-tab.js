/*
 * Copyright 2016 Open Text.
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
