package randomGenerators.map;

import organizers.FileHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.String.valueOf;

/**
 * This class generates random text-based maps based on the RandomMapGenerator class.
 * It provides functionality to generate text-based maps with random content.
 */
@SuppressWarnings("DuplicatedCode")
public class RandomTextMapGenerator extends MapGenerator {

    /**
     * Maximum height the map can have.
     */
    private int maxHeight;

    /**
     * Maximum width the map can have.
     */
    private int maxWidth;

    /**
     * Valid characters that are allowed in a map.
     */
    private static List<Character> validChar;

    /**
     * Constructs a RandomTextMapGenerator with the specified maximum height and width.
     *
     * @param maxHeight The maximum height of the generated text-based maps.
     * @param maxWidth  The maximum width of the generated text-based maps.
     */
    public RandomTextMapGenerator(int maxHeight, int maxWidth) {
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
        List<Character> validChar = new ArrayList<>();
        validChar.add('M');
        validChar.add('W');
        validChar.add('P');
        validChar.add('0');
        validChar.add('F');
        this.validChar = validChar;
    }

    /**
     * Creates a new .txt document in the actual maps directory with the map name, and stores the file path of
     * that document in a string.
     * @param fileName  The name of the file that will be created, with extension.
     * @param lines A list of strings, where each string is a row in the map.
     * @return A string, that is the file path of the newly generated .txt file that has those lines as text.
     */
    public static String writeMapAway(List<String> lines, String fileName) {
        String filePath = FileHandler.actualMapsDirectoryPath + '\\' + fileName;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }

    /**
     * Generates a random text-based map file with ASCII characters.
     *
     * @return The file path of the generated text-based map file.
     */
    @Override
    public String generateRandomMap() {
        List<String> lines = new ArrayList<>();

        // map size
        Random random = new Random();
        int mapHeight = random.nextInt(maxHeight);
        int mapWidth = random.nextInt(maxWidth);

        // map content
        for (int row = 0; row < mapHeight; row++) {
            StringBuilder line = new StringBuilder();
            for (int column = 0; column < mapWidth; column++) {
                char c = (char) (random.nextInt(26) + 'a');
                line.append(c);
            }
            lines.add(line.toString());
        }

        // write away
        String filePath = writeMapAway(lines, generateRandomMapFileName(".txt"));
        return filePath;
    }

    /**
     * Get a one-line text map, with the characters specified in the string.
     *
     * @param mapLine
     *         Only line of the text map u want to create
     *
     * @return String FilePath of the created textfile.
     */
    public String generateCustomTextMapOneLine(String mapLine) {
        List<String> lines = new ArrayList<>();

        // map content
        lines.add(mapLine);

        // write away
        String filePath = writeMapAway(lines, generateRandomMapFileName(".txt"));
        return filePath;
    }

    /**
     * Create a text file with the valid characters (M, P, W, 0 and F). The number of rows is randomly chosen.
     * The number of columns differs for each row and is randomly chosen for each row as well.
     * The number of times that each valid character occurs in the map is also randomly chosen.
     * @return String Filepath of the generated text file.
     */
    public String generateRandomValidCharRandomSizeTextMap() {
        List<String> lines = new ArrayList<>();
        Random random = new Random();

        // Randomly pick content
        int mapHeight = random.nextInt(maxHeight); // Random number of rows
        for (int row = 0; row < mapHeight; row++) {
            StringBuilder line = new StringBuilder();
            int rowWidth = random.nextInt(maxWidth); // Random length of row each row
            for (int column = 0; column < rowWidth; column++) {
                char c = validChar.get(random.nextInt(4)); // Pick one valid char randomly
                line.append(c);
            }
            lines.add(line.toString());
        }

        // write away
        String filePath = writeMapAway(lines, generateRandomMapFileName(".txt"));
        return filePath;
    }

    /**
     * Create a text file with the valid characters (M, P, W, 0 and F). The number of rows is randomly chosen.
     * The number of columns is the same for each row and is randomly chosen.
     * If justOnePlayerCheck is true, the P can only occur in the map once.
     * If foodPresentCheck is true, the F can only occur in the map one or more.
     * Else, the number of times that each valid character occurs in the map is also randomly chosen.
     * This method does not use the checkMap() method because it will alter the map until it passes all checks.
     *
     * @param justOnePlayerCheck
     *         True if the map can only have one P in it
     * @param foodPresentCheck
     *         True if the map should have at least one F in it
     * @param sizeCheck
     *          True if the map should have at least one row and one column
     *
     * @return Filepath of the generated text file as string.
     */
    public String generateRandomValidCharRectangularTextMap(boolean justOnePlayerCheck, boolean foodPresentCheck, boolean sizeCheck) {
        List<String> lines = new ArrayList<>();

        boolean invalidMap = true; // Keep generating until map is valid.

        while (invalidMap) {
            lines = new ArrayList<>(); // Reset lines to empty list each time we try to make valid map.
            Random random = new Random();

            /* Pick random size */
            int mapHeight;
            int mapWidth;
            if (sizeCheck) { // form 1 to max
                mapHeight = random.nextInt(maxHeight -1) + 1;
                mapWidth = random.nextInt(maxWidth -1) + 1;
            } else { // from 0 to max
                mapHeight = random.nextInt(maxHeight);
                mapWidth = random.nextInt(maxWidth);
            }

            /* Initiate variables that record certain map attributes */
            boolean justOnePlayer = false;
            boolean foodPresent = false;
            List<Integer> availablePositions = new ArrayList<>(); // Places that can be replaced by an F or P if needed.

            /* For each position in the map, pick random valid char and record changed map attributes. */
            for (int row = 0; row < mapHeight; row++) {
                StringBuilder line = new StringBuilder();

                for (int column = 0; column < mapWidth; column++) {
                    char c = '-';
                    boolean validCharChosen = false;

                    while (!validCharChosen) {
                        c = validChar.get(random.nextInt(4)); // Pick one valid char randomly
                        if (justOnePlayerCheck && c == 'P' && !justOnePlayer) { // If we need to check, only append player if there is not one yet. Record that there is a player now.
                            line.append(c);
                            justOnePlayer = true;
                            validCharChosen = true;
                        } else if (foodPresentCheck && c == 'F') { // If we need to check, record that there is food now.
                            foodPresent = true;
                            validCharChosen = true;
                        } else if (justOnePlayerCheck && justOnePlayer && c == 'P') {
                            // do nothing
                        } else {
                        // If we need to check, only append player if there is not one yet.
                            line.append(c);
                            validCharChosen = true;
                        }
                    }

                    if (( c != 'P') && ( c != 'F')) { // If this character is not just the only player or food
                        availablePositions.add(row * mapWidth + column); // Record this position as available to be replaced by an F or P.
                    }
                }
                lines.add(line.toString());
            }

            /* Check if 'P' is missing and add it randomly (by replacing another available one) if that's the case */
            if (justOnePlayerCheck && !justOnePlayer) {
                if (!availablePositions.isEmpty()) { // There are available positions to fix it
                    int randomPosition = availablePositions.get(random.nextInt(availablePositions.size()));
                    int randomRow = randomPosition / mapWidth;

                    if (randomRow < lines.size()) { // Check if the row exists in the lines list
                        StringBuilder randomLine = new StringBuilder(lines.get(randomRow));
                        int randomColumn = randomPosition % mapWidth;
                        if (randomColumn < randomLine.length()) { // Check if the column is within the valid range
                            randomLine.setCharAt(randomColumn, 'P');
                            lines.set(randomRow, randomLine.toString());
                            availablePositions.remove(Integer.valueOf(randomPosition)); // Remove the chosen position from available positions
                            justOnePlayer = true; // Record that there is a player now
                        }
                    }
                }
            }

            /* Check if 'F' is missing and add it randomly (by replacing another available one) if that's the case */
            if (foodPresentCheck && !foodPresent) {
                if (!availablePositions.isEmpty()) {
                    int randomPosition = availablePositions.get(random.nextInt(availablePositions.size()));
                    int randomRow = randomPosition / mapWidth;
                    StringBuilder randomLine = new StringBuilder(lines.get(randomRow));
                    int randomColumn = randomPosition % mapWidth;

                    if (randomRow < lines.size()) { // Check if the row exists in the lines list
                        if (randomColumn < randomLine.length()) { // Check if the column is within the valid range
                            randomLine.setCharAt(randomColumn, 'F');
                            lines.set(randomRow, randomLine.toString());
                            availablePositions.remove(Integer.valueOf(randomPosition)); // Remove the chosen position from available positions
                            foodPresent = true; // Record that there is food now
                        }
                    }
                }
            }
            invalidMap = (justOnePlayerCheck && !justOnePlayer) || (foodPresentCheck && !foodPresent); // If we need to check, check if the map is valid.
        }

        /* Write valid map away */
        String filePath = writeMapAway(lines, generateRandomMapFileName(".txt"));
        return filePath;
    }

    /**
     * This method mutates a map and writes the original and all mutated maps away. The mutating is done by
     * taking each separate character in the map and replacing it with other each valid character. Thus, for each character
     * in the map, there are  mutated versions of the map where, in that exact position, the map contains any other
     * valid character ('P', 'F', 'W', 'E', 'M') that was not there originally. If the map is still valid, the map is
     * written away.
     *
     * @param originalFilePath
     *         FilePath of original map file to mutate, relative to the project.
     *
     * @return List of filepaths of original (in original fuzzlessons input directory) and newly created mutated maps (in actual maps directory).
     */
    public List<String> mutateMap(String originalFilePath) {
        List<String> validFilePaths = new ArrayList<>();

        // * read original map and write it away
        // ? delete it from intial map (needed - no, directly moved to error directory i think, see end fuzzer ?)
        String mapContent = FileHandler.getFileText(originalFilePath);
        if (mapContent == null) {
            System.out.println("There was an error during the reading of the initial map or the initial map was empty.");
            return validFilePaths; // Return an empty list if reading was unsuccessful.
        }
        List<String> mapLines = Arrays.asList(mapContent.split("\\n"));
        if (!checkMap(mapLines, true, true, true)) {
            System.out.println("Original map does not meet criteria.");
            return validFilePaths; // Return an empty list if original map does not meet criteria.
            // ?  Should we return empty list in this case, not sure.
        }
        // do not forget to first add original map if this is not the case
        validFilePaths.add(originalFilePath); // I think now it gets automatically moved out of the original location

        // * For each map character, replace it with each validChar and check. Write away if passed checks.
        // for each row
        for (int rowIndex = 0; rowIndex < mapLines.size(); rowIndex++) {
            String row = mapLines.get(rowIndex);
            for (int columnIndex = 0; columnIndex < row.length(); columnIndex++) {
                char initialChar = row.charAt(columnIndex);
                for (char newChar : validChar) {
                    if (newChar != initialChar) {
                        // * Get new map
                        // copy old map
                        List<String> mutatedMapLines = new ArrayList<>(mapLines);
                        // mutate the correct row
                        StringBuilder builder = new StringBuilder(row);
                        builder.setCharAt(columnIndex, newChar);
                        String alteredRow = builder.toString();
                        // replace altered row with original
                        mutatedMapLines.set(rowIndex, alteredRow);
                        // * Check new map. If check is passed, write away
                        if (checkMap(mutatedMapLines, false, true, true)) {
                            String filePath = writeMapAway(mutatedMapLines, generateRandomMapFileName(".txt"));
                            validFilePaths.add(filePath);
                        }
                    }
                }
            }
        }
        return validFilePaths;
    }

    /**
     * Checks if a map meets the specified conditions.
     *
     * @param mapLines
     *         The lines representing the map.
     * @param sizeCheck
     *         True if the map is not empty and if all columns are equal length, all rows as well.
     * @param justOnePlayerCheck
     *         True if the map can only have one 'P' in it.
     * @param foodPresentCheck
     *         True if the map should have at least one 'F' in it.
     *
     * @return True if the map is valid according to the specified conditions.
     */
    private boolean checkMap(List<String> mapLines, boolean sizeCheck, boolean justOnePlayerCheck, boolean foodPresentCheck) {
        boolean validSize = true;
        boolean validContent = true;

        // get sizes. Check if they are 0 < size < max. Each column must have equal width.
        // check number of rows first
        int numberOfRows = mapLines.size();
        if (!((0 < numberOfRows) && (numberOfRows < maxHeight))) {
            validSize = false;
        }
        // number of columns
        for (String line : mapLines) {  // remove new lines characters for correct measurement
            String newLine = line.replaceAll("\\r|\\n", "");
            mapLines.set(mapLines.indexOf(line), newLine);
        }
        int referenceLength = mapLines.get(0).length();
        if (!((0 < referenceLength) && (referenceLength < maxWidth))) {
            validSize = false;
        }
        for (String line : mapLines) {
            int lineLength = line.length();
            if (!(referenceLength == lineLength)) {
                validSize = false;
                break;
            }
        }

        // count player and food
        long playerCount = 0;
        long foodCount = 0;
        for (String line : mapLines) {
            IntStream charactersLine = line.chars(); // char values as numbers of this line
            // filter on numbers = char value of P --> players on the line. Count the elements of that list and add to count
            playerCount += charactersLine.filter(c -> c == 'P').count();
            IntStream charactersLine2 = line.chars(); // char values as numbers of this line
            foodCount += charactersLine2.filter(c -> c == 'F').count();
        }
        if (!(playerCount == 1)) {
            validContent = false;
        }
        if (!(foodCount >= 1)) {
            validContent = false;
        }

        return validSize && validContent;
    }

}

