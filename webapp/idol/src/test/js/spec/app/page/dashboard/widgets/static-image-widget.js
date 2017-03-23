/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'find/idol/app/page/dashboard/widgets/static-image-widget'
], function(_, $, StaticImageWidget) {
    "use strict";

    describe('Static Image Widget', function() {
        beforeEach(function (done) {
            this.widget = new StaticImageWidget({
                name: 'Test Widget',
                widgetSettings: {
                    url: 'http://placehold.it/800x300'
                }
            });
            this.widget.render();

            $.when(this.widget.exportData()).done(function (data) {
                this.exportData = data;
                done();
            }.bind(this))
        });

        it('should export data', function() {
            expect(this.exportData.type).toBe('map');
        });
    });
});