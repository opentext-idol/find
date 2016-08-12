module.exports = (grunt) ->
  jasmineRequireTemplate = require 'grunt-template-jasmine-requirejs'

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
        plugins: ['transform-es2015-block-scoping']
      transform:
        files: [ {
          expand: true
          cwd: 'src/main/public/static/js'
          src: ['**/*.js']
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
        files: watchFiles
        tasks: ['jasmine:test:build']
      test:
        files: watchFiles
        tasks: ['babel:transform', 'jasmine:test']

  grunt.loadNpmTasks 'grunt-babel'
  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-watch'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['babel:transform', 'jasmine:test']
  grunt.registerTask 'browser-test', ['jasmine:browser-test:build', 'connect:server', 'watch:buildTest']
  grunt.registerTask 'watch-test', ['babel:transform', 'jasmine:test', 'watch:test']
