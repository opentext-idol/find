define([
    'backbone'
], function(Backbone) {

    var EventsProxy = function(queryModel) {
        this.model = queryModel;

        this.listenTo(this.model, 'all', function(event) {
            if (event !== 'change' && event !== 'refresh' || (this.model.get('queryText'))) {
                this.trigger.apply(this, arguments);
            }
        });
    };

    _.extend(EventsProxy.prototype, Backbone.Events, {

        hasAnyChangedAttributes: function(attributes) {
            return _.any(attributes, function (attr) {
                return _.has(this.changedAttributes(), attr);
            }, this);
        }

    });

    _.each(['get', 'set', 'unset', 'changedAttributes', 'getIsoDate', 'refresh'], function(methodName) {
        EventsProxy.prototype[methodName] = function() {
            return this.model[methodName].apply(this.model, arguments);
        };
    });

    return EventsProxy;
});

