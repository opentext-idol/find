/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
