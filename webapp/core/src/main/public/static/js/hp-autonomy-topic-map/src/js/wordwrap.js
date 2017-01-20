(function (factory) {
    if (typeof define === 'function' && define.amd) {
        // We're using AMD, e.g. require.js. Register as an anonymous module.
        define(['jquery', 'Raphael'], factory);
    } else {
        // We're using plain javascript imports, namespace everything in the Autn namespace.
        (function(scope, namespace){
            for (var key, words = namespace.split('.'); key = words.shift();) {
                scope = (scope[key] || (scope[key] = {}));
            }
        })(window, 'autn.vis.util');

        autn.vis.util.wordWrap = factory(jQuery, Raphael);
    }
}(function ($, Raphael) {
    var sizeMap = {};

    function measureWord(textEl, word, family, size) {
        // font-size differences don't actually make much difference, mainly just to whitespace, may consider
        // just linearly scaling them
        var key = family + ':' + word;
        var existing = sizeMap[key];
        if (existing) {
            return {width: existing[0] * size, height: existing[1] * size};
        }

        if (curText !== word) { textEl.attr('text', word); curText = word; }
        if (curFontFamily !== family) { textEl.attr('font-family', family); curFontFamily = family; }

        var bounds = textEl.getBBox();

        // 1/40
        var wFrac = bounds.width * 0.025;
        var wHeight = bounds.height * 0.025;
        sizeMap[key] = [wFrac, wHeight];
        return {width: wFrac * size, height: wHeight * size};
    }

    function fastLineBreak(textInput, textEl, maxWidth, padPC, fontFamily, fontSize, minFontSize, maxHeight) {
        var bestSize = fontSize, linesWhichFit;
        var lines = fastTryTextLayout(textInput, textEl, maxWidth, padPC, fontFamily, fontSize, maxHeight, minFontSize === fontSize);
        var biggestFitting = Number.MIN_VALUE;

        if (lines.fit) {
            biggestFitting = fontSize;
        } else {
            bestSize = binaryChop(minFontSize, fontSize - 1, function(fontSize) {
                lines = fastTryTextLayout(textInput, textEl, maxWidth, padPC, fontFamily, fontSize, maxHeight, minFontSize === fontSize);
                if (lines.fit && fontSize > biggestFitting) {
                    linesWhichFit = lines;
                    biggestFitting = fontSize;
                }
                return lines.fit;
            });
        }

        return {
            fit: biggestFitting > 0,
            text: (linesWhichFit || lines).text,
            'font-size': bestSize
        };
    }

    function fastTryTextLayout(text, textEl, maxWidth, padPC, fontFamily, fontSize, maxHeight, earlyTermination) {
        var spaceSize = measureWord(textEl, '\u00A0', fontFamily, fontSize);
        var spaceW = spaceSize.width;
        var aveCharH = spaceSize.height;

        if (padPC) {
            var pad = fontSize * padPC;
            maxWidth -= pad;
            maxHeight -= pad;
        }

        var cursorX = 0, cursorY = 0, lines = [], line = '';
        var fit = true;

        for (var ii = 0; ii < text.length; ++ii) {
            var word = text[ii];
            var wordW = measureWord(textEl, word, fontFamily, fontSize).width;

            if (wordW > maxWidth) {
                fit = false;
                if (earlyTermination) {
                    return {fit: false};
                }
                if (line) {
                    lines.push(line);
                    cursorY += aveCharH;
                }
                lines.push(word);
                cursorY += aveCharH;
                cursorX = 0;
                line = '';
            }
            else if (!line) {
                cursorX = wordW;
                line = word;
            }
            else if (cursorX + spaceW + wordW > maxWidth) {
                lines.push(line);
                cursorY += aveCharH;
                cursorX = wordW;
                line = word;
            }
            else {
                cursorX += spaceW + wordW;
                line += '\u00A0' + word;
            }

            if (cursorY > maxHeight) {
                fit = false;
                if (earlyTermination) {
                    return {fit: false};
                }
            }
        }

        if (line) {
            lines.push(line);
            cursorY += aveCharH;

            if (cursorY > maxHeight) {
                fit = false;
                if (earlyTermination) {
                    return {fit: false};
                }
            }
        }
        
        return {fit: fit, text: lines.join('\n')};
    }

    // should return highest integer value which passes testFn, or lowest value otherwise
    function binaryChop(low, high, testFn) {
        while (low < high) {
            var mid = ((low + high) >> 1) + 1;
            if (testFn(mid)) {
                low = mid;
            }
            else {
                high = mid - 1;
            }
        }

        return low;
    }

    function lineBreak(textInput, textEl, maxWidth, padPC, fontSize, minFontSize, maxHeight) {
        if (tryTextLayout(textInput, textEl, maxWidth, padPC, fontSize, maxHeight, true)) {
            return true;
        }

        var lastFontTested;
        var lastFontFit;
        var bestSize = binaryChop(minFontSize, fontSize - 1, function(fontSize){
            lastFontTested = fontSize;
            textEl.attr('font-size', fontSize);
            lastFontFit = tryTextLayout(textInput, textEl, maxWidth, padPC, fontSize, maxHeight, true);
            return lastFontFit;
        });

        if (bestSize !== lastFontTested || !lastFontFit) {
            var fit = tryTextLayout(textInput, textEl, maxWidth, padPC, bestSize, maxHeight, false);

            // Sometimes things don't fit when tested a second time
            return bestSize > minFontSize || fit;
        }

        return true;
    }

    function tryTextLayout(text, textEl, maxWidth, padPC, fontSize, maxHeight, earlyTermination) {
        var tmpText = '';

        if (padPC) {
            var pad = fontSize * padPC;
            maxWidth -= pad;
            maxHeight -= pad;
        }

        var onNewLine = true, fits = true;
        for (var ii = 0; ii < text.length; ++ii) {
            var testText = tmpText + (onNewLine ? text[ii] : ' ' + text[ii]);
            textEl.attr('text', testText);
            var bbox = textEl.getBBox();
            var widthFits = (bbox.width <= maxWidth);
            var heightFits = (bbox.height <= maxHeight);

            if (!heightFits) {
                fits = false;
                if (earlyTermination) {
                    return false;
                }
            }

            if (widthFits) {
                tmpText = testText;
                onNewLine = false;
                continue;
            }

            if (!onNewLine) {
                testText = tmpText + '\n' + text[ii];
                textEl.attr('text', testText);
                bbox = textEl.getBBox();
                widthFits = (bbox.width <=  maxWidth);
                heightFits = (bbox.height <= maxHeight);

                if (!heightFits) {
                    fits = false;
                    if (earlyTermination) {
                        return false;
                    }
                }

                if (widthFits) {
                    onNewLine = false;
                    tmpText = testText;
                    continue;
                }
            }

            fits = false;
            if (earlyTermination) {
                return false;
            }

            tmpText = testText + '\n';
            onNewLine = true;
        }
        textEl.attr('text', tmpText);
        return fits;
    }

    var layoutEl, curFontSize, curText, curFontFamily, curPaper;

    Raphael.eve.on('raphael.clear', function() {
        if (curPaper === this) {
            sizeMap = {};

            if (layoutEl) {
                layoutEl.remove();
                layoutEl = null;
            }
        }
    });

    return Raphael.vml ? function(paper, font, maxWidth, text, padPC, fontSize, minFontSize, maxHeight, textEl) {
        var terms = text.split(' ');

        if (!layoutEl) {
            layoutEl = paper.text(0,0,terms[0]).attr({'font-family': font, 'font-size': 40}).hide();
            curText = text;
            curFontFamily = font;
            curPaper = paper;
        }

        var lineAttrs = fastLineBreak(terms, layoutEl, maxWidth, padPC ? padPC + 0.15 : 0, font, fontSize, minFontSize, maxHeight);

        if (textEl) {
            textEl.attr(lineAttrs);
        }

        return {
            fit: lineAttrs.fit,
            text: lineAttrs.text,
            fontSize: lineAttrs['font-size']
        };
    } : function(paper, font, maxWidth, text, padPC, fontSize, minFontSize, maxHeight, textEl) {
        if (!textEl) {
            // if you call .remove(), the removed flag is set,
            // but if you just do paper.clear(), the removed flag remains unset
            if (!layoutEl) {
                layoutEl = paper.text(0,0,text).attr({'font-family': font, 'font-size': fontSize}).hide();
                curText = text;
                curFontFamily = font;
                curPaper = paper;
            }
            else {
                if (curText !== text) { layoutEl.attr('text', text); curText = text; }
                if (curFontSize !== fontSize) { layoutEl.attr('font-size', fontSize); curFontSize = font; }
                if (curFontFamily !== font) { layoutEl.attr('font-family', font); curFontFamily = font; }
            }
            textEl = layoutEl;
        }

        var fit = lineBreak(text.split(' '), textEl, maxWidth, padPC, fontSize, minFontSize, maxHeight);

        if (textEl === layoutEl) {
            // for speed, only return the text and fontSize if requested to do so
            return {
                fit: fit,
                text: textEl.attr('text'),
                fontSize: textEl.attr('font-size')
            };
        }

        return { fit: fit };
    };
}));
