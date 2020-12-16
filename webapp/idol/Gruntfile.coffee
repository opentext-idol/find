#
# Copyright 2017-2018 Micro Focus International plc.
# Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
  serverPort = 8000

  testWatchFiles = [
    'target/classes/static/**/*.js'
    'src/test/**/*.js'
  ]

  jsSourceMap = grunt.option('jsSourceMap') || false

  grunt.initConfig
    pkg: grunt.file.readJSON 'package.json'
    babel:
      options:
        plugins: ['@babel/plugin-transform-block-scoping']
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
      wellknowntext:
        files: [
          '../core/src/main/public/static/js/find/app/util/geoindex/idol-wkt.pegjs'
        ],
        tasks: ['peg:wellknowntext']
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
      wellknowntext:
        src: '../core/src/main/public/static/js/find/app/util/geoindex/idol-wkt.pegjs'
        dest: 'target/classes/static/js/pegjs/idol-wkt/parser.js'
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
        generateSourceMaps: jsSourceMap
      public:
        options:
          name: 'public',
          include: [
            'require-config'
            'find/idol/app/idol-app'
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
    terser:
      js:
        files: [{
          expand: true
          cwd: 'target/classes/static/js'
          src: ['public.js', 'config.js', 'login.js']
          dest: 'target/classes/static/js'
        }]
      languages:
        files: [{
          expand: true
          cwd: 'target/classes/static/js'
          src: ['find/**/nls/**/*.js']
          dest: 'target/classes/static/js'
        }]

  grunt.loadNpmTasks 'grunt-babel'
  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-requirejs'
  grunt.loadNpmTasks 'grunt-terser'
  grunt.loadNpmTasks 'grunt-contrib-watch'
  grunt.loadNpmTasks 'grunt-sync'
  grunt.loadNpmTasks 'grunt-peg'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['babel:transform', 'jasmine:test']
  grunt.registerTask 'browser-test', ['jasmine:browser-test:build', 'connect:server', 'watch:buildBrowserTest']
  grunt.registerTask 'watch-test', ['babel:transform', 'jasmine:test', 'watch:test']
  grunt.registerTask 'copy-resources', ['sync:devResources', 'watch:copyResources']
  grunt.registerTask 'concatenate', ['requirejs']
  grunt.registerTask 'minify', ['terser:js', 'terser:languages']
  grunt.registerTask 'compile', ['concatenate', 'minify']
