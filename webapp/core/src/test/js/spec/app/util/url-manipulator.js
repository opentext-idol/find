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
    'backbone',
    'find/app/util/url-manipulator',
    'find/app/configuration'
], function(Backbone, urlManipulator, configuration) {
    'use strict';

    const url = 'http://myUrl';
    const prefix = 'myPrefix:';
    const contentTypeWithPrefix = 'someType';
    const contentTypeWithoutPrefix = 'someOtherType';
    const urlWithHashFragment = 'http://myUrl#hashfragment';
    const modelWithUrlWithHashFragment = new Backbone.Model({ reference: urlWithHashFragment });
    const modelWithUrlWithoutHashFragment = new Backbone.Model({ reference: url });

    describe('Url Manipulator', function() {
        beforeEach(function() {
            configuration.and.returnValue({
                uiCustomization: {
                    specialUrlPrefixes: {
                        someType: prefix
                    }
                }
            });
        });

        describe('when adding special url prefixes', function() {
            it('should add url to prefix for configured content type', function() {
                expect(urlManipulator.addSpecialUrlPrefix(contentTypeWithPrefix, url)).toEqual(prefix + url);
            });

            it('should not add url to prefix for other content type', function() {
                expect(urlManipulator.addSpecialUrlPrefix(contentTypeWithoutPrefix, url)).toEqual(url);
            });

            it('should handle no prefix definitions', function() {
                configuration.and.returnValue({
                    uiCustomization: {}
                });
                expect(urlManipulator.addSpecialUrlPrefix(contentTypeWithPrefix, url)).toEqual(url);
            });

            it('should handle no ui customization', function() {
                configuration.and.returnValue({});
                expect(urlManipulator.addSpecialUrlPrefix(contentTypeWithPrefix, url)).toEqual(url);
            });
        });

        describe('when appending hash fragments', function() {
            it('should re-append any url fragments to the url', function() {
                expect(urlManipulator.appendHashFragment(modelWithUrlWithHashFragment, url)).toEqual(urlWithHashFragment);
            });

            it('if there are no url fragments should not append anything to the url', function() {
                expect(urlManipulator.appendHashFragment(modelWithUrlWithoutHashFragment, url)).toEqual(url);
            });
        });
    });
});
