# Requirements
Designed for use with com.github.searls:jasmine-maven-plugin.

# Usage
Set these plugin configuration options in your pom.xml:
1. customRunnerTemplate should be the location of spec-runner.html.
2. customRunnerConfiguration should be the location of a json file containing an array of dependencies to require before loading the tests (eg:
   require config).
3. You must leave srcDirectoryName as the default ('src').
4. You must set specDirectoryName to 'test'.
Dependencies must be unpacked before running this plugin.

# Mocks and Resources
If you wish to store mocks and test resources in the test directory, set jsTestSrcDir to the test directory and use specIncludes and/or specExcludes to restrict
the specs to the spec directory.