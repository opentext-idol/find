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
    'underscore',
    'find/idol/app/page/dashboard/widgets/widget'
], function(_, Widget) {
    'use strict';

    const settings = {
        widgetSettings: {
            'option1': 'abc',
            'option2': 'xyz'
        }
    };

    describe('Widget', function() {
        beforeEach(function() {
            this.widget = new Widget(_.extend({
                name: 'Test Widget',
            }, settings));
            this.widget.render();
        });

        it('holds a reference to widgetSettings', function() {
            expect(this.widget.widgetSettings).toEqual(settings.widgetSettings);
        });

        it('has an onClick method', function() {
            expect(this.widget.onClick).toBeDefined();
            expect(typeof this.widget.onClick).toEqual('function');
        });

        it('is not clickable by default', function() {
            spyOn(this.widget, 'onClick');
            this.widget.$el.click();
            expect(this.widget.onClick).not.toHaveBeenCalled();
        });

        it('is static by default', function() {
            expect(this.widget.isUpdating()).toEqual(false);
        });

        it('displays its name as a title', function() {
            expect(this.widget.$el.find('.title')).toHaveText('Test Widget');
        });

        describe('when created without a settings hash', function() {
            beforeEach(function() {
                this.widget = new Widget({
                    name: 'Test Widget'
                });
            });

            it('defaults to widgetSettings being an empty object', function() {
                expect(this.widget.widgetSettings).toEqual({});
            });
        });
    });
});
