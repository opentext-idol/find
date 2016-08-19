define([
    'backbone',
    'jquery'
], function(Backbone, $) {

    var $body = $(document.body);
    var $window = $(window);

    function updateCallback() {
        this.set({
            innerHeight: $body.innerHeight(),
            scrollTop: $body.scrollTop(),
            scrollHeight: $body.prop('scrollHeight'),
            top: 0,
            bottom: $window.innerHeight()
        });
    }

    // Tracks window scroll parameters
    return Backbone.Model.extend({
        initialize: function() {
            this.updateCallback = updateCallback.bind(this);

            $window
                .on('scroll', this.updateCallback)
                .on('resize', this.updateCallback);

            this.updateCallback();
        },

        shutdown: function() {
            $window
                .off('scroll', this.updateCallback)
                .off('resize', this.updateCallback);
        }
    });

});
