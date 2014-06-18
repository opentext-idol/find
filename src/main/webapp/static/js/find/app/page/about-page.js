define([
	'about-page/js/about-page',
    'i18n!find/nls/bundle',
    'find/app/page/about/configuration',
    'find/app/page/about/lib-list'
], function(AboutPage, i18n, config, libList) {

	return AboutPage.extend({

        getTemplateParameters: function() {
            return {
                about: config.about,
                libraries: libList,
                strings: {
                    build: i18n['about.app.build'],
                    copyright: i18n['about.copyright'],
                    foss: i18n['about.foss'],
                    fossVersion: i18n['about.lib.version'],
                    library: i18n['about.lib.name'],
                    license: i18n['about.lib.licence'],
                    tagLine: i18n['about.tagline'],
                    title: i18n['app.about'],
                    version: i18n['about.app.version']
                }
            }
        }
	});
});