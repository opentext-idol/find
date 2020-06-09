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
    'underscore',
    'jquery',
    'backbone',
    'find/app/page/customizations/asset-viewer',
    'jasmine-ajax'
], function(_, $, Backbone, AssetViewer) {
    'use strict';

    describe('Asset Viewer', function() {
        describe('with a single page of assets', function() {
            beforeEach(function() {
                jasmine.Ajax.install();

                this.collection = new Backbone.Collection([
                    {id: '1.png'},
                    {id: '2.png'},
                    {id: 'foo.png'}
                ]);

                this.view = new AssetViewer({
                    collection: this.collection,
                    defaultImage: '/real-logo.png',
                    height: 100,
                    imageClass: 'app-logo',
                    type: 'BIG_LOGO',
                    width: 200
                });

                this.view.render();
            });

            afterEach(function() {
                jasmine.Ajax.uninstall();
            });

            describe('the default asset', function() {
                it('should be rendered', function() {
                    expect(this.view.$('.asset-preview div').eq(0).css('background-image')).toContain('/real-logo.png');
                });

                it('should not have a delete button', function() {
                    expect(this.view.$('.asset-preview div').eq(0).find('.asset-delete')).toHaveLength(0);
                });
            });

            it('should add the image class to the preview', function() {
                expect(this.view.$('.asset-preview div')).toHaveClass('app-logo');
            });

            it('should disable the apply button for the currently selected image', function() {
                expect(this.view.$('[data-id="foo.png"] .asset-apply button')).toHaveClass('disabled');
            });

            it('should show the current asset is selected', function() {
                expect(this.view.$('[data-id="foo.png"] .asset-selected i')).not.toHaveClass('hide');
            });

            it('should not show that assets which are not the current asset are selected', function() {
                expect(this.view.$('[data-id="1.png"] .asset-selected i')).toHaveClass('hide');
            });

            it('should disable the pagination buttons', function() {
                expect(this.view.$('.next')).toHaveClass('disabled');
                expect(this.view.$('.previous')).toHaveClass('disabled');
            });

            describe('when the default asset is enabled', function() {
                beforeEach(function() {
                    this.view.$('.asset:eq(0) .asset-apply button').click();

                    jasmine.Ajax.requests.mostRecent().respondWith({
                        status: 204
                    });
                });

                it('should disable the apply button for the currently selected image', function() {
                    expect(this.view.$('.asset:eq(0) .asset-apply button')).toHaveClass('disabled');
                });

                it('should not disable the apply button for the previous image', function() {
                    expect(this.view.$('[data-id="foo.png"] .asset-apply button')).not.toHaveClass('disabled');
                });

                it('should display a success message', function() {
                    expect(this.view.$message).toHaveClass('text-success');
                });
            });

            describe('when saving a new asset fails', function() {
                beforeEach(function() {
                    this.view.$('.asset:eq(0) .asset-apply button').click();

                    jasmine.Ajax.requests.mostRecent().respondWith({
                        status: 500
                    });
                });

                it('should display an error message', function() {
                    expect(this.view.$message).toHaveClass('text-error');
                });

                it('should not disable the apply button for the currently selected image', function() {
                    expect(this.view.$('.asset:eq(0) .asset-apply button')).not.toHaveClass('disabled');
                });

                it('should disable the apply button for the previous image', function() {
                    expect(this.view.$('[data-id="foo.png"] .asset-apply button')).toHaveClass('disabled');
                });
            })
        });

        describe('with several pages of assets', function() {
            beforeEach(function() {
                this.collection = new Backbone.Collection([{id: 'foo.png'}].concat(_.range(0, 12).map(function(i) {
                    return {id: i + '.png'}
                })));

                this.view = new AssetViewer({
                    collection: this.collection,
                    defaultImage: '/real-logo.png',
                    height: 100,
                    imageClass: 'app-logo',
                    type: 'BIG_LOGO',
                    width: 200
                });

                this.view.render();
            });

            it('should disable the previous button', function() {
                expect(this.view.$('.next')).not.toHaveClass('disabled');
                expect(this.view.$('.previous')).toHaveClass('disabled');
            });

            it('should display the correct number of assets', function() {
                expect(this.view.$('.asset')).toHaveLength(6);
            });

            it('should display the correct assets', function() {
                expect(_.map(this.view.$('.asset'), function(el) {
                    return $(el).attr('data-id')
                })).toEqual([undefined, 'foo.png', '0.png', '1.png', '2.png', '3.png']);
            });

            describe('when the next button is clicked', function() {
                beforeEach(function() {
                    this.view.$('.next').click();
                });

                it('should not disable the pagination buttons', function() {
                    expect(this.view.$('.next')).not.toHaveClass('disabled');
                    expect(this.view.$('.previous')).not.toHaveClass('disabled');
                });

                it('should display the correct number of assets', function() {
                    expect(this.view.$('.asset')).toHaveLength(6);
                });

                it('should display the correct assets', function() {
                    expect(_.map(this.view.$('.asset'), function(el) {
                        return $(el).attr('data-id')
                    })).toEqual([undefined, '4.png', '5.png', '6.png', '7.png', '8.png']);
                });

                describe('and then the next button is clicked again', function() {
                    beforeEach(function() {
                        this.view.$('.next').click();
                    });

                    it('should disable the next button', function() {
                        expect(this.view.$('.next')).toHaveClass('disabled');
                        expect(this.view.$('.previous')).not.toHaveClass('disabled');
                    });

                    it('should display the correct number of assets', function() {
                        expect(this.view.$('.asset')).toHaveLength(4);
                    });

                    it('should display the correct assets', function() {
                        expect(_.map(this.view.$('.asset'), function(el) {
                            return $(el).attr('data-id')
                        })).toEqual([undefined, '9.png', '10.png', '11.png']);
                    });

                    describe('and then the previous button is clicked', function() {
                        beforeEach(function() {
                            this.view.$('.previous').click();
                        });

                        it('should not disable the pagination buttons', function() {
                            expect(this.view.$('.next')).not.toHaveClass('disabled');
                            expect(this.view.$('.previous')).not.toHaveClass('disabled');
                        });

                        it('should display the correct number of assets', function() {
                            expect(this.view.$('.asset')).toHaveLength(6);
                        });
                    })
                })
            });
        });
    });
});
