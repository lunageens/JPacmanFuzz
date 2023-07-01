package outputProviders;

import organizers.FileHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static organizers.FileHandler.actualMapsDirectoryPath;

/**
 * Represents the result of an iteration in the application.
 * Contains information such as iteration number, map file type, map file path,
 * string sequence, error code, and output messages. Also check if the combination
 * of the map and the actions is valid.
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
     * The custom attribute of the map file (often some extra information) associated with the iteration.
     */
    private final String customAttribute;


    /**
     * Constructs an IterationResult object with the specified parameters. Replaces unknown exit codes with -1, and empty output-messages
     * and custom attributes with "None".
     *
     * @param iterationNumber The number of the iteration.
     * @param mapFilePath     The file path of the map.
     * @param stringSequence  The string sequence associated with the iteration.
     * @param exitCode       The error code associated with the iteration.
     * @param outputMessages  The output messages produced during the iteration.
     * @param customAttribute The custom attribute of the map file (often some extra information) associated with the iteration.
     */
    public IterationResult(int iterationNumber, String mapFilePath, String stringSequence, int exitCode, String outputMessages, String customAttribute) {
        this.iterationNumber = iterationNumber;
        this.mapFileType = mapFilePath.substring(mapFilePath.lastIndexOf('.') + 1).toUpperCase();
        this.mapFilePath = mapFilePath;
        this.stringSequence = stringSequence;
        if ((exitCode != 0) && (exitCode != 1) && (exitCode != 10)) {
            exitCode = -1;
        } // If unknown, -1
        this.errorCode = exitCode;

        if (outputMessages.isEmpty()){outputMessages = "None";}
        this.outputMessages = outputMessages;

        if (customAttribute.isEmpty()){customAttribute = "None";}
        this.customAttribute = customAttribute;
    }

    /**
     * Returns the number of the iteration.
     *
     * @return The iteration number.
     */
    public int getIterationNumber() {return iterationNumber;}

    /**
     * Returns the file path of the map. Note that map files get moved to a particular subdirectory of actual_ after making them.
     * This method returns the path of the map file in the correct actual maps subdirectory.
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
        return FileHandler.normalizeFilePath(destinationPathText); // was met backlash eerst and string.valueOf(path)
    }


    /**
     * Return the map file name without the path (as calculated in  FileHandler).
     * @return The map file name with extension
     */
    public String getMapFileName(){
        String mapFilePath = this.getMapFilePath();
        return FileHandler.getFileName(mapFilePath);
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
     *
     * @return String the custom map attribute
     */
    public String getCustomAttribute() {
        return customAttribute;
    }


    /**
     * Checks for each the action sequence if this is a possible move within the map: when the game has already
     * started, and has not ended yet, the player does not move to a wall cell or outside the bounds of the
     * map.
     *
     * @param mapFilePath
     *         The file path of the .txt map file
     * @param actionSequence
     *         The action sequence to be checked.
     *
     * @return True if the actionSequence is true, false otherwise.
     */
    public static boolean isValidMove(String mapFilePath, String actionSequence) {
        boolean isValidMove = true;
        List<String> executedStrings = extractSubstrings(actionSequence);
        char[][] map = getMap(mapFilePath);
        int[] playerCoordinates = getPlayerLocation(map);
        for (String s : executedStrings) {
            char[] actions = s.toCharArray();
            for (char c : actions) {
                switch (c) {
                    case 'U' -> playerCoordinates[0]--;
                    case 'D' -> playerCoordinates[0]++;
                    case 'L' -> playerCoordinates[1]--;
                    case 'R' -> playerCoordinates[1]++;
                }
                // if player moves out of bounds of map
                if (playerCoordinates[0] < 0 || playerCoordinates[0] >= map.length || playerCoordinates[1] < 0 || playerCoordinates[1] >= map[0].length) {
                    isValidMove = false;
                    break;
                }
                // if player moves to wall cell
                if (map[playerCoordinates[0]][playerCoordinates[1]] == 'W') {
                    isValidMove = false;
                    break;
                }
            }
        }
        return isValidMove;
    }

    /**
     * From a full action sequence, get the strings that actually are executed (Starting with
     * the last S and ending with the first E).
     *
     * @param input
     *         The full action sequence.
     *
     * @return A list of strings that are executed.
     */
    public static List<String> extractSubstrings(String input) {
        List<String> substrings = new ArrayList<>();
        int prevEIndex = -1; // Initialize the previous "E" index to -1

        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == 'S') {
                for (int j = i + 1; j < input.length(); j++) {
                    if (input.charAt(j) == 'E') {
                        if (prevEIndex == -1 || i > prevEIndex) { // no previous E found or S is after the previous E
                            substrings.add(input.substring(i, j + 1));
                        }
                        prevEIndex = j; // Update the previous "E" index
                        break;
                    }
                }
            }
        }
        return substrings;
    }

    /**
     * Makes a map that stores each char at its x and y coordinate, from a .txt file that
     * represents the map.
     *
     * @param mapFilePath
     *         The path to the .txt file that represents the map.
     *
     * @return A 2D array of chars that represents the map.
     */
    public static char[][] getMap(String mapFilePath) {
        List<String> lines = new ArrayList<>();
        // Get lines of file
        try (BufferedReader reader = new BufferedReader(new FileReader(mapFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Store height and width map
        int height = lines.size();
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, line.length());
        }

        // Make map that stores each char at its x and y coordinate
        char[][] map = new char[height][width];
        for (int i = 0; i < height; i++) {
            String line = lines.get(i);
            for (int j = 0; j < width; j++) {
                if (j < line.length()) {
                    map[i][j] = line.charAt(j);
                } else {
                    map[i][j] = ' '; // Assuming empty space if line is shorter
                }
            }
        }
        return map;
    }

    /**
     * Returns the coordinates of the player within the map.
     *
     * @param map
     *         The map that stores each character and its x and y coordinates.
     *
     * @return An int array of length 2, where the first element is the x coordinate, and the second element is the y coordinate.
     */
    public static int[] getPlayerLocation(char[][] map) {
        int playerRow = -1;
        int playerCol = -1;

        int height = map.length;
        int width = (height > 0) ? map[0].length : 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (map[i][j] == 'P') {
                    playerRow = i;
                    playerCol = j;
                    break;
                }
            }
            if (playerRow != -1) {
                break;
            }
        }
        return new int[]{playerRow, playerCol};
    }
}
