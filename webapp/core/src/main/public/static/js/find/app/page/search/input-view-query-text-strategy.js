define([], function () {
    "use strict";

    return function (model) {
        return {
            initialize: function (view) {
                view.listenTo(model, 'change:inputText', view.updateText);
            },
            onTextUpdate: function (updatedText) {
                model.set({inputText: updatedText});
            },
            onExternalUpdate: function () {
                return model.get('inputText');
            }
        }
    }
});