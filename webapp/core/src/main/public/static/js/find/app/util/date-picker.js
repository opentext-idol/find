define(['underscore', 'moment'], function (_, moment) {
    let DATE_WIDGET_FORMAT;

    const render = function ($el, onSubmit) {
        $el.datetimepicker({
            format: DATE_WIDGET_FORMAT,
            icons: {
                time: 'hp-icon hp-fw hp-clock',
                date: 'hp-icon hp-fw hp-calendar',
                up: 'hp-icon hp-fw hp-chevron-up',
                down: 'hp-icon hp-fw hp-chevron-down',
                next: 'hp-icon hp-fw hp-chevron-right',
                previous: 'hp-icon hp-fw hp-chevron-left',
                today: 'hp-icon hp-fw hp-icon-vulnerability',
                clear: 'hp-icon hp-fw hp-icon-trash',
                close: 'hp-icon hp-fw hp-icon-close'
            },
            keyBinds: {
                // We customise 'enter' so we can trigger manual updates on 'enter' which the datetimepicker does not do by default
                enter: function (widget) {
                    if (widget && widget.find('.datepicker').is(':visible')) {
                        //noinspection JSUnresolvedFunction
                        this.hide();
                    } else {
                        //noinspection JSUnresolvedFunction
                        onSubmit();
                    }
                }
            }
        });
    };

    const configureLocale = function () {
        _.find(window.navigator.languages, function (lang) {
            moment.locale(lang);
            if (moment.locale().toLowerCase() === lang.toLowerCase()) {
                return true;
            }
        });

        DATE_WIDGET_FORMAT = moment.localeData().longDateFormat('L') + ' ' +
            moment.localeData().longDateFormat('LTS');
    }

    configureLocale();

    return {
        DATE_WIDGET_FORMAT: DATE_WIDGET_FORMAT,
        render: render
    }
});
