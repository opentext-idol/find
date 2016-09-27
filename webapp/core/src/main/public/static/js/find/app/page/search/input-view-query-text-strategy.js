define([
    'i18n!find/nls/bundle'
], function (i18n) {
    "use strict";

    return function (model) {
        return {
            inputClass: 'search-controls',
            placeholder: i18n['app.searchPlaceholder'],

            initialize: function (view) {
                view.listenTo(model, 'change:inputText', view.updateText);
            },
            onTextUpdate: function (updatedText) {
                model.set({inputText: updatedText});
            },
            onExternalUpdate: function () {
                return model.get('inputText');
            },
            inFocus: $.noop(),
            onBlur: $.noop()
        }
    }
});