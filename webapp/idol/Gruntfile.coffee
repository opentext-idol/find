#
# Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
# Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
#

module.exports = (grunt) ->
  jasmineRequireTemplate = require 'grunt-template-jasmine-requirejs'

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
          keepRunner: false
          outfile: jasmineSpecRunner
          specs: specs
          template: jasmineRequireTemplate
          templateOptions:
            requireConfigFile: testRequireConfig
          junit:
            path: "target/jasmine-tests"
            consolidate: true
      'browser-test':
        src: sourcePath
        options:
          keepRunner: false
          outfile: jasmineSpecRunner
          specs: browserSpecs
          template: jasmineRequireTemplate
          templateOptions:
            requireConfigFile: browserTestRequireConfig
    less:
      build:
        files:
          'target/classes/static/css/bootstrap.css': '../core/src/main/less/bootstrap.less',
          'target/classes/static/css/compiled.css': '../core/src/main/less/app.less',
          'target/classes/static/css/login.css': '../core/src/main/less/login.less',
          'target/classes/static/css/result-highlighting.css': '../core/src/main/less/result-highlighting.less'
        options:
          strictMath: true
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
          '../core/src/main/less/**/*.less'
          'src/main/public/static/**/*'
        ]
        spawn: false
        tasks: ['sync:devResources', 'less:build']
    sync:
      devResources:
        files: [
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

  grunt.loadNpmTasks 'grunt-babel'
  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-less'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-watch'
  grunt.loadNpmTasks 'grunt-sync'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['babel:transform', 'jasmine:test']
  grunt.registerTask 'browser-test', ['jasmine:browser-test:build', 'connect:server', 'watch:buildBrowserTest']
  grunt.registerTask 'watch-test', ['babel:transform', 'jasmine:test', 'watch:test']
  grunt.registerTask 'copy-resources', ['sync:devResources', 'less:build', 'watch:copyResources']
