module.exports = (grunt) ->
  jasmineRequireTemplate = require 'grunt-template-jasmine-requirejs'

  jasmineSpecRunner = 'spec-runner.html'

  sourcePath = 'src/main/public/static/js/find/**/*.js'

  testRequireConfig = [
    'src/main/public/static/js/require-config.js'
    'src/test/js/test-require-config.js'
  ]

  specs = 'src/test/js/spec/**/*.js'
  serverPort = 8000

  watchFiles = [
    'src/main/public/**/*.js'
    'src/test/**/*.js'
  ]

  grunt.initConfig
    pkg: grunt.file.readJSON 'package.json'
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
    watch:
      buildTest:
        files: watchFiles
        tasks: ['jasmine:test:build']
      test:
        files: watchFiles
        tasks: ['jasmine:test']

  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-watch'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['jasmine:test']
  grunt.registerTask 'browser-test', ['jasmine:test:build', 'connect:server', 'watch:buildTest']
  grunt.registerTask 'watch-test', ['jasmine:test', 'watch:test']
