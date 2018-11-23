[![Build Status](https://travis-ci.org/thomasdeanwhite/Scythe.svg?branch=dev)](https://travis-ci.org/thomasdeanwhite/Scythe)[![Coverage Status](https://coveralls.io/repos/github/thomasdeanwhite/Scythe/badge.svg?branch=dev)](https://coveralls.io/github/thomasdeanwhite/Scythe?branch=dev)


# Scythe
An instrumentation tool for Light-weight Code Coverage tracking.

# Building
We use Shade to build:
- *git clone git@github.com:thomasdeanwhite/Scythe.git*
- *cd Scythe*
- *mvn clean install package*
- Scythe-*-shaded.jar in target folder

# Running
## Java Agent
Scythe runs as a Java Agent:
- Scythe can run using java -javaagent:Scythe-jar-name.jar="[opts]" myprogram.jar
- Options can be seen below or by directly running the Scythe jar.
## Ant Script
Scythe is able to run as part of an Ant script in your own projects.
When creating a `build.xml` file, create an xml namespace for the Scythe object
```
<project name="Your Project Name" basedir="." xmlns:scythe="antlib:com.scythe.ant">
```
To load the Scythe tasks into your project, use the `taskdef` task
```
  <taskdef uri="antlib:com.scythe.ant"
    resource="com/scythe/ant/antlib.xml"
    classpath="path/to/scythe.jar"/>
```
For offline instrumentation, create a target with an appropriate name and include the `scythe:instrument` command. For example:
```
<!-- ensure all classes are compiled before instrumenting -->
<target name="instrument" depends="compile">
  <!-- offline instrumentation stores instrumented class files in a target directory -->
  <scythe:instrument destdir="instrumented/files/directory">
    <!-- an ant fileset can be used to specify all the files we wish to instrument -->
    <fileset dir="path/to/compiled/class/files">
      <include name="**/*.class"/>
    </fileset>
  </scythe:instrument>
</target>
```
Once code has been instrumented, we then run the tests and collect the coverage using a second call (Coming Soon)

# Runtime Options
| Key | Description |
| --- | --- |
| **Logging** |  |
| log_filename:[arg]  | _Select the file name for the log file. Files are divided into folders for coverage etc_ |
| log_dir:[arg]  | _directory in which to store log files (application.log, timings.log)_ |
| log_timings:[arg]  | _set whether application timings should be written to a log file_ |
| **Dev** |  |
| write_class:[arg]  | _flag to determine whether or not to write classes. If set to true, the InstrumentingClassLoader will write out all classes to the value of BYTECODE_DIR_ |
| bytecode_dir:[arg]  | _directory in which to store bytecode if the WRITE_CLASS property is set to true_ |
| **Instrumentation** |  |
| instrumentation_approach:[arg]  | _Determines the approach to be used during class instrumentation. A static approach inserts calls to ClassAnalyzer.lineFound etc to track which lines/branches have been covered. Using an array stores all line/branch executions in an array of integers and has a method to get all the values_ |
| instrument_lines:[arg]  | _Switch on line instrumentation_ |
| instrument_branches:[arg]  | _Switch on branch instrumentation_ |
| use_changed_flag:[arg]  | _It is possible to add a flag through instrumentation that will tell the ClassAnalyzer that a class has changed in some way. This creates a form of hybrid approach to instrumentation, but saves work at the time of collecting coverage data_ |
| **Testing** |  |
| track_active_testcase:[arg]  | _When collecting coverage information, it is possible to include information about which test case covered each line. If this argument is true, use ClassAnalyzer.setActiveTest(TestCase), and then each line/branch object will have a list of test cases that cover it, accessed by CoverableGoal.getCoveringTests_ |
