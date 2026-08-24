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

const _ = require('underscore');

module.exports = function(value, size) {
    // https://www.mediawiki.org/wiki/Common_thumbnail_sizes
    const allowedSizes = [20, 40, 60, 120, 250, 330, 500, 960, 1280, 1920, 3840]
    const defaultedSize = (size || 300)
    const resolvedSize = _.find(allowedSizes, s => s >= defaultedSize) || _.last(allowedSizes)

    return value && String(value).replace(/(\/wikipedia\/(?:commons|\w{2})\/)([^/]+\/[^/]+\/([^/]+\.(jpg|jpeg|gif|png|svg)))/i, function(all, first, second, filename, extension){
        const url = first + 'thumb/' + second + '/' + resolvedSize + 'px-' + filename;

        if (extension && extension.toLowerCase() === 'svg') {
            // The thumbnailer only can raster bitmap outputs, so if we have a svg, we need to convert to e.g. png.
            return url + '.png';
        }

        return url;
    });
};

