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
        files: [ {
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
        } ]
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
      'browser-test':
        src: sourcePath
        options:
          keepRunner: false
          outfile: jasmineSpecRunner
          specs: browserSpecs
          template: jasmineRequireTemplate
          templateOptions:
            requireConfigFile: browserTestRequireConfig
    watch:
      buildTest:
        files: testWatchFiles
        tasks: ['jasmine:test:build']
      test:
        files: testWatchFiles
        tasks: ['test']
      copyResources:
        files: [
          '../core/src/main/public/static/**/*'
          'src/main/public/static/**/*'
        ]
        spawn: false
        tasks: ['sync:devResources']
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
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-watch'
  grunt.loadNpmTasks 'grunt-sync'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['babel:transform', 'jasmine:test']
  grunt.registerTask 'browser-test', ['jasmine:browser-test:build', 'connect:server', 'watch:buildTest']
  grunt.registerTask 'watch-test', ['babel:transform', 'jasmine:test', 'watch:test']
  grunt.registerTask 'copy-resources', ['sync:devResources', 'watch:copyResources']
