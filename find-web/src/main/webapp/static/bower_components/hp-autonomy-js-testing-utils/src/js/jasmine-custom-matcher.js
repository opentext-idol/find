/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

(function(jasmine, $){

    jasmine.Matchers.prototype.toBeStrictlyAscendingOrderedDropDown = function(){
        var prev = Number.NEGATIVE_INFINITY;

        return _.every(this.actual, function(option){
            var optionValue = Number($(option).val());
            var isNewValueBigger =  optionValue > prev;
            prev = optionValue;

            return isNewValueBigger;
        });
    };

    jasmine.Matchers.prototype.toBeAscendingOrderedDropDown = function(){
        var prev = Number.NEGATIVE_INFINITY;

        return _.every(this.actual, function(option){
            var optionValue = Number($(option).val());

            var isNewValueBigger =  optionValue >= prev;
            prev = optionValue;

            return isNewValueBigger;
        });
    };

    jasmine.Matchers.prototype.toBeStrictlyDescendingOrderedDropDown = function(){
        var prev = Number.MAX_VALUE;

        return _.every(this.actual, function(option){
            var optionValue = Number($(option).val());
            var isNewValueBigger =  optionValue < prev;
            prev = optionValue;

            return isNewValueBigger;
        });
    };

    jasmine.Matchers.prototype.toBeDescendingOrderedDropDown = function(){
        var prev = Number.MAX_VALUE;

        return _.every(this.actual, function(option){
            var optionValue = Number($(option).val());
            var isNewValueBigger =  optionValue <= prev;
            prev = optionValue;

            return isNewValueBigger;
        });
    };

    jasmine.Matchers.prototype.toHaveCallCount = function(n) {
        if (!jasmine.isSpy(this.actual)) {
            throw new Error('Expected a spy, but got ' + jasmine.pp(this.actual) + '.');
        }

        var callCount = this.actual.calls ? this.actual.calls.length : 0;

        this.message = function() {
            return [
                'Expected spy ' + this.actual.identity + ' to have been called ' + n +' times, but it was called ' + callCount + ' times.',
                'Expected spy ' + this.actual.identity + ' not to have been called ' + n +' times.'
            ];
        };

        return callCount === n;
    };

    jasmine.Matchers.prototype.toBeInstanceOf = function(obj) {
        this.message = [
            'Expected ' + JSON.stringify(obj) + ' to be an instance of ' + this.actual + '.',
            'Expected ' + JSON.stringify(obj) + ' not to be an instance of ' + this.actual + '.'
        ];

        return (this.actual instanceof obj);
    };

}(window.jasmine, window.jQuery));