package outputProviders.logInputter;

import outputProviders.IterationResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class contains methods to calculate the statistics of the iteration results of this fuzz attempt, such as exit code
 * counts, unique values, execution time, ... . It also holds the full name of each variable.
 */
public class LogFileCalculator {

    /**
     * Retrieves the count of iteration results with a specific exit code from a list of iteration results.
     *
     * @param results
     *         The list of iteration results.
     * @param exitCode
     *         The exit code to count.
     *
     * @return The count of iteration results with the specified exit code.
     */
    public static int getExitCodeCount(List<IterationResult> results, int exitCode) {
        int count = 0;
        if (exitCode != -1){
            for (IterationResult result : results) {
                if (result.getErrorCode() == exitCode) {
                    count++;
                }
            }
        } else {
            count = results.size() - (getExitCodeCount(results, 0) + getExitCodeCount(results, 1) + getExitCodeCount(results, 10));
        }
        return count;
    }

    /**
     * Retrieves the current timestamp in the format "dd-MM-yyyy HH:mm".
     *
     * @return The current timestamp as a formatted string.
     */
    public static String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return dateFormat.format(new Date());
    }

    /**
     * Retrieves the execution time in milliseconds as a formatted string in the format "mm:ss".
     *
     * @param elapsedTime
     *         The elapsed time in milliseconds.
     *
     * @return The execution time as a formatted string.
     */
    public static String getExecutionTime(long elapsedTime) {
        long minutes = (elapsedTime / 1000) / 60;
        long seconds = (elapsedTime / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Gets the unique values for a specific variable name from a list of iteration results.
     * @param results List of iteration results where u need the unique value from.
     * @param variableName Variable name as specified in the getFullVariableName method input.
     * @return List of strings that are the unique values. For error code -> Parsed into String.
     */
    public static List<String> getUniqueValues(ArrayList<IterationResult> results, String variableName) {
        List<String> uniqueValues = new ArrayList<>();
        for (IterationResult result : results) {
            String value = "";
            switch (variableName) {
                case "mapFileType" -> value = result.getMapFileType();
                case "mapFileName" -> value = result.getMapFileName();
                case "mapFilePath" -> value = result.getMapFilePath();
                case "stringSequence" -> value = result.getStringSequence();
                case "customAttribute" -> value = result.getCustomAttribute();
                case "outputMessages" -> value = result.getOutputMessages();
                case "errorCode" -> value = String.valueOf(result.getErrorCode());
            }
            if (!uniqueValues.contains(value)) {
                uniqueValues.add(value);
            }
        }
        return uniqueValues;
    }


    /**
     * Get the full variable names of each short name.
     * @param variableName Short name of variable.
     * @param appendColon True if you want to append a colon and a space to the end of the variable name.
     * @return String, full name of the variable in natural language.
     */
    public static String getFullVariableName(String variableName, boolean appendColon) {
        String fullName;
        switch (variableName) {
            case "fuzzAttemptNumber" -> fullName = "Fuzz Attempt Number";
            case "timeStamp" -> fullName = "Date and Time";
            case "executedTime" -> fullName = "Execution Time";
            case "iterationNumber" -> fullName = "Iteration Number";
            case "mapFileType" -> fullName = "Map File Type";
            case "mapFileName" -> fullName = "Map File Name";
            case "mapFilePath" -> fullName = "Absolute Map File Path";
            case "mapFileRelativePath" -> fullName = "Relative Map File Path";
            case "mapFileText" -> fullName = "Map File Text";
            case "stringSequence" -> fullName = "Action Sequence";
            case "errorCode" -> fullName = "Exit Code";
            case "outputMessages" -> fullName = "Output Messages";
            case "customAttribute" -> fullName = "Map File Custom Attribute";
            case "totalIterations" -> fullName = "Total Number of Iterations";
            case "allIterations" -> fullName = "All Iteration Numbers";
            default -> fullName = variableName;
        }
        if (variableName.startsWith("exitCount")){
            if (variableName.equals("exitCount"))
                fullName = "Exit Code Count";
            else
                fullName = "Exit Code " + fullName.substring(9) + " Count";
        }
        if (variableName.startsWith("exitCode")){
            if (variableName.equals("exitCode"))
                fullName = "Exit Code";
            else
                fullName = "Exit Code " + fullName.substring(8) ;
        }
        if(variableName.startsWith("exitIterations")){
            if (variableName.equals("exitIterations")) {
                fullName = "Exit Code Iterations";
            } else{
                fullName = "Exit Code " + fullName.substring(12) + " Iterations";
            }
        }
        if (appendColon) {
            fullName += ": ";
        }
        return fullName;
    }
}