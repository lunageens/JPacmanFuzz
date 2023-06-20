package randomGenerators.map;

import organizers.FileHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private List<Character> validChar;

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
     * Generates a random text-based map file.
     *
     * @return The file path of the generated text-based map file.
     */
    @Override
    public String generateRandomMap() {
        List<String> lines = new ArrayList<>();
        Random random = new Random();

        int mapHeight = random.nextInt(maxHeight);
        int mapWidth = random.nextInt(maxWidth);

        for (int row = 0; row < mapHeight; row++) {
            StringBuilder line = new StringBuilder();
            for (int column = 0; column < mapWidth; column++) {
                char c = (char) (random.nextInt(26) + 'a');
                line.append(c);
            }
            lines.add(line.toString());
        }

        @SuppressWarnings("DuplicatedCode") String fileName = generateRandomMapFileName(".txt");
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
     * Get a one-line text map, with the characters specified in the string.
     *
     * @param mapLine
     *         Only line of the text map u want to create
     *
     * @return String FilePath of the created textfile.
     */
    public String generateCustomTextMapOneLine(String mapLine) {
        String fileName = generateRandomMapFileName(".txt");
        String filePath = FileHandler.actualMapsDirectoryPath + '\\' + fileName;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            writer.write(mapLine);
            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

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

        // Name the file and find the path.
        @SuppressWarnings("DuplicatedCode") String fileName = generateRandomMapFileName(".txt");
        String filePath = FileHandler.actualMapsDirectoryPath + '\\' + fileName;

        // Write the random content to the file.
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
     * Create a text file with the valid characters (M, P, W, 0 and F). The number of rows is randomly chosen.
     * The number of columns is the same for each row and is randomly chosen.
     * If justOnePlayerCheck is true, the P can only occur in the map once.
     * If foodPresentCheck is true, the F can only occur in the map one or more.
     * Else, the number of times that each valid character occurs in the map is also randomly chosen.
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

        /* Name the file and find the path */
        @SuppressWarnings("DuplicatedCode") String fileName = generateRandomMapFileName(".txt");
        String filePath = FileHandler.actualMapsDirectoryPath + '\\' + fileName;

        /* Write the random content to the file */
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
}
