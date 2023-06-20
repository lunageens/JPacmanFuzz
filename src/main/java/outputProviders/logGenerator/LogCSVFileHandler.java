package outputProviders.logGenerator;

import organizers.FileHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static organizers.FileHandler.fuzzAttemptNr;
import static organizers.FileHandler.logFullHistoryFilePath;

/**
 * Class to handle operations done on the CSV files, such as modifying paths in CSV log files, parsing CSV lines, counting
 * lines of a CSV file and formatting a string for writing in a CSV file.
 */
public class LogCSVFileHandler {

    /**
     * Modifies the "Map File Path" columns in the CSV file to reflect the previous run attempt.
     * Precondition: not called in a first run attempt
     */
     public static void modifyMapFilePathColumn() {
        assert fuzzAttemptNr > 1: "Cannot modify previous run paths during the first run attempt.";
        try {
            String csvFilePath = logFullHistoryFilePath;
            File tempFile = File.createTempFile("tempFile", ".csv"); // create temporary file to write to
            BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            String header = reader.readLine(); // read first line of CSV file already stored, and write it in the new one
            writer.write(header);
            writer.newLine();
            String line;
            while ((line = reader.readLine()) != null) { // read all lines
                List<String> values = parseCSVLine(line); // get list of values in csv line
                if (fuzzAttemptNr != Integer.parseInt(values.get(0))) { // only for lines that are from the previous run attempt
                    if (values.size() > 10) { // In absolute path
                        String mapFilePath = values.get(9).trim();
                        String fuzzAttemptNr = values.get(0).trim();
                        if (!mapFilePath.isEmpty() && !mapFilePath.equals("Absolute Map File Path")) {
                            String modifiedMapFilePath = mapFilePath.replace("actual_maps", "previous_maps" + "\\" + "run_" + fuzzAttemptNr);
                            values.set(9, modifiedMapFilePath);
                            String relativeModifiedFilePath = FileHandler.normalizeFilePath(modifiedMapFilePath, false, true); // modify relative path based on this one
                            values.set(10, relativeModifiedFilePath);
                        }
                    }
                }
                writer.write(String.join(",", values)); // write new values away to temporary files
                writer.newLine();
            }
            reader.close();
            writer.close();
            // Replace the original file with the modified file
            File originalFile = new File(csvFilePath);
            if (originalFile.exists()) {
                boolean succesDelete = originalFile.delete();
                assert succesDelete: "Could not delete original log history file.";
            }
            boolean succesRename = tempFile.renameTo(originalFile);
            assert succesRename: "Could not rename temporary log history file.";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses a line of CSV data and splits it into a list of values.
     * Handles values enclosed in double quotes and respects the CSV comma delimiter.
     *
     * @param line the line of CSV data to parse
     * @return a list of values extracted from the CSV line
     */
    public static List<String> parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean withinQuotes = false;
        StringBuilder currentValue = new StringBuilder();
        // Loop over each character in line.
        // e.g. line = "1,"John Doe","john.doe@example.com",25"
        for (char c : line.toCharArray()) {
            if (c == '"') { //
                withinQuotes = !withinQuotes; // The current value is a string and thus enclosed in ""
            } else if (c == ',' && !withinQuotes) {
                values.add(currentValue.toString()); // Add previous value to list and start new string with new value
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c); // Add part of this value to the string of this value
            }
        }
        values.add(currentValue.toString());
        return values;
    }

    /**
     * Replaces all comma's and line breaks in a string with empty strings.
     * @param unformattedString string to be formatted
     * @return formatted string with no commas or line breaks
     */
    public static String CSVReplacement(String unformattedString) {
        unformattedString = unformattedString.replace(",", "");
        unformattedString = unformattedString.replace("\n", "");
        return unformattedString;
    }

    /**
     * Counts the lines in a file.
     * Lines are only counted when writer on that file is closed. Otherwise, previous verison of file is line counted.
     * Not in use anymore. Is replaced with the fuzzAttemptNr variable.
     *
     * @param filePath path to the csv file
     * @return int Numbers of files in path (heading counts as a row)
     */
    private int countLines(String filePath) {
        int lineCount = 0; // but don't need to count header, so cool
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
