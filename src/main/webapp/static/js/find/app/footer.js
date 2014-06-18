define([
	'js-utils/js/footer',
    'find/app/footer/footer-pane',
    'find/app/footer/footer-tab',
    'find/app/vent',
    'i18n!find/nls/bundle'
], function(Footer, FooterPane, FooterTab, vent, i18n) {

	return Footer.extend({

        className: Footer.prototype.className + ' footer',

		initialize: function(options) {
			Footer.prototype.initialize.call(this, _.extend({
                vent: vent,
                strings: {
                    clickToHide: i18n['footer.click-to-hide'],
                    clickToShow: i18n['footer.click-to-show']
                },
                tabData: [
                    {
                        key: 'placeholder-tab-key',
                        tab: new FooterTab(),
                        view: new FooterPane()
                    }
                ]
            }, options));
		},

		render: function() {
			Footer.prototype.render.apply(this, arguments);
		}
	});

});
