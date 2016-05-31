/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
   'find/app/util/events',
   'jasmine-ajax'
], function(events) {
    "use strict";

    describe('Events', function() {
        beforeEach(function() {
            jasmine.Ajax.install();

            this.requests = jasmine.Ajax.requests;
        });

        afterEach(function() {
            jasmine.Ajax.uninstall();
        });

        describe('with a single instance', function() {
            beforeEach(function() {
                this.events = events('some-cid')
            });

            afterEach(function() {
                this.events.abandon();
            });

            it('should track pagination events', function() {
                this.events.reset('cat man', false);

                this.events.page(2);

                expect(this.requests.count()).toBe(1);

                var params = JSON.parse(this.requests.mostRecent().params);
                expect(params).toEqual({page: 2, type: 'page', search: 'cat man'});

                this.events.page(3);

                expect(this.requests.count()).toBe(2);

                var params2 = JSON.parse(this.requests.mostRecent().params);
                expect(params2).toEqual({page: 3, type: 'page', search: 'cat man'});
            });

            describe('click through tracking', function() {
                beforeEach(function () {
                    this.events.reset('dog man', false);
                });

                it('should track a single preview', function() {
                    this.events.preview(5);

                    expect(this.requests.count()).toBe(1);

                    var params = JSON.parse(this.requests.mostRecent().params);
                    expect(params).toEqual({'click-type': 'preview', type: 'clickthrough', search: 'dog man', position: 5});
                });

                it('should track a preview leading to a full preview', function() {
                    this.events.preview(5);
                    this.events.fullPreview();

                    expect(this.requests.count()).toBe(2);

                    var params0 = JSON.parse(this.requests.at(0).params);
                    expect(params0).toEqual({'click-type': 'preview', type: 'clickthrough', search: 'dog man', position: 5});

                    var params1 = JSON.parse(this.requests.at(1).params);
                    expect(params1).toEqual({'click-type': 'full_preview', type: 'clickthrough', search: 'dog man', position: 5});
                });

                it('should track a preview leading to a full preview leading to an original', function() {
                    this.events.preview(5);
                    this.events.fullPreview();
                    this.events.original();

                    expect(this.requests.count()).toBe(3);

                    var params0 = JSON.parse(this.requests.at(0).params);
                    expect(params0).toEqual({'click-type': 'preview', type: 'clickthrough', search: 'dog man', position: 5});

                    var params1 = JSON.parse(this.requests.at(1).params);
                    expect(params1).toEqual({'click-type': 'full_preview', type: 'clickthrough', search: 'dog man', position: 5});

                    var params2 = JSON.parse(this.requests.at(2).params);
                    expect(params2).toEqual({'click-type': 'original', type: 'clickthrough', search: 'dog man', position: 5});
                });
            });

            describe('abandonments', function() {
                beforeEach(function () {
                    this.events.reset('batman', false);
                });

                it('should track abandonments if no results are clicked', function() {
                    this.events.reset('horse man');

                    expect(this.requests.count()).toBe(3);

                    var params = _.chain(this.requests.filter(/.*?\/stats/)).pluck('params').map(JSON.parse).value();

                    expect(params).toContain({'click-type': 'preview', type: 'abandonment', search: 'batman'});
                    expect(params).toContain({'click-type': 'full_preview', type: 'abandonment', search: 'batman'});
                    expect(params).toContain({'click-type': 'original', type: 'abandonment', search: 'batman'});
                });

                it('should track abandonments if a preview clicked', function() {
                    this.events.preview(7);

                    this.events.reset('horse man');

                    expect(this.requests.count()).toBe(3);

                    var params = _.chain(this.requests.filter(/.*?\/stats/)).pluck('params').map(JSON.parse).value();

                    expect(params).toContain({'click-type': 'preview', type: 'clickthrough', search: 'batman', position: 7});

                    expect(params).toContain({'click-type': 'full_preview', type: 'abandonment', search: 'batman'});
                    expect(params).toContain({'click-type': 'original', type: 'abandonment', search: 'batman'});
                });

                it('should track abandonments if a full preview clicked', function() {
                    this.events.preview(7);
                    this.events.fullPreview();

                    this.events.reset('horse man');

                    expect(this.requests.count()).toBe(3);

                    var params = _.chain(this.requests.filter(/.*?\/stats/)).pluck('params').map(JSON.parse).value();

                    expect(params).toContain({'click-type': 'preview', type: 'clickthrough', search: 'batman', position: 7});
                    expect(params).toContain({'click-type': 'full_preview', type: 'clickthrough', search: 'batman', position: 7});

                    expect(params).toContain({'click-type': 'original', type: 'abandonment', search: 'batman'});
                });

                it('should not track abandonments if an original is clicked', function() {
                    this.events.preview(7);
                    this.events.fullPreview();
                    this.events.original();

                    this.events.reset('horse man');

                    expect(this.requests.count()).toBe(3);

                    var params = _.chain(this.requests.filter(/.*?\/stats/)).pluck('params').map(JSON.parse).value();

                    expect(params).toContain({'click-type': 'preview', type: 'clickthrough', search: 'batman', position: 7});
                    expect(params).toContain({'click-type': 'full_preview', type: 'clickthrough', search: 'batman', position: 7});
                    expect(params).toContain({'click-type': 'original', type: 'clickthrough', search: 'batman', position: 7});
                });

                it('should track abandonments if no results are clicked regardless of pagination', function() {
                    this.events.page(2);

                    this.events.reset('horse man');

                    expect(this.requests.count()).toBe(4);

                    expect(JSON.parse(this.requests.first().params)).toEqual({page: 2, type: 'page', search: 'batman'});

                    var params = _.chain(this.requests.filter(/.*?\/stats/)).pluck('params').map(JSON.parse).value();

                    expect(params).toContain({'click-type': 'preview', type: 'abandonment', search: 'batman'});
                    expect(params).toContain({'click-type': 'full_preview', type: 'abandonment', search: 'batman'});
                    expect(params).toContain({'click-type': 'original', type: 'abandonment', search: 'batman'});
                });

                it('should log abandonments on unload', function() {
                    $(window).trigger('unload');

                    expect(this.requests.count()).toBe(3);

                    var params = _.chain(this.requests.filter(/.*?\/stats/)).pluck('params').map(JSON.parse).value();

                    expect(params).toContain({'click-type': 'preview', type: 'abandonment', search: 'batman'});
                    expect(params).toContain({'click-type': 'full_preview', type: 'abandonment', search: 'batman'});
                    expect(params).toContain({'click-type': 'original', type: 'abandonment', search: 'batman'});
                });
            })
        });

        describe('with multiple instances', function() {
            beforeEach(function() {
                this.id1 = events('id1');
                this.id2 = events('id2');
            });

            afterEach(function() {
                this.id1.abandon();
                this.id2.abandon();
            });

            it('should track pagination events independently', function() {
                this.id1.reset('cat man', false);
                this.id2.reset('dog man', false);

                this.id1.page(2);
                this.id1.page(3);

                this.id2.page(2);

                expect(this.requests.count()).toBe(3);

                var params = _.chain(this.requests.filter(/.*?\/stats/)).pluck('params').map(JSON.parse).value();

                expect(params).toContain({page: 2, type: 'page', search: 'cat man'});
                expect(params).toContain({page: 3, type: 'page', search: 'cat man'});
                expect(params).toContain({page: 2, type: 'page', search: 'dog man'});
            });

            it('should track click events independently', function() {
                this.id1.reset('bear man', false);
                this.id2.reset('horse man', false);

                this.id1.preview(6);
                this.id1.fullPreview();
                this.id1.original();

                this.id2.preview(7);
                this.id2.fullPreview();

                expect(this.requests.count()).toBe(5);

                var params = _.chain(this.requests.filter(/.*?\/stats/)).pluck('params').map(JSON.parse).value();

                expect(params).toContain({position: 6, type: 'clickthrough', search: 'bear man', 'click-type': 'preview'});
                expect(params).toContain({position: 6, type: 'clickthrough', search: 'bear man', 'click-type': 'full_preview'});
                expect(params).toContain({position: 6, type: 'clickthrough', search: 'bear man', 'click-type': 'original'});

                expect(params).toContain({position: 7, type: 'clickthrough', search: 'horse man', 'click-type': 'preview'});
                expect(params).toContain({position: 7, type: 'clickthrough', search: 'horse man', 'click-type': 'full_preview'});
            });

            it('should track abandonments independently', function() {
                this.id1.reset('batman', false);
                this.id2.reset('goose man', false);

                this.id1.preview(6);
                this.id1.fullPreview();
                this.id1.abandon();

                this.id2.preview(7);
                this.id2.abandon();

                expect(this.requests.count()).toBe(6);

                var params = _.chain(this.requests.filter(/.*?\/stats/)).pluck('params').map(JSON.parse).value();

                expect(params).toContain({position: 6, type: 'clickthrough', search: 'batman', 'click-type': 'preview'});
                expect(params).toContain({position: 6, type: 'clickthrough', search: 'batman', 'click-type': 'full_preview'});
                expect(params).toContain({type: 'abandonment', search: 'batman', 'click-type': 'original'});

                expect(params).toContain({position: 7, type: 'clickthrough', search: 'goose man', 'click-type': 'preview'});
                expect(params).toContain({type: 'abandonment', search: 'goose man', 'click-type': 'full_preview'});
                expect(params).toContain({type: 'abandonment', search: 'goose man', 'click-type': 'original'});
            });
        });

        describe('caching', function() {
            it('should return different instances when different ids are passed in', function() {
                var first = events('foo');
                var second = events('bar');

                expect(first).not.toBe(second);

                first.abandon();
            });

            it('should return the last generated instance when no id is specified', function() {
                var first = events('foo');
                var second = events();

                expect(first).toBe(second);

                var third = events('bar');
                var fourth = events();

                expect(third).toBe(fourth);

                first.abandon();
                third.abandon();
            });

            it('should stop caching instances when they are abandoned', function() {
                var first = events('foo');
                first.abandon();

                var second = events('foo');

                expect(first).not.toBe(second);

                second.abandon();
            });

            it('should deactivate instances when they are abandoned', function() {
                var first = events('foo');
                first.abandon();

                var second = events();

                expect(first).not.toBe(second);

                second.abandon();
            });
        });
    });

});