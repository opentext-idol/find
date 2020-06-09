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
    'jquery',
    'find/idol/app/page/dashboard/widgets/static-content-widget'
], function($, StaticContentWidget) {
    'use strict';

    describe('Static Content Widget', function() {
        beforeEach(function(done) {
            this.widget = new StaticContentWidget({
                name: 'Test Widget',
                widgetSettings: {
                    html: '<div><p style="font-weight: bold; font-style: italic">I love cheese</p><p>cheese is the best</p></div>'
                }
            });
            this.widget.render();

            $.when(this.widget.exportData()).done(function(data) {
                this.exportData = data;
                done();
            }.bind(this))
        });

        it('should export data', function() {
            expect(this.exportData.type).toBe('text');
            expect(this.exportData.data.text[0].text).toBe('I love cheese\n');
            expect(this.exportData.data.text[0].bold).toBe(true);
            expect(this.exportData.data.text[0].italic).toBe(true);
            expect(this.exportData.data.text[1].text).toBe('cheese is the best');
            expect(this.exportData.data.text[1].bold).toBe(false);
            expect(this.exportData.data.text[1].italic).toBe(false);
        });
    });
});
