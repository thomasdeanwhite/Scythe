B# Scythe
An instrumentation tool for Light-weight Code Coverage tracking.

# Building
We use Shade to build:
- *git clone git@github.com:thomasdeanwhite/Scythe.git*
- *cd Scythe*
- *mvn clean install package*
- Scythe-*-shaded.jar in target folder

# Running
Scythe runs as a Java Agent:
- Scythe can run using java -javaagent:Scythe-jar-name.jar="[opts]" myprogram.jar
- Options can be seen below or by directly running the Scythe jar.

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

Scythe attaches to processed as a Java Agent. Please use javaagent:Sytche.jar to run as a java Agent