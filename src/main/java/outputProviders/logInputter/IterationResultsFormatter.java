package outputProviders.logInputter;

import organizers.FileHandler;
import outputProviders.IterationResult;

import java.util.List;

import static outputProviders.logGenerator.LogCSVFileHandler.CSVReplacement;

/**
 * This class formats some attributes of the total iteration results, such as iterations numbers together, amount of total
 * iterations, fuzz attempt number, exit counts, execution times, current times, and so on.
 */
@SuppressWarnings("DuplicatedCode")
public class IterationResultsFormatter {

    /**
     * Results that need formatting and analyzing.
     */
    private final List<IterationResult> iterationResults;

    /**
     * Constructor for IterationResultsFormatter.
     *
     * @param iterationResults
     *         - list of iteration results to be formatted.
     */
    public IterationResultsFormatter(List<IterationResult> iterationResults) {
        this.iterationResults = iterationResults;
    }

    /**
     * Retrieves a string representation of iteration numbers from a list of iteration results.
     * e.g., [1, 2, 3] becomes "1-2-3"
     *
     * @param titlePrefix
     *         - if true, the variable name will be prefixed with "All Iteration Numbers: "
     * @param CSVReplacement
     *         -if true,  comma's and line breaks in the name will be removed.
     * @param appendComma
     *         - if true, a comma will be appended
     * @param appendNewLine
     *         - if true, a newline will be appended
     *
     * @return A string containing the iteration numbers separated by hyphens.
     */
    public String getIterationNumbersString(boolean titlePrefix, boolean CSVReplacement, boolean appendComma, boolean appendNewLine) {
        StringBuilder iterationNumbers = new StringBuilder();
        for (int i = 0; i < iterationResults.size(); i++) {
            if (i > 0) {
                iterationNumbers.append("-");
            }
            IterationResultFormatter form = new IterationResultFormatter(iterationResults.get(i));
            iterationNumbers.append(form.getFormattedIterationNumber());
        }
        if (titlePrefix) {
            iterationNumbers = new StringBuilder(LogFileCalculator.getFullVariableName("allIterations", true) + iterationNumbers);
        }
        if (CSVReplacement) {
            iterationNumbers = new StringBuilder(CSVReplacement(iterationNumbers.toString()));
        }
        if (appendComma) {
            iterationNumbers = new StringBuilder(iterationNumbers + ",");
        }
        if (appendNewLine) {
            iterationNumbers = new StringBuilder(iterationNumbers + "\n");
        }
        return iterationNumbers.toString();
    }

    /**
     * Default values of getIterationNumbersString()
     *
     * @return A string containing the iteration numbers separated by hyphens, without titles, with replaced commas and newlines, and a comma appended thereafter.
     */
    public String getIterationNumbersString() {

        return getIterationNumbersString(false, true,
                true, false);
    }

    /**
     * Static version of the getIterationNumbersString.
     * Should only be used for iteration results that have one error code in common.
     *
     * @param filteredResults
     *         Results that need formatting and analyzing and only have one error code in common.
     * @param titlePrefix-
     *         if true, the variable name will be prefixed with "All Iteration Numbers:
     * @param CSVReplacement
     *         -if true, comma's and line breaks in the name will be removed.
     * @param appendComma
     *         – if true, a comma will be appended
     * @param appendNewLine
     *         – - if true, a newline will be appended
     *
     * @return A string containing the iteration numbers separated by hyphens.
     */
    public static String getFormattedIterationNumbersString(List<IterationResult> filteredResults, boolean titlePrefix,
                                                            boolean CSVReplacement, boolean appendComma, boolean appendNewLine) {
        IterationResult dummy = filteredResults.get(0);
        for (IterationResult result : filteredResults) {
            assert result.getErrorCode() == dummy.getErrorCode();
        }
        return new IterationResultsFormatter(filteredResults).getIterationNumbersString(titlePrefix, CSVReplacement, appendComma, appendNewLine);
    }

    /**
     * Default version of the static version of the getFormattedIterationNumbersString.
     * @param filteredResults - Results that need formatting and analyzing and only have one error code in common.
     * @return A string containing the iteration numbers of that error code separated by hyphens, without titles, with replaced commas and newlines, and a comma appended thereafter.
     */
    public static String getFormattedIterationNumbersString(List<IterationResult> filteredResults) {
        return getFormattedIterationNumbersString(filteredResults, false, true, true, false);
    }

    /**
     * Gets formatted fuzz number attempt. Is always static because is same for whole application run.
     *
     * @param titlePrefix
     *         - if true, the variable name will be prefixed with "Fuzz Attempt Nr.: "
     * @param CSVReplacement
     *         - if true, comma's and line breaks in the name will be removed.
     * @param appendComma
     *         - if true, a comma will be appended
     * @param appendNewLine
     *         - if true, a newline will be appended
     *
     * @return String Fuzz attempt nr as a string and formatted as required
     */
    public static String getFormattedFuzzAttemptNr(boolean titlePrefix, boolean CSVReplacement, boolean appendComma, boolean appendNewLine) {
        String fuzzAttemptNr = Integer.toString(FileHandler.fuzzAttemptNr);
        if (titlePrefix) {
            fuzzAttemptNr = LogFileCalculator.getFullVariableName("fuzzAttemptNumber", true) + fuzzAttemptNr;
        }
        if (CSVReplacement) {
            fuzzAttemptNr = CSVReplacement(fuzzAttemptNr);
        }
        if (appendComma) {
            fuzzAttemptNr = fuzzAttemptNr + ",";
        }
        if (appendNewLine) {
            fuzzAttemptNr = fuzzAttemptNr + "\n";
        }
        return fuzzAttemptNr;
    }

    /**
     * Default values of getFormattedFuzzAttemptNr()
     *
     * @return String Fuzz attempt nr without titles, with replaced commas and newlines, and a comma appended thereafter.
     */
    public static String getFormattedFuzzAttemptNr() {
        return getFormattedFuzzAttemptNr(false, true, true, false);
    }

    /**
     * Counts the iterations of the given error code.
     *
     * @param exitCode
     *         - the error code to count. Can be 0 1 10 or -1.
     * @param asText
     *         - "occurrences" will be appended after the count number.
     * @param titlePrefix
     *         - if true, the variable name will be prefixed with "Exit code " + exitCode + ": "
     * @param CSVReplacement
     *         - if true, the commas and new lines will be removed.
     * @param appendComma
     *         - if true, a comma will be appended
     * @param appendNewLine
     *         - if true, a newline will be appended
     *
     * @return The number of iterations that had the exit code, formatted as required in a String.
     */
    public String getFormattedExitCount(int exitCode, boolean asText, boolean titlePrefix, boolean CSVReplacement, boolean appendComma, boolean appendNewLine) {
        String exitCount = Integer.toString(LogFileCalculator.getExitCodeCount(iterationResults, exitCode));
        //noinspection DuplicatedCode
        if (titlePrefix) {
            exitCount = LogFileCalculator.getFullVariableName("exitCount" + exitCode, true) + exitCount;
        }
        if (asText) {
            exitCount += " occurrences";
        }
        if (CSVReplacement) {
            exitCount = CSVReplacement(exitCount);
        }
        if (appendComma) {
            exitCount = exitCount + ",";
        }
        if (appendNewLine) {
            exitCount = exitCount + "\n";
        }
        return exitCount;
    }

    /**
     * Default values of getFormattedExitCount()
     *
     * @param exitCode
     *         - the error code to count. Can be 0 1 10 or -1.
     *
     * @return The number of iterations that had the exit code, without titles or text, with newlines and commas replaced,and with a comma appended afterwards.
     */
    public String getFormattedExitCount(int exitCode) {
        return getFormattedExitCount(exitCode, false, false, true, true, false);
    }

    /**
     * Static version to get formatted exit count. Is only meant to be used with filtered results that only have one error code.
     * Could also just take size() if no format needed.
     *
     * @param iterationResults
     *         - the list of iteration results with only one exit code to get the exit count from.
     * @param asText
     *         - "occurrences" will be appended after the count number.
     * @param titlePrefix
     *         - if true, the variable name will be prefixed with "Exit code " + exitCode + ": "
     * @param CSVReplacement
     *         - if true, newlines and commas are removed.
     * @param appendComma
     *         - if true, a comma is appended afterwards.
     * @param appendNewLine
     *         - if true, a new line is appended afterwards.
     *
     * @return Exit count of the only error code in the results, formatted as required in a String.
     */
    public static String getFormattedExitCount(List<IterationResult> iterationResults, boolean asText, boolean titlePrefix, boolean CSVReplacement, boolean appendComma, boolean appendNewLine) {
        IterationResult dummy = iterationResults.get(0);
        int errorCode = dummy.getErrorCode();
        for (IterationResult iterationResult : iterationResults) {
            assert iterationResult.getErrorCode() == errorCode;
        }
        IterationResultsFormatter dummyFormatter = new IterationResultsFormatter(iterationResults);
        return dummyFormatter.getFormattedExitCount(errorCode, asText, titlePrefix, CSVReplacement, appendComma, appendNewLine);
    }

    /**
     * Default values of getFormattedExitCount().
     *
     * @param iterationResults
     *         - the list of iteration results with only one exit code to get the exit count from.
     *
     * @return Exit count of the only error code in the results, without titles or text, with newlines and commas replaced, and with a comma appended afterwards.
     */
    public static String getFormattedExitCount(List<IterationResult> iterationResults) {
        return getFormattedExitCount(iterationResults, false, false, true, true, false);
    }

    /**
     * Should only be used carefully, if one is completely sure of count. Defers from the getExitCount method
     * in LogFileCalculator.
     *
     * @param ErrorCode
     *         - the error code to count. Can be 0 1 10 or -1.
     * @param ErrorCount
     *         - the number of iterations that had the error code.
     * @param asText
     *         - "occurrences" will be appended after the count number.
     * @param titlePrefix
     *         - if true, the variable name will be prefixed with "Exit code " + exitCode + ": "
     * @param CSVReplacement
     *         - if true, newlines and commas are removed.
     * @param appendComma
     *         - if true, a comma is appended afterwards.
     * @param appendNewLine
     *         - if true, a new line is appended afterwards.
     *
     * @return The number of iterations that had the exit code, formatted as required in a String.
     */
    public static String getFormattedExitCount(int ErrorCode, int ErrorCount, boolean asText, boolean titlePrefix, boolean CSVReplacement, boolean appendComma, boolean appendNewLine) {
        String exitCount = Integer.toString(ErrorCount);
        String exitCode = Integer.toString(ErrorCode);
        //noinspection DuplicatedCode
        if (titlePrefix) {
            exitCount = LogFileCalculator.getFullVariableName("exitCount" + exitCode, true) + exitCount;
        }
        if (asText) {
            exitCount += " occurrences";
        }
        if (CSVReplacement) {
            exitCount = CSVReplacement(exitCount);
        }
        if (appendComma) {
            exitCount = exitCount + ",";
        }
        if (appendNewLine) {
            exitCount = exitCount + "\n";
        }
        return exitCount;
    }

    /**
     * Default version of the static getFormattedExitCount() method with use of own count.
     * @param ErrorCode - the error code to count. Can be 0 1 10 or -1.
     * @param ErrorCount - the number of iterations that had the error code.
     * @return The number of iterations that had the exit code, without titles or text, with newlines and commas replaced, and with a comma appended afterwards.
     */
    public static String getFormattedExitCount(int ErrorCode, int ErrorCount) {
        return getFormattedExitCount(ErrorCode, ErrorCount, false, false, true, true, false);
    }
    /**
     * Gets the total number of iterations as a string.
     *
     * @param titlePrefix
     *         - if true, the variable name will be prefixed with "Total Iterations: "
     * @param CSVReplacement
     *         -if true, newlines and commas are removed.
     * @param appendComma
     *         if true, a comma is appended afterwards.
     * @param appendNewLine-
     *         if true, a new line is appended afterwards.
     *
     * @return The total number of iterations, formatted as required in a String.
     */
    public String getFormattedTotalIterations(boolean titlePrefix, boolean CSVReplacement, boolean appendComma, boolean appendNewLine) {
        int totalIterations = iterationResults.size();
        String totalIterationsString = Integer.toString(totalIterations);
        if (titlePrefix) {
            totalIterationsString = LogFileCalculator.getFullVariableName("totalIterations", true) + totalIterationsString;
        }
        if (CSVReplacement) {
            totalIterationsString = CSVReplacement(totalIterationsString);
        }
        if (appendComma) {
            totalIterationsString = totalIterationsString + ",";
        }
        if (appendNewLine) {
            totalIterationsString = totalIterationsString + "\n";
        }
        return totalIterationsString;
    }

    /**
     * Default values of getFormattedTotalIterations()
     *
     * @return Total number of iterations, with newlines and commas replaced, and with a comma appended afterwards.
     */
    public String getFormattedTotalIterations() {
        return getFormattedTotalIterations(false, true, true, false);
    }

    /**
     * Gives the execution time formatted properly. Static because it is only used in the main method, and calculated once there.
     * Therefore, remains the same over whole run.
     *
     * @param elapsedTime
     *         Time start to finish of iterationresults (see Fuzzer main method).
     * @param titlePrefix
     *         - if true, the variable name will be prefixed with "Execution time: "
     * @param CSVReplacement
     *         -if true, newlines and commas are removed.
     * @param appendComma
     *         - if true, a comma is appended afterwards.
     * @param appendNewLine
     *         - if true, a new line is appended afterwards.
     *
     * @return Execution time, formatted as required in a String.
     */
    public static String getFormattedExecutionTime(long elapsedTime, boolean titlePrefix, boolean CSVReplacement, boolean appendComma, boolean appendNewLine) {
        String totalTime = LogFileCalculator.getExecutionTime(elapsedTime);
        if (titlePrefix) {
            totalTime = LogFileCalculator.getFullVariableName("executedTime", true) + totalTime;
        }
        if (CSVReplacement) {
            totalTime = CSVReplacement(totalTime);
        }
        if (appendComma) {
            totalTime = totalTime + ",";
        }
        if (appendNewLine) {
            totalTime = totalTime + "\n";
        }
        return totalTime;
    }

    /**
     * Default values of getFormattedExecutionTime()
     *
     * @param elapsedTime
     *         Time start to finish of iterationresults (see Fuzzer main method).
     *
     * @return Execution time formatted from the elapsedTime, with newlines and commas replaced, and with a comma appended afterwards.
     */
    public static String getFormattedExecutionTime(int elapsedTime) {
        return getFormattedExecutionTime(elapsedTime, false, true, true, false);
    }

    /**
     * Gets the current timestamp formatted properly. Static because it is only used in the main method, and calculated once there.
     * Therefore, remains the same over whole run.
     *
     * @param titlePrefix
     *         - if true, the variable name will be prefixed with "Date and Time: "
     * @param CSVReplacement
     *         -if true, newlines and commas are removed.
     * @param appendComma
     *         - if true, a comma is appended afterwards.
     * @param appendNewLine
     *         - if true, a new line is appended afterwards.
     *
     * @return Current timestamp, formatted as required in a String.
     */
    public static String getFormattedTimeStamp(boolean titlePrefix, boolean CSVReplacement, boolean appendComma, boolean appendNewLine) {
        String timeStamp = LogFileCalculator.getCurrentTimestamp(); // is already dd-MM-HH HH:mm
        if (titlePrefix) {
            timeStamp = LogFileCalculator.getFullVariableName("timeStamp", true) + timeStamp;
        }
        if (CSVReplacement) {
            timeStamp = CSVReplacement(timeStamp);
        }
        if (appendComma) {
            timeStamp = timeStamp + ",";
        }
        if (appendNewLine) {
            timeStamp = timeStamp + "\n";
        }
        return timeStamp;
    }

    /**
     * Default values of getFormattedTimeStamp()
     *
     * @return Current timestamp formatted from the current timestamp, with newlines and commas replaced, and with a comma appended afterwards.
     */
    public static String getFormattedTimeStamp() {
        return getFormattedTimeStamp(false, true, true, false);
    }

}
