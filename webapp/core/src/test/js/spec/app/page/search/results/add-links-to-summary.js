/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/results/add-links-to-summary'
], function(addLinksToSummary) {

    describe('addLinksToSummary', function() {
        it('returns the empty string when called with null', function() {
            expect(addLinksToSummary(null)).toBe('');
        });

        it('replaces placeholder tags with spans', function() {
            expect(addLinksToSummary('I love <HavenSearch-QueryText-Placeholder>cat videos</HavenSearch-QueryText-Placeholder>.'))
                .toBe('I love <span class="search-text">cat videos</span>.');
        });
    });

});
