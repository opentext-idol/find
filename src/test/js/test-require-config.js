define(['/src/js/require-config.js'], function() {
    require.config({
        baseUrl: '/src/static/js',
        paths: {
            /*  Directories  */
            mock: '/test/mock',
            real: '/src/static/js',
            resources: '/test/resources'

            /* Mocks */
            // replace this comment with your mocks
        }
    });
});