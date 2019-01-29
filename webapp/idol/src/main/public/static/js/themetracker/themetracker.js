/**
 * @fileOverview
 * ThemeTracker visualization. Shows themes obtained from category spectrograph jobs.
 *
 * @see themetracker.html/themetracker-require.html in the sample project as an example of integration
 *      with a working backend.
 */
(function (factory) {
    if (typeof define === 'function' && define.amd) {
        // We're using AMD, e.g. require.js. Register as an anonymous module.
        define(['jquery', 'underscore', 'Raphael', 'moment', 'json2'], factory);
    } else {
        // We're using plain javascript imports, namespace everything in the Autn namespace.
        (function(scope, namespace){
            for (var key, words = namespace.split('.'); key = words.shift();) {
                scope = (scope[key] || (scope[key] = {}));
            }
        })(window, 'autn.vis.themetracker');

        autn.vis.themetracker.ThemeTracker = factory(jQuery, _, Raphael, moment);
    }
}(function ($, _, Raphael, moment) {
    /**
     * Creates a themetracker instance.
     * See themetracker.html/themetracker-require.html in the sample project as an example of integration
     * with a working backend.
     * @class ThemeTracker
     * @memberOf autn.vis.themetracker
     * @param {Object} opts
     * @param {jQuery} opts.$el
     *      jQuery element to render on.
     * @param {Object} opts.jobs
     *      map of category spectrograph job names to human-friendly labels.
     * @param {string} [opts.jobName]
     *      spectrograph job to select, defaults to the first job.
     * @param {boolean} [opts.showDatepickers=false]
     *      If enabled, clicking on the dates on top will show from/end datepickers.
     * @param {number} [opts.fromDate]
     *      Start date when fetching clusters, in epoch seconds.
     * @param {number} [opts.toDate]
     *      End date when fetching clusters, in epoch seconds.
     * @param {number} [opts.width=opts.$el.width()]
     *      Width in pixels.
     * @param {number} [opts.height=opts.$el.height()]
     *      Height in pixels.
     * @param {autn.vis.themetracker.ThemeTracker~jobsSelectorLink} [opts.jobsSelectorLink]
     *      Callback to build the links in the job selector. If null, the job selector menu is disabled.
     * @param {autn.vis.themetracker.ThemeTracker~doClusterImageFetch} [opts.doClusterImageFetch]
     *      Callback to fetch the spectrograph image for the themetracker.
     * @param {autn.vis.themetracker.ThemeTracker~doClustersFetch} [opts.doClustersFetch]
     *      Callback to fetch the data for the themetracker.
     * @param {Object} [opts.images]
     *      Map of image paths.
     * @param {String} [opts.images.details-btn='lib/autn/vis/themetracker/img/cluster-detail.png']
     *      Image URL for the button which toggles the visibility of clusters which aren't the start of a new theme.
     * @example
     * <pre><code>
     new autn.vis.themetracker.ThemeTracker({
    $el: $('#themetracker'),
    jobs: {
        "MDN_CEN": "All",
        "CAT_BUSINESS_CEN": "Business"
    }
})
     // or if using require.js
     require('autn/vis/themetracker/themetracker', function(ThemeTracker){
    ThemeTracker({
        //...
    });
})
     * </code></pre>
     */

    return function(opts){
        opts = $.extend({
            $el: undefined,
            jobs: undefined,
            jobName: undefined,
            fromDate: undefined,
            toDate: undefined,
            width: undefined,
            height: undefined,
            /**
             * Builds URLs for links in the job selector.
             * @callback autn.vis.themetracker.ThemeTracker~jobsSelectorLink
             * @param {Object} job
             * @param {string} job.jobName the spectrograph job name which should be used.
             * @param {number} fromDate
             *      any fromDate filter currently in use.
             * @param {number} toDate
             *      any toDate filter currently in use.
             * @returns {string}
             *      URL to a page which will show the specified job using the specified date filters.
             * @example
             * <pre><code>
             new autn.vis.themetracker.ThemeTracker({
    // ...
    jobsSelectorLink: function(job, fromDate, toDate){
        var params = { jobname: job.jobName };

        if (fromDate) {
            params.fromdate = fromDate * 1000;
        }
        if (toDate) {
            params.todate = toDate * 1000;
        }

        return '?' + $.param(params);
    }
});
             </code></pre>
             */
            jobsSelectorLink: undefined,
            /**
             * @callback autn.vis.themetracker.ThemeTracker~doClusterImageFetch
             * Fetches the spectrograph image for the themetracker.
             * @param {Object} params
             * @param {string} params.jobName the spectrograph job.
             * @param {number} params.startDate the start date in epoch seconds.
             * @param {number} params.interval the interval to fetch, in seconds.
             *
             * @returns {string} url to image
             * @example
             * <pre><code>
             new autn.vis.themetracker.ThemeTracker({
    // ...
    doClusterImageFetch: function(job, fromDate, toDate){
        return 'themeImage.json?' + $.param(params)
    }
});
             </code></pre>
             */
            doClusterImageFetch: function(params) {
                return 'api/public/themetracker/image?' + $.param(params)
            },
            /**
             * @callback autn.vis.themetracker.ThemeTracker~doClustersFetch
             * Fetches the data for the themetracker
             * @param {Object} params contains jobName, startDate and interval
             * @param {Object} params.jobName the current selected job.
             * @param {number} params.startDate the start date, in epoch seconds.
             * @param {number} params.interval the time span to be fetched, in seconds.
             * @returns {$.Deferred} deferred object which must resolve to spectrograph clusters.
             * @example
             * <pre><code>
             new autn.vis.themetracker.ThemeTracker({
    // ...
    doClustersFetch: function(params){
         return $.ajax('mock.json', {});
    }
})
             // where mock.json looks like
             {"clusters":[{"title":"author Tom Clancy, Tom Clancy dies","fromDate":1380782400,"toDate":1380868800,"numDocs":40,"x1":0,"x2":48,"y1":159,"y2":159,"id":7}]}
             </code></pre>
             */
            doClustersFetch: function(params){
                return $.ajax('api/public/themetracker/clusters', {
                    data: params
                });
            },
            images: {
                'sources-btn': 'lib/autn/vis/themetracker/img/cluster-sources.png',
                'details-btn': 'lib/autn/vis/themetracker/img/cluster-detail.png',
                'view-btn': 'lib/autn/vis/util/img/magnifier.png',
                'email-btn': 'lib/autn/vis/themetracker/img/cluster-email.png',
                'chart-btn': 'lib/autn/vis/themetracker/img/cluster-chart.png'
            }
        }, opts);

        var $container = opts.$el.addClass('autn-vis-themetracker');
        var width = opts.width || $container.width(),
            height = opts.height || $container.height();
        var $dom = $('<div class="autn-vis-themetracker-paper"></div>').width(width).height(height).prependTo($container);

        var jobs = _.map(opts.jobs, function(label, jobName){
            return {
                label: label,
                jobName: jobName.toUpperCase()
            };
        });

        var debug = opts.debug;
        // in epoch-seconds
        var fromDate = opts.fromDate;
        var toDate = opts.toDate;
        var days = opts.days || 7;
        var jobName = opts.jobName || jobs[0].jobName;

        var paper = Raphael($dom[0], width, height);

        var sgWidth = 512, sgHeight = 512;
        var datePadding = 40;
        var imageHeight = height - datePadding;
        var interval = days * 24 * 3600;
        var startDate = fromDate ? fromDate : Math.round(new Date() / 1000) - interval;

        if (toDate) {
            if (!fromDate) {
                startDate = toDate - interval;
            }
            else {
                interval = toDate - startDate;
            }
        }

        // if we're showing more than 10 days, or if the results don't end within the last week, show d/m/y
        var clusterDateFormat = interval <= 86400 ? 'HH:mm' :  interval > 86400*10 ||
        (1000 * (startDate + interval) - new Date()) > 86400 * 7 ? 'MMM DD' : 'dddd';

        var image = paper.image(opts.doClusterImageFetch({startDate: startDate, interval: interval, jobName: jobName}), 0, datePadding, width, imageHeight);
        $(image.node).attr('class', 'autn-vis-themetracker-cluster-image');
        var xScale = width / sgWidth, yScale = imageHeight / sgHeight;

        var dateEls = {};

        function warn(args) {
            debug && alert(args);
        }

        var updateSubchildren = function(){};
        var showSubchildren = true;

        this.toggleDetails  = function(show) {
            showSubchildren = show === undefined ? !showSubchildren : !!show;

            updateSubchildren.apply(this, arguments);
        };

        opts.doClustersFetch({startDate: startDate, jobName: jobName, interval: interval}).done(function(json) {
            $('<div class="autn-vis-themetracker-date-background">').appendTo($dom);

            var dedupe = {};
            var groups = {};

            _.each(json.clusters, function(cluster){
                var fromdate = cluster.fromDate;
                var todate = cluster.toDate;
                var x1 = cluster.x1;
                var x2 = cluster.x2;
                var y1 = cluster.y1;
                var y2 = cluster.y2;
                var title = cluster.title;

                var groupKey = fromdate + ':' + y1;

                var key = [fromdate,y1].join('_');
                // based on sgd data, the actual node comes first, continuation links to other nodes come later
                if (!dedupe[key]) {
                    var group = groups[groupKey];
                    if (!group) {
                        cluster.isNew = true;
                        group = groups[groupKey] = paper.set();
                    }

                    cluster.baseColor = cluster.isNew ? 'orange' : 'teal';

                    // is an actual node, e.g. from the .sgd
                    // 1339828800	CAT_EVERYTHING	19	812	1024	812	1024	violence derailing Observers mission, Moscow, Russia, Syria	26	104	9	20	0	20	3	3	0	1	1
                    var textL = cluster.textL = xScale * (x1);
                    var textW = cluster.textW = xScale * (x2-x1);

                    // Nudge components slightly right if they're too close to the left edge.
                    if (textL < 5) {
                        textL += 5;
                    }

                    var textDom = $('<div class="autn-vis-themetracker-textwrap">'+_.escape(title)+'</div>').css({
//                                'background-color': 'rgba(255,0,0,0.25)' doesn't work in ie, so we use raphael semitransparent rounded rectangles instead
                        left: textL,
                        width: textW
                    }).appendTo($container);

                    if (cluster.isNew) {
                        textDom.css('font-weight', 'bold');
                    }

                    var textH = cluster.textH = textDom.height();
                    var textT = cluster.textT = datePadding + yScale * y1 - textH * 0.5;
                    textDom.css('top', textT);

                    var bgRect = paper.rect(textL, textT, textW, textH, 0).attr({fill: cluster.baseColor, 'fill-opacity': 0.5, stroke: 'black'}).data('cluster', cluster);

                    dedupe[key] = cluster;

                    group.push(bgRect);

                    textDom.hover(function() {
                        group.attr({fill: 'orange', 'fill-opacity': 0.5, stroke: 'orange'});
                    }, function() {
                        _.each(group, function(bgRect){
                            bgRect.attr({fill: bgRect.data('cluster').baseColor, 'fill-opacity': 0.5, stroke: 'black'});
                        });
                    }).click(function(evt){
                        evt.preventDefault();
                        evt.stopPropagation();
                        onClusterClick(evt, cluster, group);
                    });

                    cluster.textEl = textDom;
                    cluster.bgRect = bgRect;

                    if (!dateEls[fromdate]) {
                        var day = moment(new Date(fromdate * 1000)).format(clusterDateFormat);
                        dateEls[fromdate] = $('<div class="autn-vis-themetracker-dateheader autn-vis-themetracker-showdates">'+day+'</div>').css({
                            left: textL,
                            width: textW
                        }).appendTo($container);
                    }
                }
                else {
                    // is a continuation node, e.g.
                    // 1339828800	CAT_EVERYTHING	19	812	1024	812	1024	violence derailing Observers mission, Moscow, Russia, Syria	26	104	9	20	12	20	3	14	1	1	1
                    var sourceGroup = groups[groupKey];
                    if (sourceGroup) {
                        var destKey = todate + ':' + y2;
                        var destGroup = groups[destKey];
                        if (destGroup && destGroup !== sourceGroup) {
                            Array.prototype.push.apply(sourceGroup, destGroup);
                        }

                        groups[destKey] = groups[groupKey];
                    }
                    else {
                        warn('group for y1 should exist, was null');
                    }
                }
            });

            if (!json.clusters.length) {
                $dom.addClass('autn-vis-themetracker-showdates');
            }

            updateSubchildren = function(){
                for (var key in dedupe) {
                    var cluster = dedupe[key];
                    var animTime = 200;
                    if (!cluster.isNew) {
                        if (showSubchildren) {
                            cluster.bgRect.animate({opacity: 1}, animTime, undefined, $.proxy(cluster.textEl.show, cluster.textEl));
                        }
                        else {
                            cluster.textEl.hide();
                            cluster.bgRect.animate({opacity: 0}, animTime, undefined);
                        }
                    }
                }
            }

            updateSubchildren();
        }).fail(function(){
            $dom.addClass('autn-vis-themetracker-showdates');
        });

        function onClusterClick(evt, cluster, group) {
            $.ajax('api/public/themetracker/terms', {
                contentType: 'application/json',
                data: JSON.stringify({
                    fromDate: cluster.fromDate,
                    toDate: cluster.toDate,
                    jobName: cluster.jobName,
                    id: cluster.id
                }),
                type: 'POST',
                dataType: 'json',
            }).done(function(terms){
                // If there's more than 40 terms, we throw the rest away.
                // When clustering, extra terms are useful, but we don't need them for querying, and if there's too ]
                //   many then we'd get a too-many-terms error.
                var query = ['"' + cluster.title + '"'].concat(terms.slice(0, 40)).join('\n')
                window.open('public/search/query/' + encodeURIComponent(query), '_blank')
            })
        }
    }
}));