package outputProviders;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static outputProviders.FileHandler.logHistoryFilePath;

/**
 * Handles the writing of log files for the application.
 * Provides methods for writing iteration results, summaries, log overviews, log histories,
 * error histories, and closing the log file.
 */
public class LogFileHandler {

    /**
     * The FileWriter object used to write to the log file.
     */
    private final FileWriter writer;

    /**
     * Constructs a LogFileHandler object and initializes the FileWriter.
     * Opens the log text file for writing.
     * If the log file cannot be opened, a RuntimeException is thrown.
     */
    public LogFileHandler() {
        String logFilePath = FileHandler.logFilePath;
        try {
            writer = new FileWriter(logFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open log file: " + logFilePath);
        }
    }

    /**
     * Writes the iteration results to the log file.
     * The following things are provided in log.txt for each iteration:
     * Iteration number - Map file Type (Extension of the file path) - Absolute full path of the mape file -
     * String action sequence - Error code of the process (0, 10, 1 or -1 (= others) ). -
     * Output messages of the process
     * Only writes the results with an error code other than 0.
     *
     * @param iterationResults The list of iteration results to write.
     */
    public void writeIterationResults(List<IterationResult> iterationResults) {
        try {
            for (IterationResult iterationResult : iterationResults) {
                if (iterationResult.getErrorCode() != 0) {
                    writer.write("Iteration: " + iterationResult.getIterationNumber() + "\n");
                    writer.write("Map File Type: " + iterationResult.getMapFileType() + "\n");
                    writer.write("Map File Path: " + iterationResult.getMapFilePath() + "\n");
                    writer.write("String Sequence: " + iterationResult.getStringSequence() + "\n");
                    writer.write("Error Code: " + iterationResult.getErrorCode() + "\n");
                    writer.write("Output Messages: " + iterationResult.getOutputMessages() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the summary of iteration results at the end of the log text file.
     * Counts the occurrences of each error code and writes the summary.
     *
     * @param iterationResults The list of iteration results to summarize.
     */
    public void writeSummary(List<IterationResult> iterationResults) {
        try {
            int exitCode0Count = 0;
            int exitCode10Count = 0;
            int exitCodeOtherCount = 0;

            for (IterationResult iterationResult : iterationResults) {
                int exitCode = iterationResult.getErrorCode();
                if (exitCode == 0) {
                    exitCode0Count++;
                } else if (exitCode == 10) {
                    exitCode10Count++;
                } else {
                    exitCodeOtherCount++;
                }
            }

            writer.write("SUMMARY\n");
            writer.write("Exit Code 0: " + exitCode0Count + " occurrences\n");
            writer.write("Exit Code 10: " + exitCode10Count + " occurrences\n");
            writer.write("Exit Code Other: " + exitCodeOtherCount + " occurrences\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the log text file.
     * Flushes and closes the FileWriter object.
     */
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a log overview in CSV format based on the iteration results.
     * Each unique combination of error codes and message outputs is one row in the csv.
     * The iteration numbers where this combination occured are given, and counted.
     * Writes the log overview to a CSV file specified in the FileHandler.
     *
     * @param iterationResultsByErrorCode     A map of iteration results grouped by error code.
     * @param iterationResultsByOutputMessage A map of iteration results grouped by output message.
     */
    public void generateLogOverview(Map<Integer, List<IterationResult>> iterationResultsByErrorCode, Map<String, List<IterationResult>> iterationResultsByOutputMessage) {
        try {

            String csvFilePath = FileHandler.csvFilePath;
            try {
                FileWriter csvWriter = new FileWriter(csvFilePath);
                csvWriter.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to open log overview csv file: " + csvFilePath);
            }
            FileWriter csvWriter = new FileWriter(csvFilePath);

            // Write header
            csvWriter.append("ErrorCode,OutputMessage,Count,IterationNumbers").append("\n");

            // Sort error codes in descending order of occurrence
            List<Integer> sortedErrorCodes = new ArrayList<>(iterationResultsByErrorCode.keySet());
            sortedErrorCodes.sort((errorCode1, errorCode2) ->
                    iterationResultsByErrorCode.get(errorCode2).size() - iterationResultsByErrorCode.get(errorCode1).size());

            for (int errorCode : sortedErrorCodes) {
                List<IterationResult> errorCodeResults = iterationResultsByErrorCode.get(errorCode);

                // Sort output messages in descending order of occurrence for the current error code
                List<String> sortedOutputMessages = new ArrayList<>(iterationResultsByOutputMessage.keySet());
                sortedOutputMessages.sort((msg1, msg2) ->
                        iterationResultsByOutputMessage.get(msg2).size() - iterationResultsByOutputMessage.get(msg1).size());

                for (String outputMessage : sortedOutputMessages) {
                    List<IterationResult> outputMessageResults = iterationResultsByOutputMessage.get(outputMessage);

                    // Filter results with the current error code and output message
                    List<IterationResult> filteredResults = new ArrayList<>();
                    for (IterationResult result : outputMessageResults) {
                        if (result.getErrorCode() == errorCode) {
                            filteredResults.add(result);
                        }
                    }

                    // Write row for the current error code and output message
                    csvWriter.append(Integer.toString(errorCode)).append(",");
                    csvWriter.append(outputMessage.replace("\n", "; ")).append(","); // Otherwise new line in CSV file
                    csvWriter.append(Integer.toString(filteredResults.size())).append(", ");
                    csvWriter.append(getIterationNumbersString(filteredResults)).append("\n"); // Find iteration maps that had this message and error code
                }
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a string representation of iteration numbers from a list of iteration results.
     * e.g., [1, 2, 3] becomes "1-2-3"
     *
     * @param iterationResults The list of iteration results.
     * @return A string containing the iteration numbers separated by hyphens.
     */
    private static String getIterationNumbersString(List<IterationResult> iterationResults) {
        StringBuilder iterationNumbers = new StringBuilder();
        for (int i = 0; i < iterationResults.size(); i++) {
            if (i > 0) {
                iterationNumbers.append("-");
            }
            iterationNumbers.append(iterationResults.get(i).getIterationNumber());
        }
        return iterationNumbers.toString();
    }

    /**
     * Generates a log history in CSV format based on the iteration results.
     * Appends the log history to a CSV file specified in the FileHandler.
     * <p>
     * If file did not yet exists, create one and write header.
     * Otherwise, append row.
     * <p>
     * Each run gets one row with the following information:
     * FuzzAttemptNr - Time and date - Elapsed time - Counts of each of the iterations with the errorcodes.
     *
     * @param iterationResultsByErrorCode A map of iteration results grouped by error code.
     * @param elapsedTime                 The elapsed time of the iteration process.
     */
    public void generateLogHistory(Map<Integer, List<IterationResult>> iterationResultsByErrorCode, long elapsedTime) {
        try {
            FileWriter writer = new FileWriter(logHistoryFilePath, true);
            BufferedWriter csvWriter = new BufferedWriter(writer);

            // Write header
            int runAttempt;
            if (countLines(logHistoryFilePath) == 0) {
                csvWriter.append("FuzzAttemptNr,Timestamp,ExecutionTime,ExitCode0,ExitCode1,ExitCode10,ExitCodeOther").append("\n");
                runAttempt = 1; // only counts what has been written away already
            } else {
                runAttempt = countLines(logHistoryFilePath);
            }
            ;

            for (Map.Entry<Integer, List<IterationResult>> entry : iterationResultsByErrorCode.entrySet()) {
                int errorCode = entry.getKey();
                List<IterationResult> results = entry.getValue();

                int exitCode0Count = getExitCodeCount(results, 0);
                int exitCode1Count = getExitCodeCount(results, 1);
                int exitCode10Count = getExitCodeCount(results, 10);
                int exitCodeOtherCount = results.size() - exitCode0Count - exitCode1Count - exitCode10Count;

                csvWriter.append(Integer.toString(runAttempt)).append(","); //going to add this line after counting
                csvWriter.append(getCurrentTimestamp()).append(",");
                csvWriter.append(getExecutionTime(elapsedTime)).append(",");
                csvWriter.append(Integer.toString(exitCode0Count)).append(", ");
                csvWriter.append(Integer.toString(exitCode1Count)).append(",");
                csvWriter.append(Integer.toString(exitCode10Count)).append(",");
                csvWriter.append(Integer.toString(exitCodeOtherCount)).append("\n");
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Retrieves the count of iteration results with a specific exit code from a list of iteration results.
     *
     * @param results  The list of iteration results.
     * @param exitCode The exit code to count.
     * @return The count of iteration results with the specified exit code.
     */
    private static int getExitCodeCount(List<IterationResult> results, int exitCode) {
        int count = 0;
        for (IterationResult result : results) {
            if (result.getErrorCode() == exitCode) {
                count++;
            }
        }
        return count;
    }

    /**
     * Retrieves the current timestamp in the format "dd-MM-yyyy HH:mm".
     *
     * @return The current timestamp as a formatted string.
     */
    private static String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return dateFormat.format(new Date());
    }


    /**
     * Retrieves the execution time in milliseconds as a formatted string in the format "mm:ss".
     *
     * @param elapsedTime The elapsed time in milliseconds.
     * @return The execution time as a formatted string.
     */
    private static String getExecutionTime(long elapsedTime) {
        long minutes = (elapsedTime / 1000) / 60;
        long seconds = (elapsedTime / 1000) % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Generates a log error history in CSV format based on the iteration results.
     * Appends the log error history to a CSV file specified in the FileHandler.
     * <p>
     * If file did not yet exists, create one and write header.
     * Otherwise, append rows.
     * <p>
     * Each unique combination of run (and timestamp) - errorcode -  output message is one row.
     * Counts instances.
     *
     * @param iterationResultsByOutputMessage A map of iteration results grouped by output message.
     */
    public void generateLogErrorHistory(Map<String, List<IterationResult>> iterationResultsByOutputMessage) {
        try {
            FileWriter writer = new FileWriter(FileHandler.logErrorHistoryFilePath, true);
            BufferedWriter csvWriter = new BufferedWriter(writer);

            // Write header
            if (countLines(logHistoryFilePath) == 2) { // created one line & header in other file
                csvWriter.append("FuzzAttemptNr,TimeStamp,ErrorCode,OutputMessage,Count").append("\n");
            }

            List<String> sortedOutputMessages = new ArrayList<>(iterationResultsByOutputMessage.keySet());
            sortedOutputMessages.sort(Comparator.naturalOrder());

            for (String outputMessage : sortedOutputMessages) {
                List<IterationResult> results = iterationResultsByOutputMessage.get(outputMessage);

                Map<Integer, Integer> errorCodeCounts = new HashMap<>();
                for (IterationResult result : results) {
                    int errorCode = result.getErrorCode();
                    errorCodeCounts.put(errorCode, errorCodeCounts.getOrDefault(errorCode, 0) + 1);
                }

                List<Integer> sortedErrorCodes = new ArrayList<>(errorCodeCounts.keySet());
                sortedErrorCodes.sort(Comparator.naturalOrder());

                for (int errorCode : sortedErrorCodes) {
                    int count = errorCodeCounts.get(errorCode);
                    csvWriter.append(Integer.toString(countLines(logHistoryFilePath) - 1)).append(","); //already added new line and header in that file
                    csvWriter.append(getCurrentTimestamp()).append(",");
                    csvWriter.append(Integer.toString(errorCode)).append(",");
                    csvWriter.append(outputMessage.replace("\n", "").replace(";", "")).append(",");
                    csvWriter.append(Integer.toString(count)).append("\n");
                }
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Counts the lines in a file.
     * Lines are only counted when writer on that file is closed. Otherwise, previous verison of file is line counted.
     *
     * @param filePath path to the csv file
     * @return int Numbers of files in path (heading counts as a row)
     */
    private int countLines(String filePath) {
        int lineCount = 0; // but dont need to count header, so cool
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while (reader.readLine() != null) {
                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lineCount;
    }
}
