package outputProviders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static outputProviders.FileHandler.actualMapsDirectoryPath;

/**
 * Represents the result of an iteration in the application.
 * Contains information such as iteration number, map file type, map file path,
 * string sequence, error code, and output messages.
 */
public class IterationResult {

    /**
     * The number of the iteration.
     */
    private final int iterationNumber;

    /**
     * The file type of the map.
     */
    private final String mapFileType;

    /**
     * The file path of the map.
     */
    private final String mapFilePath;

    /**
     * The string sequence associated with the iteration.
     */
    private final String stringSequence;

    /**
     * The error code associated with the iteration.
     */
    private final int errorCode;

    /**
     * The output messages produced during the iteration.
     */
    private final String outputMessages;

    private final String customAttribute;

    /**
     * Constructs an IterationResult object with the specified parameters.
     *
     * @param iterationNumber The number of the iteration.
     * @param mapFilePath     The file path of the map.
     * @param stringSequence  The string sequence associated with the iteration.
     * @param errorCode       The error code associated with the iteration.
     * @param outputMessages  The output messages produced during the iteration.
     */
    public IterationResult(int iterationNumber, String mapFilePath, String stringSequence, int errorCode, String outputMessages, String customAttribute) {
        this.iterationNumber = iterationNumber;
        this.mapFileType = mapFilePath.substring(mapFilePath.lastIndexOf('.') + 1).toUpperCase();
        this.mapFilePath = mapFilePath;
        this.stringSequence = stringSequence;
        this.errorCode = errorCode;
        this.outputMessages = outputMessages;
        this.customAttribute = customAttribute;
    }

    /**
     * Returns the number of the iteration.
     *
     * @return The iteration number.
     */
    public int getIterationNumber() {
        return iterationNumber;
    }

    /**
     * Returns the file type of the map, based on its suffix in the file path.
     *
     * @return The map file type.
     */
    public String getMapFileType() {
        return mapFileType;
    }

    /**
     * Returns the file path of the map. Note that map files get moved after making them.
     *
     * @return The map file path, cleaned up.
     */
    public String getMapFilePath() {

            // Determine the path the file is on now
            Path sourcePath = Paths.get(mapFilePath);
            String mapFileName = sourcePath.getFileName().toString();

            // Determine the path that the file should ultimately have, based on its exit code
            String exitDirectoryName;
            int exitCodeMap = getErrorCode();
            switch (exitCodeMap) {
                case 0 -> exitDirectoryName = "exitcode0_accepted";
                case 1 -> exitDirectoryName = "exitcode1_crash";
                case 10 -> exitDirectoryName = "exitcode10_rejected";
                default -> exitDirectoryName = "exitcodeX_unknown";
            }
            String destinationPathText = actualMapsDirectoryPath + "/" + exitDirectoryName + "/" + mapFileName;
            Path destinationPath = Paths.get(destinationPathText);
            String mapFilePath = FileHandler.normalizeFilePath(String.valueOf(destinationPath));

        return mapFilePath;
    }

    /**
     * Returns the string sequence associated with the iteration.
     *
     * @return The string sequence.
     */
    public String getStringSequence() {
        return stringSequence;
    }

    /**
     * Returns the error code associated with the iteration.
     *
     * @return The error code.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the output messages produced during the iteration.
     *
     * @return The output messages.
     */
    public String getOutputMessages() {
        return outputMessages;
    }

    /**
     * Get the note of the program with this particular map file if applicable.
     * @return String the custom map attribute
     */
    public String getCustomAttribute(){return customAttribute;}

}
