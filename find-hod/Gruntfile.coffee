module.exports = (grunt) ->
  jasmineRequireTemplate = require 'grunt-template-jasmine-requirejs'

  jasmineSpecRunner = 'spec-runner.html'

  sourcePath = 'target/classes/static/js/find/**/*.js'

  documentation = 'doc'

  testRequireConfig = [
    'target/classes/static/js/require-config.js'
    'src/test/js/test-require-config.js'
  ]

  specs = 'src/test/js/spec/**/*.js'
  serverPort = 8000
  host = "http://localhost:#{serverPort}/"

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
          host: host
          keepRunner: true
          outfile: jasmineSpecRunner
          specs: specs
          template: jasmineRequireTemplate
          templateOptions:
            requireConfigFile: testRequireConfig
    watch:
      test:
        files: [
          'src/**/*.js'
          'test/**/*.js'
        ]
        tasks: ['test']
      copyResources:
        files: [
          'src/main/public/static/**/*'
        ]
        spawn: false
        tasks: ['sync:devResources']
    sync:
      devResources:
        files: [
          {
            cwd: 'src/main/public/static/'
            src: '**/*'
            dest: 'target/classes/static'
          }
        ]
        verbose: true

  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-watch'
  grunt.loadNpmTasks 'grunt-sync'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['connect:server', 'jasmine:test']
  grunt.registerTask 'browser-test', ['connect:server:keepalive']
  grunt.registerTask 'watch-test', ['watch:test']
  grunt.registerTask 'copy-resources', ['watch:copyResources']
