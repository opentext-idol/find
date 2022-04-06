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
    'find/app/mmap-tab',
    'underscore',
    'js-whatever/js/location'
], function(mmapTabGenerator, _, location) {
    'use strict';

    const VALID_ATTRIBUTES = {
        mmapUrl: '/media/channels?channel=BBC News&start=2008-03-01T13:00:00Z',
        mmapEventSourceType: 'Channel',
        mmapEventSourceName: 'BBC News',
        mmapEventTime: '2008-03-01T13:00:00Z'
    };

    describe('MMap Tab', function() {
        describe('with valid configuration', function() {
            beforeEach(function() {
                this.mmapTab = mmapTabGenerator({mmapBaseUrl: 'http://some-host:8080'});
            });

            it('should not be supported without valid attributes', function() {
                expect(this.mmapTab.supported({})).toBeFalsy();
            });

            it('should be supported with valid attributes', function() {
                expect(this.mmapTab.supported(VALID_ATTRIBUTES)).toBeTruthy();
            });

            describe('when the tab is opened', function() {
                beforeEach(function() {
                    this.childWindow = {
                        loadCamera: jasmine.createSpy('loadCamera'),
                        loadChannel: jasmine.createSpy('loadChannel'),
                        focus: jasmine.createSpy('focus')
                    };
                    this.open = spyOn(window, 'open').and.returnValue(this.childWindow);

                    this.mmapTab.open(VALID_ATTRIBUTES);
                });

                it('should open a new tab', function() {
                    expect(this.open).toHaveBeenCalled();
                });

                describe('but is not accessible', function() {
                    beforeEach(function() {
                        spyOn(location, 'host').and.returnValue('http://some-other-host:8080');
                    });

                    it('should not be reusable', function() {
                        expect(this.mmapTab.canBeReused()).toBeFalsy();
                    });

                    it('should open a new tab when opened again', function() {
                        this.mmapTab.open(VALID_ATTRIBUTES);
                        expect(this.open).toHaveBeenCalledTimes(2);
                    });
                });

                describe('and is accessible', function() {
                    beforeEach(function() {
                        spyOn(location, 'host').and.returnValue('http://some-host:8080');
                    });

                    it('should be reusable', function() {
                        expect(this.mmapTab.canBeReused()).toBeTruthy();
                    });

                    const sourceTypeTests = [{
                        sourceType: 'Channel',
                        attributes: VALID_ATTRIBUTES,
                        loadFunction: 'loadChannel'
                    }, {
                        sourceType: 'Camera',
                        attributes: _.defaults({mmapEventSourceType: 'Camera'}, VALID_ATTRIBUTES),
                        loadFunction: 'loadCamera'
                    }];
                    sourceTypeTests.forEach(function(sourceTypeTest) {
                        describe('when the tab is opened again with type ' + sourceTypeTest.sourceType, function() {
                            beforeEach(function() {
                                this.mmapTab.open(sourceTypeTest.attributes);
                            });

                            it('should not open a new tab', function() {
                                expect(this.open).toHaveBeenCalledTimes(1);
                            });

                            it('should load the correct source', function() {
                                expect(this.childWindow[sourceTypeTest.loadFunction]).toHaveBeenCalledWith(VALID_ATTRIBUTES.mmapEventSourceName, VALID_ATTRIBUTES.mmapEventTime);
                            });

                            it('should transfer focus to the child window', function() {
                                expect(this.childWindow.focus).toHaveBeenCalled();
                            });
                        });
                    });
                });
            });
        });

        describe('with configuration on port 80', function() {
            beforeEach(function() {
                this.mmapTab = mmapTabGenerator({mmapBaseUrl: 'http://some-host/'});
                spyOn(location, 'host').and.returnValue('some-host');
            });

            it('should be reusable', function() {
                expect(this.mmapTab.canBeReused()).toBeTruthy();
            });
        });

        describe('Without valid configuration', function() {
            beforeEach(function() {
                this.mmapTab = mmapTabGenerator({});
            });

            it('should not be supported', function() {
                expect(this.mmapTab.supported(VALID_ATTRIBUTES)).toBeFalsy();
            });

            it('should not be reusable', function() {
                expect(this.mmapTab.canBeReused()).toBeFalsy();
            });
        });
    });
});
