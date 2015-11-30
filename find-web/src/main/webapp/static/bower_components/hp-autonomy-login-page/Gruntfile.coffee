module.exports = (grunt) ->
  jasmineRequireTemplate = require 'grunt-template-jasmine-requirejs'
  jasmineIstanbulTemplate = require 'grunt-template-jasmine-istanbul'

  jasmineSpecRunner = 'spec-runner.html'
  coverageSpecRunner = 'coverage-runner.html'

  sourcePath = 'src/js/**/*.js'

  documentation = 'doc'
  testRequireConfig = 'test/js/js-test-require-config.js'
  specs = 'test/js/spec/**/*.js'
  styles = 'bower_components/hp-autonomy-js-testing-utils/src/css/bootstrap-stub.css'
  serverPort = 8000
  host = "http://localhost:#{serverPort}/"
  helpers = 'bower_components/hp-autonomy-js-testing-utils/src/js/jasmine-custom-matcher.js'

  grunt.initConfig
    pkg: grunt.file.readJSON 'package.json'
    clean: [
      jasmineSpecRunner
      coverageSpecRunner
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
          styles: styles
          template: jasmineRequireTemplate
          templateOptions:
            requireConfigFile: testRequireConfig
      coverage:
        src: sourcePath
        options:
          helpers: helpers
          host: host
          keepRunner: true
          outfile: coverageSpecRunner
          specs: specs
          styles: styles
          template: jasmineIstanbulTemplate
          templateOptions:
            coverage: 'bin/coverage/coverage.json'
            replace: false
            report:
              type: 'text'
            template: jasmineRequireTemplate
            templateOptions:
              requireConfigFile: testRequireConfig
              requireConfig:
                config:
                  instrumented: {
                    src: grunt.file.expand(sourcePath)
                  }
                callback: () ->
                  define('instrumented', ['module'], (module) ->
                    module.config().src
                  )
                  require ['instrumented'], (instrumented) ->
                    oldLoad = requirejs.load
                    requirejs.load = (context, moduleName, url) ->
                      if url.substring(0, 1) == '/'
                        url = url.substring 1
                      else if url.substring(0, 2) == './'
                        url = url.substring 2

                      # redirect
                      if instrumented.indexOf(url) > -1
                        url = './.grunt/grunt-contrib-jasmine/' + url;

                      return oldLoad.apply(this, [context, moduleName, url]);
                    return
                  return

    jshint:
      all: [
        sourcePath
        specs
      ],
      options:
        asi: true
        bitwise: true
        browser: true
        camelcase: true
        curly: true
        devel: true
        eqeqeq: true
        es3: true
        expr: true
        forin: true
        freeze: true
        jquery: true
        latedef: true
        newcap: true
        noarg: true
        noempty: true
        nonbsp: true
        undef: true
        unused: true
        globals:
          define: false
          _: false
          expect: false
          it: false
          require: false
          describe: false
          sinon: false
          beforeEach: false
          afterEach: false
          jasmine: false
          runs: false
          waits: false
          waitsFor: false
          spyOn: false
          xit: false
          xdescribe: false
    coffeelint:
      app: [
        'Gruntfile.coffee'
      ]
      options:
        max_line_length:
          level: 'ignore'
    jsdoc:
      dist:
        src: ['src/**/*.js', 'README.md']
        options:
          destination: documentation
          template: 'node_modules/ink-docstrap/template'
          configure: 'jsdoc.conf.json'
    'gh-pages':
      'default':
        src: '**/*'
        options:
          base: 'doc'
          message: 'Update documentation'
      travis:
        src: '**/*'
        options:
          base: 'doc'
          message: 'Update documentation'
          repo: 'https://' + process.env.GH_TOKEN + '@github.com/' + process.env.TRAVIS_REPO_SLUG
          user:
            name: 'Travis CI Server'
            email: 'alex.scown@hp.com'

  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-connect'
  grunt.loadNpmTasks 'grunt-contrib-jasmine'
  grunt.loadNpmTasks 'grunt-contrib-jshint'
  grunt.loadNpmTasks 'grunt-coffeelint'
  grunt.loadNpmTasks 'grunt-gh-pages'
  grunt.loadNpmTasks 'grunt-jsdoc'

  grunt.registerTask 'default', ['lint', 'connect:server', 'jasmine:test', 'jasmine:coverage']
  grunt.registerTask 'test', ['connect:server', 'jasmine:test']
  grunt.registerTask 'browser-test', ['connect:server:keepalive']
  grunt.registerTask 'coverage', ['connect:server', 'jasmine:coverage']
  grunt.registerTask 'lint', ['jshint', 'coffeelint']
  grunt.registerTask 'doc', ['jsdoc']
  grunt.registerTask 'push-doc', ['doc', 'gh-pages:default']
  grunt.registerTask 'push-doc-travis', ['doc', 'gh-pages:travis']
