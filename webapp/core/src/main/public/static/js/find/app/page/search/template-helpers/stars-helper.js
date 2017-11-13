/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'handlebars',
    'underscore'
], function(Handlebars, _) {

    return function(rating, reference, database, cssClasses) {
        if (rating === false) {
            return new Handlebars.SafeString('');
        }

        // The rating is undefined if unrated, or 1-5 stars if rated.
        let str = '';
        for (let ii = 1; ii <= 5; ++ii) {
            // use filled stars for the rating, and empty stars as placeholders
            str += '<span class="'+cssClasses+'" data-rating="'+ii+'" data-reference="'+_.escape(reference)+'" data-database="'+_.escape(database)+'">' + (rating >= ii ? '★' : '☆') + '</span>';
        }
        const rounded = Math.round(rating);
        const round10 = Math.round(10 * rating) / 10;
        if (isFinite(round10) && rounded !== round10) {
            str += '<span class="'+cssClasses+'-text">' + round10 + '</span>';
        }
        return new Handlebars.SafeString(str);
    };

});
