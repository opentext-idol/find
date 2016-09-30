// TODO also used in IDOL Admin - consider moving to library
define([
    'js-whatever/js/list-view'
], function(ListView) {

    var monitorChange = function(fn) {
        return function() {
            var oldValue = this.getValue();
            var output = fn.apply(this, arguments);
            var newValue = this.getValue();

            if (newValue !== oldValue) {
                this.fireChange(newValue);
            }

            return output;
        };
    };

    var preventChange = function(fn) {
        return function() {
            var value = this.getValue();
            var output = fn.apply(this, arguments);
            this.setValue(value, true);
            return output;
        };
    };

    var ValuedListView = ListView.extend({
        getValue: $.noop,

        // Arguments:
        // 1 new value
        // 2 silent flag: if true, should not fire change event
        setValue: $.noop,

        render: monitorChange(ListView.prototype.render),
        onAdd: monitorChange(ListView.prototype.onAdd),
        onChange: monitorChange(preventChange(ListView.prototype.onChange)),
        onRemove: monitorChange(ListView.prototype.onRemove),
        onSort: preventChange(ListView.prototype.onSort),

        fireChange: function(value) {
            this.trigger('change', value);
        }
    });

    ValuedListView.monitorChange = monitorChange;
    ValuedListView.preventChange = preventChange;

    return ValuedListView;

});