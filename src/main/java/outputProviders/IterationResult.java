package outputProviders;

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

    /**
     * Constructs an IterationResult object with the specified parameters.
     *
     * @param iterationNumber The number of the iteration.
     * @param mapFilePath     The file path of the map.
     * @param stringSequence  The string sequence associated with the iteration.
     * @param errorCode       The error code associated with the iteration.
     * @param outputMessages  The output messages produced during the iteration.
     */
    public IterationResult(int iterationNumber, String mapFilePath, String stringSequence, int errorCode, String outputMessages) {
        this.iterationNumber = iterationNumber;
        this.mapFileType = mapFilePath.substring(mapFilePath.lastIndexOf('.') + 1).toUpperCase();
        this.mapFilePath = mapFilePath;
        this.stringSequence = stringSequence;
        this.errorCode = errorCode;
        this.outputMessages = outputMessages;
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
     * Returns the file path of the map.
     *
     * @return The map file path.
     */
    public String getMapFilePath() {
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
}
