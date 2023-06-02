/*
 * Copyright 2017-2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore'
], function(_) {

    return function(value, size) {
        return value && String(value).replace(/(\/wikipedia\/(?:commons|\w{2})\/)([^/]+\/[^/]+\/([^/]+\.(jpg|jpeg|gif|png|svg)))/i, function(all, first, second, filename, extension){
            const url = first + 'thumb/' + second + '/' + (size||300) + 'px-' + filename;

            if (extension && extension.toLowerCase() === 'svg') {
                // The thumbnailer only can raster bitmap outputs, so if we have a svg, we need to convert to e.g. png.
                return url + '.png';
            }

            return url;
        });
    };

});
