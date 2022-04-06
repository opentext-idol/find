/*
 * (c) Copyright 2015-2016 Micro Focus or one of its affiliates.
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
    'find/app/util/parse-url'
], function(parseUrl) {

    describe('parseUrl', function() {
        describe('with an absolute URL', function() {
            beforeEach(function() {
                this.output = parseUrl('http://www.example.com:1337/foo/bar?query=monkey#results');
            });

            it('parses the hash', function() {
                expect(this.output.hash).toBe('#results');
            });

            it('parses the host', function() {
                expect(this.output.host).toBe('www.example.com:1337');
            });

            it('parses the hostname', function() {
                expect(this.output.hostname).toBe('www.example.com');
            });

            it('parses the href', function() {
                expect(this.output.href).toBe('http://www.example.com:1337/foo/bar?query=monkey#results');
            });

            it('parses the origin', function() {
                expect(this.output.origin).toBe('http://www.example.com:1337');
            });

            it('parses the pathname', function() {
                expect(this.output.pathname).toBe('/foo/bar');
            });

            it('parses the port', function() {
                expect(this.output.port).toBe(1337);
            });

            it('parses the protocol', function() {
                expect(this.output.protocol).toBe('http:');
            });

            it('parses the search', function() {
                expect(this.output.search).toBe('?query=monkey');
            });
        });

        describe('with a relative URL path', function() {
            beforeEach(function() {
                this.output = parseUrl('foo/bar');
            });

            it('parses the pathname', function() {
                // We can't say more than this because in PhantomJS the document has a file URL
                expect(this.output.pathname.indexOf('/foo/bar') >= 0).toBe(true);
            });

            it('uses the current document host', function() {
                expect(this.output.host).toBe(window.location.host);
            });
        });
    });

});
