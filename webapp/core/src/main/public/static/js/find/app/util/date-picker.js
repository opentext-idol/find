define([], function () {
    const DATE_WIDGET_FORMAT = 'YYYY-MM-DD HH:mm:ss';
    const render = function ($el, onSubmit) {
        $el.datetimepicker({
            format: DATE_WIDGET_FORMAT,
            icons: {
                time: 'hp-icon hp-fw hp-clock',
                date: 'hp-icon hp-fw hp-calendar',
                up: 'hp-icon hp-fw hp-chevron-up',
                down: 'hp-icon hp-fw hp-chevron-down',
                next: 'hp-icon hp-fw hp-chevron-right',
                previous: 'hp-icon hp-fw hp-chevron-left'
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
    
    return {
        DATE_WIDGET_FORMAT: DATE_WIDGET_FORMAT,
        render: render
    }
});