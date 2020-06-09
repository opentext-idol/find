/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'find/app/util/generate-error-support-message',
    'find/app/configuration'
], function($, generateErrorHtml, configuration) {
    'use strict';

    function generateDummyError(xhr) {
        return generateErrorHtml({
            errorDetails: xhr.responseJSON.message,
            errorUUID: xhr.responseJSON.uuid,
            errorLookup: xhr.responseJSON.backendErrorCode,
            isUserError: xhr.responseJSON.isUserError
        });
    }

    const DUMMY_UUID = 'dummyuuid';
    const DUMMY_MESSAGE_FOR_USER = 'Message for user';
    const KNOWN_USER_ERROR_CODE = 'DUMMYERRORCODE123';
    const KNOWN_USER_PRETTY_ERROR_MESSAGE = 'Prettified dummy error message';
    const UNKNOWN_ERROR = 'Unknown error';

    describe("The error-generating function", function() {
        beforeEach(function() {
            configuration.and.returnValue(function() {
                return {};
            });
        });

        describe("when passed a user-caused error", function() {
            beforeEach(function() {
                this.xhr = {
                    responseJSON: {
                        backendErrorCode: KNOWN_USER_ERROR_CODE,
                        uuid: DUMMY_UUID,
                        isUserError: true
                    }
                };
                this.testErrorMessage = generateDummyError(this.xhr);
            });
            it("prints prettified error details", function() {
                expect($(this.testErrorMessage)).toContainText(KNOWN_USER_PRETTY_ERROR_MESSAGE);
            });
            it("does not print the error code", function() {
                expect($(this.testErrorMessage).find('.error-details-for-techsupport')).not.toContainText(KNOWN_USER_ERROR_CODE);
            });
            it("does not print a 'call support' message", function() {
                expect($(this.testErrorMessage).find('.error-contact-support')).not.toContainText('support');
            });
        });

        describe("when passed a non-user-caused error", function() {
            beforeEach(function() {
                this.xhr = {
                    responseJSON: {
                        backendErrorCode: 'SOME.ERROR.NOT.IN.errors.js',
                        message: DUMMY_MESSAGE_FOR_USER,
                        uuid: DUMMY_UUID,
                        isUserError: false
                    }
                };
                this.testErrorMessage = generateDummyError(this.xhr);
            });
            it("prints a 'call support' message", function() {
                expect($(this.testErrorMessage).find('.error-contact-support')).toContainText('support');
            });
        });

        describe("when passed an unknown error", function() {
            beforeEach(function() {
                this.xhr = {
                    responseJSON: {
                        backendErrorCode: 'SOME.ERROR.NOT.IN.CORE.errors.js',
                        // message: null,
                        uuid: DUMMY_UUID
                    }
                };
                this.testErrorMessage = generateDummyError(this.xhr);
            });
            it("prints a 'call support' message", function() {
                expect($(this.testErrorMessage).find('.error-contact-support')).toContainText('support');
            });
            it("prints an 'Unknown error' message as details", function() {
                expect($(this.testErrorMessage)).toContainText(UNKNOWN_ERROR);
            });
        });
        describe("when given no error information", function() {
            beforeEach(function() {
                this.testErrorMessageNull = generateErrorHtml(null);
                this.testErrorMessageUndefined = generateErrorHtml(undefined);
                this.testErrorMessageEmpty = generateErrorHtml({});
            });
            it("prints a 'call support' message", function() {
                expect($(this.testErrorMessageNull).find('.error-contact-support')).toContainText('support');
                expect($(this.testErrorMessageUndefined).find('.error-contact-support')).toContainText('support');
                expect($(this.testErrorMessageEmpty).find('.error-contact-support')).toContainText('support');
            });
            it("prints an 'Unknown error' message", function() {
                expect($(this.testErrorMessageNull)).toContainText(UNKNOWN_ERROR);
                expect($(this.testErrorMessageUndefined)).toContainText(UNKNOWN_ERROR);
                expect($(this.testErrorMessageEmpty)).toContainText(UNKNOWN_ERROR);
            });
        });
    });
});
