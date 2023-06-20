package outputProviders.logInputter;

import organizers.FileHandler;
import outputProviders.IterationResult;
import outputProviders.logGenerator.LogCSVFileHandler;

import java.util.List;
import java.util.Objects;

import static outputProviders.logGenerator.LogCSVFileHandler.CSVReplacement;

/**
 * This class is used to format individual iteration results and their attributes.
 */
public class IterationResultFormatter {
    /**
     * Result that needs to be formatted.
     */
    private final IterationResult iterationResult;

    /**
     * Constructor for the IterationResultFormatter.
     *
     * @param iterationResult
     *         The result that needs formatting.
     */
    public IterationResultFormatter(IterationResult iterationResult) {

        this.iterationResult = iterationResult;
    }


    /**
     * Formats the iteration number according to a string.
     *
     * @param titlePrefix
     *         True if the iteration number should have the title and colon appended. (E.g., Iteration Number: )
     * @param commaSuffix
     *         True if the iteration number should have comma at the end.
     * @param newLineSuffix
     *         True if the iteration number should have a newline at the end.
     *
     * @return The formatted iteration number.
     */
    public String getFormattedIterationNumber(boolean titlePrefix, boolean commaSuffix, boolean newLineSuffix) {
        String iterationNumber = Integer.toString(iterationResult.getIterationNumber());
        if (titlePrefix) {
            iterationNumber = LogFileCalculator.getFullVariableName("iterationNumber", true) + iterationNumber;
        }
        if (commaSuffix) {
            iterationNumber = iterationNumber + ",";
        }
        if (newLineSuffix) {
            iterationNumber = iterationNumber + "\n";
        }
        return iterationNumber;
    }

    /**
     * Default values of getFormatted iteration number.
     *
     * @return Iteration number with comma appended and without title.
     */
    public String getFormattedIterationNumber() {
        return getFormattedIterationNumber(false, true, false);
    }

    /**
     * Returns the error code as a string.
     *
     * @param asText
     *         - True if the error code should be returned as text. (E.g., 0 becomes Crash [0]. )
     * @param titlePrefix
     *         - True if the variable name and a : should be appended in front. (E.g., Exit Code: )
     * @param CSVReplacement
     *         - True if the comma's and line breaks in the error code should be replaced by an empty string.
     * @param commaSuffix
     *         - True if the error code should have comma at the end.
     * @param newLineSuffix
     *         - True if the error code should have a newline at the end.
     *
     * @return String The formatted error code.
     */
    public String getFormattedErrorCode(boolean asText, boolean titlePrefix, boolean CSVReplacement, boolean commaSuffix,
                                        boolean newLineSuffix) {
        String errorCode = Integer.toString(iterationResult.getErrorCode());
        if (asText) {
            errorCode = errorCode.toUpperCase();
            errorCode = switch (errorCode) {
                case "0" -> "Crash [0]";
                case "1" -> "Accepted [1]";
                case "10" -> "Rejected [10]";
                case "-1" -> "Unknown [-1]";
                default -> errorCode;
            };
        }
        if (titlePrefix) {
            errorCode = LogFileCalculator.getFullVariableName("errorCode", true) + errorCode;
        }
        if (CSVReplacement) {
            errorCode = CSVReplacement(errorCode);
        }
        if (commaSuffix) {
            errorCode = errorCode + ",";
        }
        if (newLineSuffix) {
            errorCode = errorCode + "\n";
        }
        return errorCode;
    }

    /**
     * Default values of getFormatted error code.
     *
     * @return Error code with comma and newline removed, with comma appended.
     */
    public String getFormattedErrorCode() {
        return getFormattedErrorCode(false, false, true,
                true, false);
    }

    /**
     * Returns the error code as a string. Can only be used for filtered results that have one error code in common.
     *
     * @param filteredResults
     *         - The filtered results that have one error code in common.
     * @param asText
     *         - True if the error code should be returned as text. (E.g., 0 becomes Crash [0]. )
     * @param titlePrefix
     *         - True if the variable name and a : should be appended in front. (E.g., Exit Code: )
     * @param CSVReplacement
     *         - True if comma's and line breaks in should be replaced by an empty string.
     * @param commaSuffix
     *         - True if the error code should have comma at the end.
     * @param newLineSuffix
     *         - True if the error code should have a newline at the end.
     *
     * @return String The formatted error code.
     */
    public static String getFormattedErrorCode(List<IterationResult> filteredResults, boolean asText, boolean titlePrefix,
                                               boolean CSVReplacement, boolean commaSuffix, boolean newLineSuffix) {
        IterationResult dupe = filteredResults.get(0);
        for (IterationResult iterationResult : filteredResults) {
            assert iterationResult.getErrorCode() == dupe.getErrorCode();
        }
        IterationResultFormatter dupeForm = new IterationResultFormatter(dupe);
        return dupeForm.getFormattedErrorCode(asText, titlePrefix, CSVReplacement, commaSuffix, newLineSuffix);
    }

    /**
     * Default values of static getFormatted error code.
     *
     * @param filteredResults
     *         The filtered results that have one error code in common.
     *
     * @return Error code with comma and newline removed, with comma appended.
     */
    public static String getFormattedErrorCode(List<IterationResult> filteredResults) {
        return getFormattedErrorCode(filteredResults, false, false, true, true, false);
    }

    /**
     * Returns the output messages as a string.
     *
     * @param replaceUnspecified
     *         - True if empty output messages should be replaced by "None" instead of a space.
     * @param titlePrefix
     *         - True if the variable name and a : should be appended in front. (E.g., Output messages: )
     * @param CSVReplacement
     *         - True if the comma's and line breaks in the output messages should be replaced by an empty string.
     * @param commaSuffix
     *         - True if the output messages should have comma at the end.
     * @param newLineSuffix
     *         - True if the output messages should have a newline at the end.
     *
     * @return String The formatted output messages.
     */
    public String getFormattedOutputMessages(boolean replaceUnspecified, boolean titlePrefix, boolean CSVReplacement,
                                             boolean commaSuffix, boolean newLineSuffix) {
        String outputMessages = iterationResult.getOutputMessages();
        if (!replaceUnspecified) {
            if (outputMessages.equals("None")) {
                outputMessages = " ";
            }
        }
        if (titlePrefix) {
            outputMessages = LogFileCalculator.getFullVariableName("outputMessages", true) + outputMessages;
        }
        if (CSVReplacement) {
            outputMessages = CSVReplacement(outputMessages);
        }
        if (commaSuffix) {
            outputMessages = outputMessages + ",";
        }
        if (newLineSuffix) {
            outputMessages = outputMessages + "\n";
        }
        return outputMessages;
    }

    /**
     * Default values of getFormatted output messages.
     *
     * @return Output messages with comma and newline removed, with comma appended and "None" for empty ones.
     */
    public String getFormattedOutputMessages() {
        return getFormattedOutputMessages(true, false,
                true, true, false);
    }

    /**
     * Returns the output message as a string.
     * Can only be used for a list of filtered iterations results all have one output message in common.
     *
     * @param filteredResults
     *         The list of iterations that have the same output message.
     * @param replaceUnspecified
     *         True if empty output messages should be replaced by a "None" instead of a space.
     * @param titlePrefix
     *         True if the variable name and a : should be appended in front. (E.g., Output messages: )
     * @param CSVReplacement
     *         True if the comma's and line breaks in output messages should be replaced by an empty string.
     * @param commaSuffix
     *         True if the output messages should have comma at the end.
     * @param newLineSuffix
     *         True if the output messages should have a newline at the end.
     *
     * @return String The formatted output messages.
     */
    public static String getFormattedOutputMessages(List<IterationResult> filteredResults, boolean replaceUnspecified,
                                                    boolean titlePrefix, boolean CSVReplacement, boolean commaSuffix, boolean newLineSuffix) {
        IterationResult dupe = filteredResults.get(0);
        IterationResultFormatter dupeForm = new IterationResultFormatter(dupe);
        for (IterationResult iterationResult : filteredResults) {
            assert Objects.equals(iterationResult.getOutputMessages(), dupe.getOutputMessages());
        }
        return dupeForm.getFormattedOutputMessages(replaceUnspecified, titlePrefix, CSVReplacement, commaSuffix, newLineSuffix);
    }

    /**
     * Default values of static getFormatted output messages.
     *
     * @param filteredResults
     *         The list of iterations that have the same output message.
     *
     * @return Output messages with comma and newline removed, with comma appended and "None" for empty ones.
     */
    public static String getFormattedOutputMessages(List<IterationResult> filteredResults) {
        return getFormattedOutputMessages(filteredResults, true, false,
                true, true, false);
    }

    /**
     * Returns the string sequence as one string.
     *
     * @param deCapitalized
     *         - True if all characters in the string sequence should be small.
     * @param fullText
     *         - True if all characters in the string sequence should be replaced by their full meaning (e.g., E becomes Exit).
     * @param replaceUnknown
     *         - True if unknown characters should be replaced by ?
     * @param titlePrefix
     *         - True if the variable name and a : should be appended in front. (E.g., Action Sequence: )
     * @param CSVReplacement
     *          - True if the comma's and line breaks in the string sequence should be replaced wih an empty string.
     * @param commaSuffix
     *         - True if the string sequence type should have comma at the end.
     * @param newLineSuffix
     *         - True if the string sequence type should have a newline at the end.
     *
     * @return String The formatted string sequence.
     */
    public String getFormattedStringSequence(boolean deCapitalized, boolean fullText, boolean replaceUnknown, boolean titlePrefix, boolean CSVReplacement,
                                             boolean commaSuffix, boolean newLineSuffix) {
        String stringSequence = iterationResult.getStringSequence();
        // For player
        char[] charSequence = stringSequence.toCharArray();
        for (char c : charSequence) {
            if (fullText) {
                if (c == 'E') {
                    stringSequence = stringSequence.replace("E", "Exit ");
                } else if (c == 'S') {
                    stringSequence = stringSequence.replace("S", "Start ");
                } else if (c == 'U') {
                    stringSequence = stringSequence.replace("U", "Up ");
                } else if (c == 'D') {
                    stringSequence = stringSequence.replace("D", "Down ");
                } else if (c == 'L') {
                    stringSequence = stringSequence.replace("L", "Left ");
                } else if (c == 'R') {
                    stringSequence = stringSequence.replace("R", "Right ");
                } else if (c == 'Q') {
                    stringSequence = stringSequence.replace("Q", "Quit ");
                } else if (c == 'W') {
                    stringSequence = stringSequence.replace("W", "Wait ");
                }
            }
            if (replaceUnknown && !(c == 'E' || c == 'S' || c == 'U' || c == 'D' || c == 'L' || c == 'R' || c == 'Q' || c == 'W')) {
                stringSequence = stringSequence.replace(c, '?');
            }
        }
        if (deCapitalized) {
            stringSequence = stringSequence.toLowerCase();
        }
        if (titlePrefix) {
            stringSequence = LogFileCalculator.getFullVariableName("stringSequence", true) + stringSequence;
        }
        if (CSVReplacement) {
            stringSequence = CSVReplacement(stringSequence);
        }
        if (commaSuffix) {
            stringSequence = stringSequence + ",";
        }
        if (newLineSuffix) {
            stringSequence = stringSequence + "\n";
        }
        return stringSequence;
    }

    /**
     * Default values of getFormatted string sequence.
     *
     * @return String sequence with comma and newline removed, with comma appended.
     */
    public String getFormattedStringSequence() {
        return getFormattedStringSequence(false, false, false, false, true, true, false);
    }

    /**
     * Returns the name of the map file.
     * @param appendExtension True if the file extension should be appended.
     * @param appendFuzzAttemptNrText True if the fuzz attempt number should be appended  (e.g. Fuzz attempt nr. 1: map_1.txt)
     * @param titlePrefix True if the variable name and a : should be appended in front. (e.g., Map File Name: )
     * @param CSVReplacement True if the map file type should have comma and newline removed.
     * @param commaSuffix True if the map file type should have comma at the end.
     * @param newLineSuffix True if the map file type should have a newline at the end.
     * @return String The formatted map file name.
     */
    public String getFormattedMapFileName(boolean appendExtension, boolean appendFuzzAttemptNrText,
                                          boolean titlePrefix, boolean CSVReplacement, boolean commaSuffix, boolean newLineSuffix) {
        String mapFileName = iterationResult.getMapFileName();
        if (!appendExtension) {
            mapFileName = mapFileName.substring(0, mapFileName.lastIndexOf('.'));
        }
        if (appendFuzzAttemptNrText) {
            mapFileName = "Fuzz attempt nr. " + FileHandler.fuzzAttemptNr + ": " + mapFileName;
        }
        if (titlePrefix) {
            mapFileName = LogFileCalculator.getFullVariableName("mapFileName", true) + mapFileName;
        }
        if (CSVReplacement) {
            mapFileName = CSVReplacement(mapFileName);
        }
        if (commaSuffix) {
            mapFileName = mapFileName + ",";
        }
        if (newLineSuffix) {
            mapFileName = mapFileName + "\n";
        }
        return mapFileName;
    }

    /**
     * Default values of getFormatted map file name.
     *
     * @return Map file name with comma and newline removed, with comma appended, and with extension.
     */
    public String getFormattedMapFileName() {
        return getFormattedMapFileName(true, false,
                false, true, true, false);
    }

    /**
     * Formats the map file type according to a string.
     *
     * @param textOnly
     *         True if the map file type should only be written out as text. (e.g., Unformatted Text Document)
     * @param CSVReplacement
     *         True if the map file type should have comma and newline removed.
     * @param pointPrefix
     *         True if the extension should start with .
     * @param deCapitalize
     *         True if the extension should not be capitalized.
     * @param titlePrefix
     *         True if the variable name and a : should be appended in front. (e.g., Map File Type: )
     * @param asText
     *         True if the extension should additionally be written full-out (e.g. .exe [Executable file])
     * @param asFullText
     *         True if the extension should additionally be written full-out and with extra information (e.g.  .txt [Unformatted Text Document (Text document) as Text Document])
     * @param commaSuffix
     *         True if the map file type should have comma at the end.
     * @param newLineSuffix
     *         True if the map file type should have a newline at the end.
     *
     * @return The formatted map file type.
     */
    public String getFormattedMapFileType(boolean deCapitalize, boolean textOnly, boolean asText, boolean asFullText, boolean pointPrefix,
                                          boolean titlePrefix,
                                          boolean CSVReplacement,
                                          boolean commaSuffix, boolean newLineSuffix) {
        String mapFileType = iterationResult.getMapFileType();
        if (pointPrefix && !mapFileType.startsWith(".")) {
            mapFileType = "." + mapFileType;
        }
        if (deCapitalize) {
            mapFileType = mapFileType.toLowerCase();
        }
        if (asFullText) {
            if (textOnly) {
                mapFileType = FileHandler.FileTypeResolver.formatFileInformation(FileHandler.FileTypeResolver
                        .getFileInformation(mapFileType), true, true, true);
            } else {
                mapFileType = mapFileType + " [" + FileHandler.FileTypeResolver.formatFileInformation(FileHandler.FileTypeResolver
                        .getFileInformation(mapFileType), true, true, true) + "]";
            }
        } else if (asText) {
            if (textOnly) {
                mapFileType = FileHandler.FileTypeResolver.formatFileInformation(FileHandler.FileTypeResolver
                        .getFileInformation(mapFileType), true, true, true);
            } else {
                mapFileType = mapFileType + " [" + FileHandler.FileTypeResolver.formatFileInformation(FileHandler.FileTypeResolver
                        .getFileInformation(mapFileType), true, false, false) + "]";
            }
        }
        if (titlePrefix) {
            mapFileType = LogFileCalculator.getFullVariableName("mapFileType", true) + mapFileType;
        }
        if (CSVReplacement) {
            mapFileType = CSVReplacement(mapFileType);
        }
        if (commaSuffix) {
            mapFileType = mapFileType + ",";
        }
        if (newLineSuffix) {
            mapFileType = mapFileType + "\n";
        }
        return mapFileType;
    }

    /**
     * Default values of getFormatted map file type.
     *
     * @return Decapitalized map file type with comma, full name and point appended, with comma and newline removed.
     */
    public String getFormattedMapFileType() {
        return getFormattedMapFileType(true, false, true, false, true, false, true,
                true, false);
    }

    /**
     * Returns the custom attribute of the map file.
     *
     * @param replaceEmpty
     *         True if the spaces and tabs characters in the custom attribute should be replaced by "Empty or blank string."
     * @param replaceUnspecified
     *         True if map files without a custom attribute should be replaced by "None" instead of a space.
     * @param replaceSpecialChars
     *         True if custom attributes that have special characters such as / and " should be replaced by "String contains special characters."
     * @param titlePrefix
     *         True if the variable name and a : should be appended in front. (e.g. Map File Custom Attribute: )
     * @param CSVReplacement
     *         True if the map file type should have comma and newline removed.
     * @param commaSuffix
     *         True if the map file type should have comma at the end.
     * @param newLineSuffix
     *         True if the map file type should have a newline at the end.
     *
     * @return String The formatted map file custom attribute.
     */
    public String getFormattedMapFileCustomAttribute(boolean replaceEmpty, boolean replaceUnspecified, boolean replaceSpecialChars,
                                                     boolean titlePrefix, boolean CSVReplacement, boolean commaSuffix, boolean newLineSuffix) {

        String customAttribute = iterationResult.getCustomAttribute();
        if (replaceEmpty) {
            if (customAttribute.isBlank() || customAttribute.isEmpty()) { // When custom attribute not specified, the value is set at "N.A." in Fuzzer
                customAttribute = "Empty or blank string.";
            }
        }
        if (!replaceUnspecified) {
            if (customAttribute.equals("None")) {
                customAttribute = " ";
            }
        }
        if (replaceSpecialChars) {
            if (customAttribute.equals("\"") || customAttribute.equals("'")) {
                customAttribute = "String contains special characters.";
            }
        }

        if (titlePrefix) {
            customAttribute = LogFileCalculator.getFullVariableName("customAttribute", true) + customAttribute;
        }
        if (CSVReplacement) {
            customAttribute = CSVReplacement(customAttribute);
        }
        if (commaSuffix) {
            customAttribute = customAttribute + ",";
        }
        if (newLineSuffix) {
            customAttribute = customAttribute + "\n";
        }
        return customAttribute;
    }

    /**
     * Default values of getFormatted map file custom attribute.
     *
     * @return Map file custom attribute with comma and newline removed, with comma appended, and with extension. The empty and special characters are replaced, as well as unspecified map file custom attributes.
     */
    public String getFormattedMapFileCustomAttribute() {
        return getFormattedMapFileCustomAttribute(true, true, true, false, true, true, false);
    }

    /**
     * Returns the actual file path of the map in the correct subdirectory.
     *
     * @param backslash
     *         boolean if backslash should be used instead of forward slash.
     * @param relative
     *         boolean if the relative path should be used instead of the absolute path.
     * @param titlePrefix
     *         True if the variable name and a : should be appended in front (e.g., Absolute (or Relative) Map File Path: ).
     * @param CSVReplacement
     *         True if the map file type should have comma and newline removed.
     * @param commaSuffix
     *         True if the map file type should have comma at the end.
     * @param newLineSuffix
     *         True if the map file type should have a newline at the end.
     *
     * @return String The formatted map file path in the correct actual subdirectory.
     */
    public String getFormattedMapFilePath(boolean backslash, boolean relative, boolean titlePrefix, boolean CSVReplacement,
                                          boolean commaSuffix, boolean newLineSuffix) {
        String mapFilePath = iterationResult.getMapFilePath();
        String mapFilePathFormatted = FileHandler.normalizeFilePath(mapFilePath, backslash, relative);
        if (titlePrefix) {
            if (relative) {
                mapFilePathFormatted = LogFileCalculator.getFullVariableName("mapFileRelativePath", true) + mapFilePathFormatted;
            } else {
                mapFilePathFormatted = LogFileCalculator.getFullVariableName("mapFilePath", true) + mapFilePathFormatted;
            }
        }
        if (CSVReplacement) {
            mapFilePathFormatted = LogCSVFileHandler.CSVReplacement(mapFilePathFormatted);
        }
        if (commaSuffix) {
            mapFilePathFormatted = mapFilePathFormatted + ",";
        }
        if (newLineSuffix) {
            mapFilePathFormatted = mapFilePathFormatted + "\n";
        }
        return mapFilePathFormatted;
    }

    /**
     * Default values of getFormatted map file path.
     *
     * @return Absolute map file path with comma appended afterwards, with /, with comma and newline removed.
     */
    public String getFormattedMapFilePath() {
        return getFormattedMapFilePath(false, false, false,
                true, true, false);
    }


    /**
     * Returns the full map file text as one string.
     * Not possible in CSV File, since map files can be multi line.
     *
     * @param deCapitalized
     *         - True if all characters in the map file should be small.
     * @param asFullText
     *         - True if all characters in the map file should be replaced by their full meaning (e.g., P becomes Player).
     * @param replaceUnknown
     *         - True if unknown characters should be replaced by ?
     * @param titlePrefix
     *         - True if the variable name and a : should be appended in front (e.g.,: Map File Text: )
     * @param commaSuffix
     *         - True if the map file type should have comma at the end.
     * @param newLineSuffix
     *         - True if the map file type should have a newline at the end.
     *
     * @return String The formatted map file text.
     */
    public String getFormattedMapFileText(boolean deCapitalized, boolean asFullText, boolean replaceUnknown, boolean titlePrefix,
                                          boolean commaSuffix, boolean newLineSuffix) {
        String mapFileText = FileHandler.getFileText(iterationResult.getMapFilePath());
        char[] charSequence = mapFileText.toCharArray();
        for (char c : charSequence) {
            if (asFullText) {
                if (c == 'p') {
                    mapFileText = mapFileText.replace("p", "Player ");
                } else if (c == 'M') {
                    mapFileText = mapFileText.replace("M", "Monster ");
                } else if (c == 'W') {
                    mapFileText = mapFileText.replace("W", "Wall ");
                } else if (c == 'F') {
                    mapFileText = mapFileText.replace("F", "Food ");
                } else if (c == '0') {
                    mapFileText = mapFileText.replace("0", "Empty ");
                }
            }
            if (replaceUnknown && !(c == 'M' || c == 'W' || c == 'F' || c == '0')) {
                mapFileText = mapFileText.replace(c, '?');
            }
        }
        if (deCapitalized) {
            mapFileText = mapFileText.toLowerCase();
        }
        if (titlePrefix) {
            mapFileText = LogFileCalculator.getFullVariableName("mapFileText", true) + mapFileText;
        }
        if (commaSuffix) {
            mapFileText = mapFileText + ",";
        }
        if (newLineSuffix) {
            mapFileText = mapFileText + "\n";
        }
        return mapFileText;
    }

    /**
     * Default values of getFormatted map file text.
     *
     * @return Map file text with comma and newline removed, with comma appended.
     */
    public String getFormattedMapFileText() {
        return getFormattedMapFileText(false, false, false,
                false, true, false);
    }
}


