/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/app/page/search/document/preview-mode-user-view'
], function($, Backbone, PreviewModeUserView) {
    'use strict';

    const setup = function (context, options) {
        const userModel = new Backbone.Model(options.userAttributes);

        context.previewModeModel = new Backbone.Model({
            mode: 'user',
            user: userModel,
            fields: options.fieldsConfig
        });

        context.view = new PreviewModeUserView({
            model: userModel,
            previewModeModel: context.previewModeModel
        });

        $('body').append(context.view.$el);
        context.view.render();
    }

    const getFieldNodesText = function (context) {
        return context.view.$('.preview-mode-user-container *')
            .filter((i, n) => n.children.length === 0)
            .map((i, n) => n.innerText)
            .toArray();
    }

    describe('Preview Mode User View', function () {

        afterEach(function () {
            this.view.remove();
        })

        describe('preview user', function () {

            beforeEach(function () {
                setup(this, {
                    userAttributes: {
                        uid: 123,
                        username: 'the user',
                        fields: {
                            field_one: '1 val',
                            field2: '2 val',
                            field_c: 'C val'
                        }
                    },
                    fieldsConfig: [
                        { name: 'field_one' },
                        { name: 'field2' },
                        { name: 'field_c' }
                    ]
                });
            });

            it('should display username as title', function () {
                expect(this.view.$('h4').text().trim()).toBe('the user');
            });

            it('should display fields', function () {
                expect(this.view.$('.preview-mode-user-container')).not.toHaveClass('hide');
                expect(getFieldNodesText(this)).toEqual([
                    'User ID', '123',
                    'Field One', '1 val',
                    'Field2', '2 val',
                    'Field C', 'C val'
                ]);
            });

            it('should not display loading spinner', function () {
                expect(this.view.$('.preview-mode-user-loading')).toHaveClass('hide');
            });

            describe('then the Close button is clicked', function () {

                beforeEach(function () {
                    this.view.$('.close-preview-mode').eq(0).click();
                });

                it('should set the preview mode to null', function () {
                    expect(this.previewModeModel.get('mode')).toBe(null);
                });

            });

        });

        describe('preview user with missing field', function () {

            beforeEach(function () {
                setup(this, {
                    userAttributes: {
                        uid: 123,
                        username: 'the user',
                        fields: { field_one: '1 val' }
                    },
                    fieldsConfig: [
                        { name: 'field_one' },
                        { name: 'field_missing' }
                    ]
                });
            });

            it('should not display the missing field', function () {
                expect(getFieldNodesText(this)).toEqual([
                    'User ID', '123',
                    'Field One', '1 val'
                ]);
            });

        });

        describe('preview user with hidden field', function () {

            beforeEach(function () {
                setup(this, {
                    userAttributes: {
                        uid: 123,
                        username: 'the user',
                        fields: {
                            field_one: '1 val',
                            field_hidden: 'hidden val'
                        }
                    },
                    fieldsConfig: [{ name: 'field_one' }]
                });
            });

            it('should not display the hidden field', function () {
                expect(getFieldNodesText(this)).toEqual([
                    'User ID', '123',
                    'Field One', '1 val'
                ]);
            });

        });

        describe('preview user with email', function () {

            beforeEach(function () {
                setup(this, {
                    userAttributes: {
                        uid: 123,
                        username: 'the user',
                        emailaddress: 'user@example.com',
                        fields: { field_one: '1 val' }
                    },
                    fieldsConfig: [{ name: 'field_one' }]
                });
            });

            it('should display the email', function () {
                expect(getFieldNodesText(this)).toEqual([
                    'Email Address', 'user@example.com',
                    'User ID', '123',
                    'Field One', '1 val'
                ]);
            });

        });

    });

});
