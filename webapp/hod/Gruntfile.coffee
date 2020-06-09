#
# (c) Copyright 2017 Micro Focus or one of its affiliates.
#
# Licensed under the MIT License (the "License"); you may not use this file
# except in compliance with the License.
#
# The only warranties for products and services of Micro Focus and its affiliates
# and licensors ("Micro Focus") are as may be set forth in the express warranty
# statements accompanying such products and services. Nothing herein should be
# construed as constituting an additional warranty. Micro Focus shall not be
# liable for technical or editorial errors or omissions contained herein. The
# information contained herein is subject to change without notice.
#

module.exports = (grunt) ->
  jasmineRequireTemplate = require 'grunt-template-jasmine-requirejs'
  jasmineReportPath = 'target/jasmine-reports'

  jasmineSpecRunner = 'spec-runner.html'

  sourcePath = 'target/classes/static/js/find/**/*.js'

  browserTestRequireConfig = [
    'target/classes/static/js/require-config.js'
    'src/test/js/test-require-config.js'
  ]

  testRequireConfig = browserTestRequireConfig.concat([
    'src/test/js/es5-test-require-config.js'
  ])

  specs = 'target/es5-jasmine-test-specs/spec/**/*.js'
  browserSpecs = 'src/test/js/spec/**/*.js'
  serverPort = 8001

  testWatchFiles = [
    'target/classes/static/**/*.js'
    'src/test/**/*.js'
  ]

  grunt.initConfig
    pkg: grunt.file.readJSON 'package.json'
    babel:
      options:
        plugins: ['transform-es2015-block-scoping']
      transform:
        files: [{
          expand: true
          cwd: 'target/classes/static/js'
          src: ['find/**/*.js']
          dest: 'target/es5-jasmine-test'
          ext: '.js'
        }, {
          expand: true
          cwd: 'src/test/js'
          src: ['**/*.js']
          dest: 'target/es5-jasmine-test-specs'
          ext: '.js'
        }]
    clean: [
      jasmineSpecRunner
      'bin'
      '.grunt'
    ]
    connect:
      server:
        options:
          port: serverPort
          useAvailablePort: true
    jasmine:
      test:
        src: sourcePath
        options:
          allowFileAccess: true
          outfile: jasmineSpecRunner
          specs: specs
          template: jasmineRequireTemplate
          templateOptions:
            requireConfigFile: testRequireConfig
          junit:
            path: jasmineReportPath
            # without this, some report filenames are too long; this doesn't cause an error, it just
            # hangs test execution without any logging
            consolidate: true
          # needed to work around some browser crashes
          noSandbox: true
      'browser-test':
        src: sourcePath
        options:
          outfile: jasmineSpecRunner
          specs: browserSpecs
          template: jasmineRequireTemplate
          templateOptions:
            requireConfigFile: browserTestRequireConfig
    watch:
      options:
        interval: 5000
      buildBrowserTest:
        files: testWatchFiles
        tasks: ['jasmine:browser-test:build']
      test:
        files: testWatchFiles
        tasks: ['test']
      copyResources:
        files: [
          '../core/src/main/public/static/**/*'
          '../core/src/main/resources/less/**/*.less'
          'src/main/public/static/**/*'
        ]
        spawn: false
        tasks: ['sync:devResources']
      fieldtext:
        files: [
          '../core/src/main/public/static/bower_components/hp-autonomy-fieldtext-js/src/js/field-text.pegjs'
        ],
        tasks: ['peg:fieldtext']
    sync:
      devResources:
        files: [
          {
            cwd: '../core/src/main/resources/less'
            src: '**/*'
            dest: '../core/target/classes/less'
          }
          {
            cwd: '../core/src/main/public/static'
            src: '**/*'
            dest: 'target/classes/static'
          }
          {
            cwd: 'src/main/public/static/'
            src: '**/*'
            dest: 'target/classes/static'
          }
        ]
        verbose: true
    peg:
      fieldtext:
        src: '../core/src/main/public/static/bower_components/hp-autonomy-fieldtext-js/src/js/field-text.pegjs'
        dest: 'target/classes/static/js/pegjs/fieldtext/parser.js'
        options:
          format: 'amd'
          trackLineAndColumn: true
    requirejs:
      options:
        appDir: 'target/webapp'
        baseUrl: 'static/js'
        dir: 'target/classes'
        keepBuildDir: true
        mainConfigFile: 'target/webapp/static/js/require-config.js'
        optimize: 'none'
      public:
        options:
          name: 'public',
          include: [
            'require-config'
            'find/hod/app/hod-app'
          ]
      config:
        options:
          name: 'config',
          include: [
            'require-config',
            'find/config/config-app'
          ]
      login:
        options:
          name: 'login',
          include: [
            'require-config',
            'login-page/js/login',
            'i18n!find/nls/bundle'
          ]
    uglify:
      options:
        compress: true
        mangle: true
      js:
        files: [{
          expand: true
          cwd: 'target/classes/static/js'
          src: '**/*.js'
          dest: 'target/classes/static/js'
        }]

  grunt.loadNpmTasks 'grunt-babel'
  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-requirejs'
  grunt.loadNpmTasks 'grunt-contrib-uglify-es'
  grunt.loadNpmTasks 'grunt-contrib-watch'
  grunt.loadNpmTasks 'grunt-sync'
  grunt.loadNpmTasks 'grunt-peg'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['babel:transform', 'jasmine:test']
  grunt.registerTask 'browser-test', ['jasmine:browser-test:build', 'connect:server', 'watch:buildBrowserTest']
  grunt.registerTask 'watch-test', ['babel:transform', 'jasmine:test', 'watch:test']
  grunt.registerTask 'copy-resources', ['sync:devResources', 'watch:copyResources']
  grunt.registerTask 'concatenate', ['requirejs']
  grunt.registerTask 'minify', ['uglify:js']
  grunt.registerTask 'compile', ['concatenate', 'minify']
