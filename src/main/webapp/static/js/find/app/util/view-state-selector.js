define([
    'underscore'
], function(_) {
    return function(viewStates) {
        return {
            viewStates: viewStates,

            showViewStates: function (showStates) {
                // Hide all the states
                _.each(this.viewStates, function (value, key) {
                    value.addClass('hide');
                }, this);

                // Show the states asked for
                _.each(showStates, function (stateKey) {
                    this.viewStates[stateKey].removeClass('hide');
                }, this)
            }
        }
    }
});