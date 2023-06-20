package outputProviders;

import managers.FileReaderManager;
import organizers.FileHandler;
import outputProviders.logGenerator.LogCSVFileHandler;
import outputProviders.logGenerator.LogHTMLFileHandler;
import outputProviders.logInputter.IterationResultFormatter;
import outputProviders.logInputter.IterationResultsFormatter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static organizers.FileHandler.*;
import static outputProviders.logInputter.IterationResultsFormatter.*;
import static outputProviders.logInputter.LogFileCalculator.getFullVariableName;

/**
 * Handles the writing of log files for the application.
 * Provides methods for writing iteration results, summaries, log overviews, log histories,
 * error histories, and closing the log file.
 */
public class LogFileHandler {

    /**
     * All the results that we need to  write the log file about.
     */
    private List<IterationResult> results;
    /**
     * Formatter to use for all the iteration results together.
     */
    private IterationResultsFormatter forms;
    /**
     * Map that has a list of iterations results for each error code, sorted by occurrence.
     */
    private Map<Integer, List<IterationResult>> iterationResultsByErrorCode;
    /**
     * Map that has a list of iterations results for each output message, sorted by occurrence.
     */
    private Map<String, List<IterationResult>> iterationResultsByOutputMessage;
    /**
     * Elapsed time of the simulation.
     */
    private int elapsedTime;

    /**
     * Generates all logs in the actual_logs subdirectory, that are only about this stimulation.
     * This subdirectory contains three files:
     * <ul>
     *     <li>log.txt - A text file that contains all the iteration results.</li>
     *     <li>log.csv - A CSV file that contains all the iteration results.</li>
     *     <li>log_overview.csv - A CSV file that contains all possible combinations of unique error codes and output messages,
     *     and the number of times, as well as which iteration numbers, they occur.</li>
     * </ul>
     *
     * @param results
     *         All the results that we need to write the log file about.
     * @param iterationResultsByErrorCode
     *         Map that has a list of iterations results for each error code, sorted by occurrence.
     * @param iterationResultsByOutputMessage
     *         Map that has a list of iterations results for each output message, sorted by occurrence.
     * @param elaspedTime
     *         Elapsed time of the simulation.
     */
    public void generateActualLogs(List<IterationResult> results, Map<Integer, List<IterationResult>> iterationResultsByErrorCode, Map<String, List<IterationResult>> iterationResultsByOutputMessage, long elaspedTime) {
        this.results = results;
        this.iterationResultsByErrorCode = iterationResultsByErrorCode;
        this.iterationResultsByOutputMessage = iterationResultsByOutputMessage;
        this.elapsedTime = (int) elaspedTime;
        this.forms = new IterationResultsFormatter(results);
        generateLogTXTFile(); // Write the text logfile.
        generateLogCSVFile(); // Write the CSV logfile.
        generateLogOverview();  // Write the CSV overview logfile.
    }

    /**
     * Generates all logs in the log_history subdirectory. These files possibly already contain results from other
     * stimulation's that are run previously. This subdirectory contains multiple files:
     * <ul>
     *     <li>log_fullHistory.csv - A CSV file that contains all the iteration results of all the previous stimulation's.</li>
     *     <li>log_history.csv - A CSV file that contains general information about each stimulation (such as exit code
     *     counts, timestamps, ...).</li>
     *     <li>log_errorHistory.csv - A CSV file that contains all the error codes and the number of times they
     *     occur in that stimulation, for each stimulation.</li>
     *     <li>log_fullHistory_html directory: A directory that contains multiple HTML pages reporting about the
     *     iteration results of all the stimulation's. The implementation of generating these pages is deferred to
     *     the LogHTMLFileHandler class.</li>
     * </ul>
     *
     * @param iterationResults
     *         All the results that we want to append to the historical log files.
     * @param iterationResultsByErrorCode
     *         Map that has a list of iterations results for each error code, sorted by occurrence.
     * @param iterationResultsByOutputMessage
     *         Map that has a list of iterations results for each output message, sorted by occurrence.
     * @param elapsedTime
     *         Elapsed time of the simulation.
     */
    public void generateOverviewLogs(List<IterationResult> iterationResults, Map<Integer, List<IterationResult>> iterationResultsByErrorCode,
                                     Map<String, List<IterationResult>> iterationResultsByOutputMessage, long elapsedTime) {
        this.results = iterationResults;
        this.iterationResultsByErrorCode = iterationResultsByErrorCode;
        this.iterationResultsByOutputMessage = iterationResultsByOutputMessage;
        this.elapsedTime = (int) elapsedTime;
        this.forms = new IterationResultsFormatter(results);
        generateLogHistory();
        generateLogErrorHistory();
        generateFullLogHistory();
        FileReaderManager.getInstance().getConfigReader().writeConfigFile(); // Write the configurations js file of the website
        generateFullLogHistoryHTMLReport();
    }

    /**
     * If possible, opens the log file for writing.
     *
     * @param filePath
     *         The path of the log file.
     *
     */
    public void openWriterCheck(String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open log overview csv file: " + filePath);
        }
    }

    /**
     * Writes the iteration results to the log file.
     * The following things are provided in log.txt for each iteration:
     * <p>
     * Iteration number - Map file Type (Extension of the file path) - Absolute full path of the map file -
     * String action sequence - Error code of the process (0, 10, 1 or -1 (= others) ). -
     * Output messages of the process
     * </p>
     */
    private void generateLogTXTFile() {
        //     * Opens the log text file for writing.
        //     * If the log file cannot be opened, a RuntimeException is thrown.
        // Writes the iteration results
        openWriterCheck(logFilePath);
        try {
            FileWriter writer = new FileWriter(logFilePath);
            for (IterationResult iterationResult : results) {
                IterationResultFormatter form = new IterationResultFormatter(iterationResult);
                writer.write(form.getFormattedIterationNumber(true, false, true));
                writer.write(form.getFormattedErrorCode(true, true, false, false, true));
                writer.write(form.getFormattedOutputMessages(true, true, true, false, true));
                writer.write(form.getFormattedStringSequence(false, false, false, true, false, false, true));
                writer.write(form.getFormattedMapFileName(true, false, true, false, false, true));
                writer.write(form.getFormattedMapFileType(true, false, true, true, true, true, true, false, true));
                writer.write(form.getFormattedMapFileCustomAttribute(true, true, true, true, false, false, true));
                writer.write(form.getFormattedMapFilePath(false, false, true, false, false, true));
                writer.append("\n");
            }

            // Write summary
            writer.write("SUMMARY");
            writer.append("\n");
            writer.write(getFormattedFuzzAttemptNr(true, false, false, true));
            writer.write(getFormattedTimeStamp(true, false, false, true));
            writer.write(getFormattedExecutionTime(elapsedTime, true, false, false, true));
            writer.write(forms.getFormattedTotalIterations(true, false, false, true));
            writer.write(forms.getFormattedExitCount(0, true, true, false, false, true));
            writer.write(forms.getFormattedExitCount(1, true, true, false, false, true));
            writer.write(forms.getFormattedExitCount(10, true, true, false, false, true));
            writer.write(forms.getFormattedExitCount(-1, true, true, false, false, true));

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the log CSV file.
     * For each iteration of this run, one row.
     *
     * <p>
     *     Same information as log text file.
     * </p>
     *
     */
    public void generateLogCSVFile() {
        openWriterCheck(logFileCSVPath);
        try {
            FileWriter csvWriter = new FileWriter(logFileCSVPath);

            /* Write header */
            List<String> header = new ArrayList<>();
            header.add(getFullVariableName("iterationNumber", false));
            header.add(getFullVariableName("errorCode", false));
            header.add(getFullVariableName("outputMessages", false));
            header.add(getFullVariableName("stringSequence", false));
            header.add(getFullVariableName("mapFileType", false));
            header.add(getFullVariableName("customAttribute", false));
            header.add(getFullVariableName("mapFilePath", false));
            csvWriter.append(String.join(",", header)).append("\n");
            for (IterationResult iterationResult : results) {
                IterationResultFormatter form = new IterationResultFormatter(iterationResult);
                csvWriter.append(form.getFormattedIterationNumber()); // Iteration number
                csvWriter.append(form.getFormattedErrorCode()); // Error code
                csvWriter.append(form.getFormattedOutputMessages()); // Output messages
                csvWriter.append(form.getFormattedStringSequence()); // String action sequence
                csvWriter.append(form.getFormattedMapFileType()); // Map file type
                csvWriter.append(form.getFormattedMapFileCustomAttribute()); // Map file custom attribute
                csvWriter.append(form.getFormattedMapFilePath(false, false, false, true, false, true)); // Map file path
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a log overview in CSV format based on the iteration results.
     * Each unique combination of error codes and message outputs is one row in the csv.
     * The iteration numbers where this combination occurred are given, and counted.
     * Writes the log overview to a CSV file specified in the FileHandler.
     */
    public void generateLogOverview() {
        openWriterCheck(csvFilePath);
        try {
            FileWriter csvWriter = new FileWriter(csvFilePath);
            /* Write header */
            List<String> header = new ArrayList<>();
            header.add(getFullVariableName("errorCode", false));
            header.add(getFullVariableName("outputMessages", false));
            header.add(getFullVariableName("exitCount", false));
            header.add(getFullVariableName("exitIterations", false));
            csvWriter.append(String.join(",", header)).append("\n");

            /* Sort error codes in descending order of occurrence */
            // sortedErrorCodes is a list here that contains each occurred error code, sorted by occurrence
            // errorCodeResults is a list of results with the current error code
            List<Integer> sortedErrorCodes = new ArrayList<>(iterationResultsByErrorCode.keySet());
            sortedErrorCodes.sort((errorCode1, errorCode2) ->
                    iterationResultsByErrorCode.get(errorCode2).size() - iterationResultsByErrorCode.get(errorCode1).size());
            for (int errorCode : sortedErrorCodes) {
                List<IterationResult> errorCodeResults = iterationResultsByErrorCode.get(errorCode);

                /* Sort output messages in descending order of occurrence for the current error code */
                // outputMessageResults is a list of results with the output message
                // sortedOutputMessages is a list of output messages, sorted by occurrence
                // in loop -> first sort on error, than on output message.
                List<String> sortedOutputMessages = new ArrayList<>(iterationResultsByOutputMessage.keySet());
                sortedOutputMessages.sort((msg1, msg2) ->
                        iterationResultsByOutputMessage.get(msg2).size() - iterationResultsByOutputMessage.get(msg1).size());
                for (String outputMessage : sortedOutputMessages) {
                    List<IterationResult> outputMessageResults = iterationResultsByOutputMessage.get(outputMessage);
                    ArrayList<IterationResult> filteredResults = new ArrayList<>();

                    /* Get the iteration results of the current error code and output message.*/
                    // filteredResults will contain only the results with the current error code and output message
                    for (IterationResult result : outputMessageResults) {
                        if (result.getErrorCode() == errorCode) {
                            filteredResults.add(result);
                        }
                    }

                    /* If combination has actually occurred, write row for the current error code and output message */
                    if (!(filteredResults.size() == 0)) { // Combination actually occurred
                        // Write row for the current error code and output message
                        // Static methods made for filtered results that only have one error code or one output-message
                        csvWriter.append(IterationResultFormatter.getFormattedErrorCode(filteredResults));
                        csvWriter.append(IterationResultFormatter.getFormattedOutputMessages(filteredResults));
                        csvWriter.append(IterationResultsFormatter.getFormattedExitCount(filteredResults));
                        csvWriter.append(IterationResultsFormatter
                                .getFormattedIterationNumbersString(filteredResults, false, true, false, true));
                    }

                }
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Generates a log history in CSV format based on the iteration results.
     * Appends the log history to a CSV file specified in the FileHandler.
     * <p>
     * If file did not yet exist, create one and write header.
     * Otherwise, append row.
     * <p>
     * Each run gets one row with the following information:
     * FuzzAttemptNr - Time and date - Elapsed time - Counts of each of the iterations with the errorcodes.
     */
    public void generateLogHistory() {
        try {
            FileWriter writer = new FileWriter(logHistoryFilePath, true); // Here is appended!
            BufferedWriter csvWriter = new BufferedWriter(writer);

            // Write header
            if (fuzzAttemptNr == 1) {
                List<String> header = new ArrayList<>();
                header.add(getFullVariableName("fuzzAttemptNr", false));
                header.add(getFullVariableName("timeStamp", false));
                header.add(getFullVariableName("executedTime", false));
                header.add(getFullVariableName("exitCode0", false));
                header.add(getFullVariableName("exitCode1", false));
                header.add(getFullVariableName("exitCode10", false));
                header.add(getFullVariableName("exitCodeOther", false));
                csvWriter.append(String.join(",", header)).append("\n");
            }

            for (Map.Entry<Integer, List<IterationResult>> entry : iterationResultsByErrorCode.entrySet()) {
                List<IterationResult> results = entry.getValue(); // Get only the results for this error code
                IterationResultsFormatter forms = new IterationResultsFormatter(results); // Make new one -> not total iterations

                csvWriter.append(getFormattedFuzzAttemptNr()); // FuzzAttemptNr
                csvWriter.append(getFormattedTimeStamp()); // TimeStamp
                csvWriter.append(getFormattedExecutionTime((elapsedTime))); // Elapsed time
                csvWriter.append(forms.getFormattedExitCount(0));
                csvWriter.append(forms.getFormattedExitCount(1));
                csvWriter.append(forms.getFormattedExitCount(10));
                csvWriter.append(forms.getFormattedExitCount(-1, false, false, true, false, true)); // Exit codes, true, false, true, false, true, false, true); // Other exit codes
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a log error history in CSV format based on the iteration results.
     * Appends the log error history to a CSV file specified in the FileHandler.
     * <p>
     * If file did not yet exist, create one and write header.
     * Otherwise, append rows.
     * <p>
     * Each unique combination of run (and timestamp) - errorcode -  output message is one row.
     * Counts instances.
     *
     */
    public void generateLogErrorHistory() {
        try {
            FileWriter writer = new FileWriter(FileHandler.logErrorHistoryFilePath, true);
            BufferedWriter csvWriter = new BufferedWriter(writer);
            // Write header
            if (fuzzAttemptNr == 1) {
                List<String> header = new ArrayList<>();
                header.add(getFullVariableName("fuzzAttemptNr", false));
                header.add(getFullVariableName("timeStamp", false));
                header.add(getFullVariableName("errorCode", false));
                header.add(getFullVariableName("outputMessages", false));
                header.add(getFullVariableName("Count", false));
                csvWriter.append(String.join(",", header)).append("\n");
            }
            // sortedOutputMessages contains unique output messages alphabetically
            List<String> sortedOutputMessages = new ArrayList<>(iterationResultsByOutputMessage.keySet());
            sortedOutputMessages.sort(Comparator.naturalOrder()); // Alphabetically

            // Get a filtered table per output message.
            for (String outputMessage : sortedOutputMessages) {
                List<IterationResult> results = iterationResultsByOutputMessage.get(outputMessage);
                Map<Integer, Integer> errorCodeCounts = new HashMap<>();

                // errorCodeCounts is a map of error codes and their counts.
                // for each error code the values of the counts in this filtered table.
                for (IterationResult result : results) {
                    int errorCode = result.getErrorCode();
                    errorCodeCounts.put(errorCode, errorCodeCounts.getOrDefault(errorCode, 0) + 1);
                }

                // sortedErrorCodes is a list with unique error codes for this message, sorted in ascending order.
                List<Integer> sortedErrorCodes = new ArrayList<>(errorCodeCounts.keySet());
                sortedErrorCodes.sort(Comparator.naturalOrder());
                int index = 0;
                for (int errorCode : sortedErrorCodes) {
                    // Leave this like it was, too complicated to change it with Formatters.
                    // Same result.
                    csvWriter.append(getFormattedFuzzAttemptNr()); // Same for each combo
                    csvWriter.append(getFormattedTimeStamp()); // Same for each combo

                    IterationResult dummy = new IterationResult(0, "", "", errorCode, outputMessage, "");
                    IterationResultFormatter dummyForm = new IterationResultFormatter(dummy);
                    csvWriter.append(dummyForm.getFormattedErrorCode());

                    int count = sortedErrorCodes.get(index); // with this message and this error code
                    csvWriter.append(dummyForm.getFormattedOutputMessages());
                    csvWriter.append(getFormattedExitCount(errorCode, count, false, false, true, false, true));

                    index++;
                }
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a full log history CSV file based on the provided iteration results.
     * If the log history file is empty, it appends the header to the file.
     * For subsequent runs, it modifies the "Map File Path" column for previous attempts.
     *
     */
    public void generateFullLogHistory() {
        try {
            if (fuzzAttemptNr > 1) {
                // For previous attempts, change the column Map file path type value
                // from C:/ST/JPacmanFuzz/fuzzresults/actual_logs/${directory_of_exitcode}/${map_file_name}
                // to C:/ST/JPacmanFuzz/fuzzresults/previous_logs/run_${runAttempt}/${directory_of_exitcode}/${map_file_name}
                // Modify the column "Map File Path" for previous attempts
                LogCSVFileHandler.modifyMapFilePathColumn();
            }
            FileWriter csvWriter = new FileWriter(logFullHistoryFilePath, true);
            if (fuzzAttemptNr == 1) {
                /* Write header */
                List<String> header = new ArrayList<>();
                header.add(getFullVariableName("fuzzAttemptNr", false));
                header.add(getFullVariableName("timeStamp", false));
                header.add(getFullVariableName("iterationNumber", false));
                header.add(getFullVariableName("errorCode", false));
                header.add(getFullVariableName("outputMessages", false));
                header.add(getFullVariableName("stringSequence", false));
                header.add(getFullVariableName("mapFileType", false));
                header.add(getFullVariableName("mapFileName", false));
                header.add(getFullVariableName("customAttribute", false));
                header.add(getFullVariableName("mapFilePath", false));
                header.add(getFullVariableName("mapFileRelativePath", false));
                csvWriter.append(String.join(",", header)).append("\n");
            }
            for (IterationResult iterationResult : results) {
                IterationResultFormatter format = new IterationResultFormatter(iterationResult);
                csvWriter.append(getFormattedFuzzAttemptNr());
                csvWriter.append(getFormattedTimeStamp());
                csvWriter.append(format.getFormattedIterationNumber());
                csvWriter.append(format.getFormattedErrorCode());
                csvWriter.append(format.getFormattedOutputMessages());
                csvWriter.append(format.getFormattedStringSequence());
                csvWriter.append(format.getFormattedMapFileName());
                csvWriter.append(format.getFormattedMapFileType());
                csvWriter.append(format.getFormattedMapFileCustomAttribute());
                csvWriter.append(format.getFormattedMapFilePath());
                csvWriter.append(format.getFormattedMapFilePath(false, true, false,
                        true, false, true)); // Relative file path in project
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Generates a full log history HTML report based on the provided iteration results.
     */
    public void generateFullLogHistoryHTMLReport()  {
        LogHTMLFileHandler generator = new LogHTMLFileHandler();
        generator.generateHTMLReport();
    }
}

