const _ = require('underscore');

module.exports = function(viewStates) {
        return function(showStates) {
            // Hide all the states
            _.each(viewStates, function (value, key) {
                value.addClass('hide');
            }, this);

            // Show the states asked for
            _.each(showStates, function (stateKey) {
                viewStates[stateKey].removeClass('hide');
            }, this)
        }
    };
