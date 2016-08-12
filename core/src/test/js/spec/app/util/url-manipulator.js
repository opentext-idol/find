/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/util/url-manipulator',
    'find/app/configuration'
], function(urlManipulator, configuration) {
    "use strict";

    const url = 'http://myUrl';
    const prefix = 'myPrefix:';
    const contentTypeWithPrefix = 'someType';
    const contentTypeWithoutPrefix = 'someOtherType';

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
        
        it('should add url to prefix for configured content type', function() {
            expect(urlManipulator.addSpecialUrlPrefix(contentTypeWithPrefix, url)).toEqual(prefix + url);
        });
        
        it('should not add url to prefix for other content type', function() {
            expect(urlManipulator.addSpecialUrlPrefix(contentTypeWithoutPrefix, url)).toEqual(url);
        });
        
        it('should handle no prefix definitions', function() {
            configuration.and.returnValue({
                uiCustomization: {
                }
            });
            expect(urlManipulator.addSpecialUrlPrefix(contentTypeWithPrefix, url)).toEqual(url);
        });
        
        it('should handle no ui customization', function() {
            configuration.and.returnValue({
            });
            expect(urlManipulator.addSpecialUrlPrefix(contentTypeWithPrefix, url)).toEqual(url);
        });
    });
});