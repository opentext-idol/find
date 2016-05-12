/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
   'find/app/util/events',
   'jasmine-ajax'
], function(events) {

    describe('Events', function() {
        beforeEach(function() {
            jasmine.Ajax.install();

            this.events = events();
            this.requests = jasmine.Ajax.requests;
        });

        afterEach(function() {
            jasmine.Ajax.uninstall();
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
        })

    });

});