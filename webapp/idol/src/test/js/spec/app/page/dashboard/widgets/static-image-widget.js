/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/idol/app/page/dashboard/widgets/static-image-widget',
    'html2canvas'
], function(StaticImageWidget, html2canvas) {
    'use strict';

    describe('Static Image Widget', function() {
        beforeEach(function() {
            this.widget = new StaticImageWidget({
                name: 'Test Widget',
                widgetSettings: {
                    url: 'http://placehold.it/800x300'
                }
            });

            this.widget.render();
        });

        afterEach(function() {
            this.widget.remove();
        });

        it('should export data', function() {
            this.widget.exportData();
            expect(html2canvas).toHaveBeenCalled();
        });
    });
});