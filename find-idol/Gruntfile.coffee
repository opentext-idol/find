module.exports = (grunt) ->
  grunt.initConfig
    pkg: grunt.file.readJSON 'package.json'
    watch:
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

  grunt.loadNpmTasks 'grunt-contrib-watch'
  grunt.loadNpmTasks 'grunt-sync'

  grunt.registerTask 'copy-resources', ['watch:copyResources']
