module.exports = (grunt) ->

  sources = [
    'src/js/jasmine/jasmine-custom-matcher.js'
    'src/js/backbone-mock-factory.js'
  ]

  grunt.initConfig
    pkg: grunt.file.readJSON 'package.json'
    jshint:
      all: sources
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
          jasmine: false
    coffeelint:
      app: [
        'Gruntfile.coffee'
      ]

  grunt.loadNpmTasks 'grunt-contrib-jshint'
  grunt.loadNpmTasks 'grunt-coffeelint'

  grunt.registerTask 'default', ['lint']
  grunt.registerTask 'lint', ['jshint', 'coffeelint']
