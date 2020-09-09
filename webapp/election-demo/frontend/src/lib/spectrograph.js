import _ from 'underscore';
import $ from 'jquery';
import moment from 'moment';
import Raphael from 'raphael';

export default function (optDeps, uiDeps) {
    /* reference ui components */
    const $window = $(window);
    const $body = $(document.body);
    const $parent = $(uiDeps.parent);
    const $detailsBtn = $(uiDeps.toggleDetailButton);
    const $spectrograph = $(uiDeps.spectrograph);
    const $headingsDom = $(uiDeps.headings);


    /* reference config options */
    const clustersData = optDeps.clustersData;
    const imageWidth = optDeps.imageWidth;
    const imageHeight = optDeps.imageHeight;
    const imageUrl = optDeps.imageUrl;
    const dayCount = optDeps.dayCount;

    const currentCluster = null;

    const dayLength = 60 * 60 * 24;
    const today = Math.floor((+new Date) / (dayLength * 1000));
    const dayReplacements = {};
    dayReplacements[today] = 'Today';
    dayReplacements[today - 1] = 'Yesterday';


    const hidingDelay = 500;
    const interval = (dayCount * 24 * 60 * 60);

    const sgWidth = 512, sgHeight = 512;
    const datePadding = 70;
    const clusterDateFormat = 'dddd'; // for momentjs
    const width = imageWidth;
    const columnWidth = Math.floor(width / dayCount);
    const height = imageHeight + datePadding;
    const paper = Raphael($spectrograph[0], width, height + 100);

    $parent.width(width);
    $headingsDom.width(width);

    paper.image(imageUrl, 0, datePadding, imageWidth, imageHeight);
    const xScale = imageWidth / sgWidth, yScale = imageHeight / sgHeight;

    const debug = window.location.search.match(/[?&]debug=true(&|$)/i);

    const dateEls = {};

    function warn(args) {
        debug && console && console.log && console.log.apply(console, args);
    }

    const dedupe = {};
    const groups = {};
    const days = {};
    let latestDay = -Infinity;

    const asDay = function (result) {
        return Math.ceil(result / dayLength) * dayLength;
    };

    clustersData.forEach(function (cluster) {
        const fromdate = cluster.fromDate;
        const todate = cluster.toDate;
        const x1 = cluster.x1;
        const x2 = cluster.x2;
        const y1 = cluster.y1;
        const y2 = cluster.y2;
        const title = cluster.title;

        const groupKey = fromdate + ':' + y1;

        const key = [fromdate, y1].join('_');
        // based on sgd data, the actual node comes first, continuation links to other nodes come later
        if (!dedupe[key]) {
            let group = groups[groupKey];
            if (!group) {
                cluster.isNew = true;
                group = groups[groupKey] = paper.set();
            }

            const clusterDay = asDay(cluster.fromDate);
            if (latestDay < clusterDay) {
                latestDay = clusterDay;
            }
            days[clusterDay] = (days[clusterDay] || []).concat([cluster]);

            cluster.baseColor = 'rgb(0, 150, 214)';
            cluster.selectedColor = 'rgb(240, 83, 50)';

            cluster.normalState = {
                'fill': cluster.baseColor,
                'fill-opacity': cluster.isNew ? 1 : 0.7,
                'stroke': 'rgb(0, 176, 255)'
            };

            cluster.selectedState = {
                'fill': cluster.selectedColor,
                'fill-opacity': 1,
                'stroke': 'rgb(0, 0, 0)'
            };

            // is an actual node, e.g. from the .sgd
            // 1339828800 CAT_EVERYTHING  19  812 1024  812 1024  violence derailing Observers mission, Moscow, Russia, Syria 26  104 9 20  0 20  3 3 0 1 1
            const textL = cluster.textL = xScale * (x1);
            const textW = cluster.textW = xScale * (x2 - x1);
            const textDom = $('<div class="textwrap">' + _.escape(title) + '</div>').css({
                left: textL,
                width: textW
            }).appendTo($parent);

            const textH = cluster.textH = textDom.height();
            const textT = cluster.textT = datePadding + yScale * y1 - textH * 0.5;
            textDom.css('top', textT);

            const bgRect = paper.rect(textL, textT, textW, textH, 2);
            bgRect.attr({
                fill: cluster.baseColor,
                'fill-opacity': 1,
                stroke: 'rgb(0, 176, 255)'
            });
            bgRect.data('cluster', cluster);

            if (!cluster.isNew) {
                group.attr({ 'fill-opacity': 0.7 });
            }

            dedupe[key] = cluster;

            group.push(bgRect);

            (function () {
                const globalEvents = $(groups);
                const groupData = group.data();
                groupData.persist = 0;

                const showGroup = function () {
                    if (groupData.hideTimeout) {
                        clearTimeout(groupData.hideTimeout);
                        groupData.hideTimeout = null;
                    }

                    setGroupVisible(group, true);
                };

                const attemptToHideGroup = function () {
                    if (groupData.persist > 0) {
                        return;
                    }

                    groupData.hideTimeout = setTimeout(function () {
                        setGroupVisible(group, false);
                    }, hidingDelay);
                };

                textDom.hover(showGroup, attemptToHideGroup);
            })();

            cluster.textEl = textDom;
            cluster.bgRect = bgRect;
            cluster.group = group;

            if (!dateEls[fromdate]) {

                const thisDate = Math.floor(fromdate / dayLength);
                const day = (thisDate in dayReplacements)
                    ? dayReplacements[thisDate]
                    : moment.unix(fromdate).format(clusterDateFormat);

                dateEls[fromdate] = $('<div class="dateheader showdates">' + day + '</div>').css({
                    left: textL,
                    width: textW
                }).appendTo($headingsDom);
            }
        } else {
            // is a continuation node, e.g.
            // 1339828800 CAT_EVERYTHING  19  812 1024  812 1024  violence derailing Observers mission, Moscow, Russia, Syria 26  104 9 20  12  20  3 14  1 1 1
            const sourceGroup = groups[groupKey];
            if (sourceGroup) {
                const destKey = todate + ':' + y2;
                const destGroup = groups[destKey];
                if (destGroup && destGroup !== sourceGroup) {
                    Array.prototype.push.apply(sourceGroup, destGroup);
                }

                groups[destKey] = groups[groupKey];
            } else {
                warn('group for y1 should exist, was null');
            }
        }
    });

    const todays = days[latestDay];

    if (!clustersData.length) {
        $spectrograph.addClass('showdates');
    }

    let showSubchildren = true; // TODO

    const setGroupVisible = function (group, state) {
        if (!showSubchildren) {
            group.forEach(function (bgRect) {
                setDetailVisibility(bgRect.data('cluster'), state);
            });
        }
    };

    const setDetailVisibility = function (cluster, state) {
        const animTime = 300;

        if (!cluster.isNew) {
            cluster.textEl.animate({ opacity: (+state) }, animTime / 2, undefined);
            cluster.bgRect.animate({ opacity: (+state) }, animTime, undefined);
        }
    };

    const toggleDetails = function () {
        showSubchildren = !showSubchildren;

        _.each(dedupe, function (cluster) {
            setDetailVisibility(cluster, showSubchildren)
        });
    };

    if (showSubchildren) {
        toggleDetails();
    }

    if ($detailsBtn) {
        $detailsBtn.toggleClass('active', showSubchildren);
        $detailsBtn.click(function (e) {
            e.preventDefault();
            toggleDetails();
            $detailsBtn.toggleClass('active', showSubchildren);
        });
    }

};
