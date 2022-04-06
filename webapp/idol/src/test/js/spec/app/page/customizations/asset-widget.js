/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'find/app/page/customizations/asset-widget',
    'i18n!find/nls/bundle'
], function($, Backbone, AssetWidget, i18n) {
    'use strict';

    // data uri for a 1x1 black png
    const DATA_URI = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAadEVYdFNvZnR3YXJlAFBhaW50Lk5FVCB2My41LjExR/NCNwAAAAxJREFUGFdjYGBgAAAABAABXM3/aQAAAABJRU5ErkJggg==';

    describe('Asset widget', function() {
        beforeEach(function() {
            this.collection = new Backbone.Collection();

            this.view = new AssetWidget({
                collection: this.collection,
                description: "The logo goes on the app! It's all so obvious now!",
                height: 100,
                imageClass: 'app-logo',
                title: 'App Logo',
                type: 'APP_LOGO',
                width: 200
            });

            this.view.render();
        });

        it('should have the correct heading', function() {
            expect(this.view.$('.panel-heading').text()).toBe('App Logo')
        });

        it('should have the correct description', function() {
            expect(this.view.$('.panel-body').eq(0).text()).toBe("The logo goes on the app! It's all so obvious now!")
        });

        it('should add the image class to the dropzone', function() {
            expect(this.view.$('.dropzone')).toHaveClass('app-logo');
        });

        it('should allow image files of the given dimensions', function() {
            const previewElement = $('<div>');

            const spy = jasmine.createSpy('complete');

            spyOn(this.view.dropzone, 'createThumbnail');

            const file = {
                height: 100,
                name: 'logo.png',
                previewElement: previewElement[0],
                type: 'image/png',
                width: 200
            };

            this.view.dropzone.emit('addedfile', file);
            this.view.dropzone.accept(file, spy);
            this.view.dropzone.emit('thumbnail', file, DATA_URI);

            expect(spy.calls.argsFor(0)[0]).not.toBeDefined();
        });

        it('should not allow files of other dimensions', function() {
            const previewElement = $('<div>');

            const spy = jasmine.createSpy('complete');

            spyOn(this.view.dropzone, 'createThumbnail');

            const file = {
                height: 2000,
                name: 'big.png',
                previewElement: previewElement[0],
                type: 'image/png',
                width: 4000
            };

            this.view.dropzone.emit('addedfile', file);
            this.view.dropzone.accept(file, spy);
            this.view.dropzone.emit('thumbnail', file, DATA_URI);

            expect(spy.calls.argsFor(0)[0]).toBe(i18n['customizations.fileDimensionsInvalid']);
        });

        it('should not allow files of other types', function() {
            const previewElement = $('<div>');

            const spy = jasmine.createSpy('complete');

            spyOn(this.view.dropzone, 'createThumbnail');

            const file = {
                name: 'foo.txt',
                previewElement: previewElement[0],
                type: 'text/plain'
            };

            this.view.dropzone.emit('addedfile', file);
            this.view.dropzone.accept(file, spy);

            expect(spy.calls.argsFor(0)[0]).toBe(i18n['dropzone.dictInvalidFileType']);
        });

        it('should display known error messages where one exists', function() {
            const previewElement = $('<div><div class="dz-error-message"></div></div>');

            this.view.dropzone.emit('error', {
                previewElement: previewElement[0]
            }, 'IO_ERROR', {
                status: 500
            });

            expect(previewElement.find('.dz-error-message')).toHaveText(i18n['customizations.error.IO_ERROR']);
        });

        it('should display a stock error message for unknown errors', function() {
            const previewElement = $('<div><div class="dz-error-message"></div></div>');

            this.view.dropzone.emit('error', {
                previewElement: previewElement[0]
            }, 'Seymour! The house is on fire!', {
                status: 500
            });

            expect(previewElement.find('.dz-error-message')).toHaveText(i18n['customizations.error.default']);
        });

        it('should add files to the collection when uploaded', function() {
            this.view.dropzone.emit('success', {
                name: 'logo.png'
            });

            expect(this.collection.length).toBe(1);
            expect(this.collection.at(0).id).toBe('logo.png');
        })
    });
});
