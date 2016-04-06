module.exports = (grunt) ->
  jasmineRequireTemplate = require 'grunt-template-jasmine-requirejs'

  jasmineSpecRunner = 'spec-runner.html'

  sourcePath = 'target/classes/static/js/find/**/*.js'

  testRequireConfig = [
    'target/classes/static/js/require-config.js'
    'src/test/js/test-require-config.js'
  ]

  specs = 'src/test/js/spec/**/*.js'
  serverPort = 8000

  testWatchFiles = [
    'target/classes/static/**/*.js'
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
        files: testWatchFiles
        tasks: ['jasmine:test:build']
      test:
        files: testWatchFiles
        tasks: ['test']
      copyResources:
        files: [
          '../find-core/src/main/public/static/**/*'
          'src/main/public/static/**/*'
        ]
        spawn: false
        tasks: ['sync:devResources']
    sync:
      devResources:
        files: [
          {
            cwd: '../find-core/src/main/public/static'
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

  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-watch'
  grunt.loadNpmTasks 'grunt-sync'

  grunt.registerTask 'default', ['test']
  grunt.registerTask 'test', ['jasmine:test']
  grunt.registerTask 'browser-test', ['jasmine:test:build', 'connect:server', 'watch:buildTest']
  grunt.registerTask 'watch-test', ['jasmine:test', 'watch:test']
  grunt.registerTask 'copy-resources', ['sync:devResources', 'watch:copyResources']
