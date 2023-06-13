# *Fuzzer - Automated Testing Tool for JPacman*
The Fuzzer is an automated testing tool designed to perform fuzz testing on the JPacman application. 
It is a black-box fuzzer, meaning that the JPacman application code is not visible while fuzzing.
One can execute the jpacman code with the help of the jpacman-3.0.1 jar file. 
The fuzzer generates random maps and action sequences to test the functionality and stability of the JPacman game.

## *A general overview of the project*
### Features
- Generates random maps and action sequences for testing. 
- Executes the JPacman application with the generated inputs. 
- Captures and analyzes the output and exit codes of the JPacman process. 
- Logs the results of each iteration and generates summary reports. 
- Supports both text-based and binary map file formats. 
- Provides configurable options for maximum iterations, time budget, map dimensions, and more.

### Prerequisites
- Java Development Kit (JDK) 8 or above 
- JPacman application (version 3.0.1 or compatible) 
- Configuration file (see "Configuration" section for details)

### Installation
Clone the Fuzzer repository to your local machine.
Ensure that the JPacman application is available in the project directory or specify its location in the configuration file.
Update the configuration file with the desired settings (see "Configuration" section for details).

### Configuration
The configuration file (config.properties) allows you to customize various aspects of the fuzzing process. You can modify the following settings:

- fileType: Specifies the type of map files to be generated. Valid values: TEXT, BINARY, ALL. Default: ALL
- resultPath: Specifies the path to store the fuzzing results. Default: fuzzresults
- logFilePath: Specifies the path to store the log files. Default: logs
- logFileName: Specifies the name of the log file. Default: log
- logHistory: Specifies whether to generate log history files. Valid values: true, false. Default: true
- mapFilePath: Specifies the path to store the generated map files. Default: maps
- cleanDirectories: Specifies whether to clean directories before running the fuzzing process. Valid values: true, false. Default: true
- maxIterations: Specifies the maximum number of iterations for the fuzzing process. Default: 100
- maxTime: Specifies the maximum time budget for the fuzzing process in milliseconds. Default: 900000 (15 minutes)
- maxTextMapHeight: Specifies the maximum height of the generated text-based maps. Default: 20
- maxTextMapWidth: Specifies the maximum width of the generated text-based maps. Default: 20
- maxBinaryMapSize: Specifies the maximum size of the generated binary maps. Default: 100
- maxActionSequenceLength: Specifies the maximum length of the random action sequences. Default: 5

Please fill in the appropriate values for each setting according to your needs. The default values are provided as a reference, 
but feel free to adjust them as required.

### Usage
Ensure that the JPacman application is available in the project directory or specify its location in the configuration file.
Update the configuration file (config.properties) with the desired settings.
Run the Fuzzer class as the main entry point of the application.
The fuzzing process will generate random maps and action sequences, execute the JPacman application with the inputs, 
and capture the results.
After the fuzzing process completes, the logs and summary reports will be stored in the specified directories.
Review the logs and reports to analyze the results of the fuzzing process.

### Results
The Fuzzer run-configuration provides various types of output and results:

- Log files: Detailed logs of each iteration, including map file, action sequence, exit code, and output messages. A website is build. To alter the style of the webiste with Sass run the css-watch run configuration.
- Summary reports: Reports summarizing the results of the fuzzing process, including statistics, error codes, and output messages.
- Map files: Generated map files stored in the designated directory.
- Javadoc: Published on Netifly as well, on the following [link](https://jpacmanfuzzsite.netlify.app/). 

### Acknowledgements
The Fuzzer was developed by Luna Geens as a project for the Software Testing course of the University of Antwerp.
We would like to express our gratitude to the creators of JPacman for providing an excellent application to test and fuzz.

## *Lessons learned about the JPacman project*
The directory fuzzresults_lessons includes some interesting conclusions about valid input for the JPacman application and are explained in the Pseudocode.markdown file.




