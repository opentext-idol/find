#
# Copyright 2017-2018 Micro Focus International plc.
# Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
#

module.exports = (grunt) ->
  jasmineRequireTemplate = require 'grunt-template-jasmine-requirejs'
  jasmineReportPath = 'target/jasmine-reports'

  jasmineSpecRunner = 'spec-runner.html'

  sourcePath = 'src/main/public/static/js/find/**/*.js'

  browserTestRequireConfig = [
    'src/main/public/static/js/require-config.js'
    'src/test/js/test-require-config.js'
  ]

  testRequireConfig = browserTestRequireConfig.concat([
    'src/test/js/es5-test-require-config.js'
  ])

  specs = 'target/es5-jasmine-test-specs/spec/**/*.js'
  browserSpecs = 'src/test/js/spec/**/*.js'
  serverPort = 8000

  watchFiles = [
    'src/main/public/**/*.js'
    'src/test/**/*.js'
  ]

  grunt.initConfig
    pkg: grunt.file.readJSON 'package.json'
    babel:
      options:
        plugins: ['@babel/plugin-transform-block-scoping']
      transform:
        files: [{
          expand: true
          cwd: 'src/main/public/static/js'
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
        files: watchFiles
        tasks: ['jasmine:browser-test:build']
      test:
        files: watchFiles
        tasks: ['babel:transform', 'jasmine:test']
    peg:
      fieldtext:
        src: 'src/main/public/static/bower_components/hp-autonomy-fieldtext-js/src/js/field-text.pegjs'
        dest: 'target/classes/static/js/pegjs/fieldtext/parser.js'
        options:
          format: 'amd'
          trackLineAndColumn: true
      wellknowntext:
        src: 'src/main/public/static/js/find/app/util/geoindex/idol-wkt.pegjs'
        dest: 'target/classes/static/js/pegjs/idol-wkt/parser.js'
        options:
          format: 'amd'
          trackLineAndColumn: true

  grunt.loadNpmTasks 'grunt-babel'
  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-watch'
  grunt.loadNpmTasks 'grunt-peg'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['babel:transform', 'jasmine:test']
  grunt.registerTask 'browser-test', ['jasmine:browser-test:build', 'connect:server', 'watch:buildBrowserTest']
  grunt.registerTask 'watch-test', ['babel:transform', 'jasmine:test', 'watch:test']
