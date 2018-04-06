define([
    'peg',
    'text!find/app/util/geoindex/idol-wkt.pegjs'
], function(Peg, grammar) {
    return Peg.generate(grammar);
});