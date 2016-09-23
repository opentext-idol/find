define([], function () {
    "use strict";
    return function (collection) {
        return {
            initialize: function (view) {
                view.listenTo(collection, 'update change', view.updateText);
            },
            onTextUpdate: function (updatedText) {
                if (updatedText && updatedText !== '*') {
                    collection.unshift({concepts: [updatedText]});
                }
            },
            onExternalUpdate: function () {
                return '';
            }
        }
    }
});