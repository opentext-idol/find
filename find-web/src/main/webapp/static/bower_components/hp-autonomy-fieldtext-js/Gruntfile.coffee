module.exports = (grunt) ->
  jasmineRequireTemplate = require 'grunt-template-jasmine-requirejs'

  jasmineSpecRunner = 'spec-runner.html'

  sourcePath = 'src/js/**/*.js'

  documentation = 'doc'
  testRequireConfig = 'test/js/js-test-require-config.js'
  specs = 'test/js/spec/**/*.js'
  serverPort = 8000
  host = "http://localhost:#{serverPort}/"
  helpers = 'bower_components/hp-autonomy-js-testing-utils/src/js/jasmine-custom-matcher.js'

  grunt.initConfig
    pkg: grunt.file.readJSON 'package.json'
    clean: [
      jasmineSpecRunner
      'bin'
      '.grunt'
      documentation
    ]
    connect:
      server:
        options:
          port: serverPort
    jasmine:
      test:
        src: sourcePath
        options:
          helpers: helpers
          host: host
          keepRunner: true
          outfile: jasmineSpecRunner
          specs: specs
          template: jasmineRequireTemplate
          templateOptions:
            requireConfigFile: testRequireConfig

  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['connect:server', 'jasmine:test']
  grunt.registerTask 'browser-test', ['connect:server:keepalive']