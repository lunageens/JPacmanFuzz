package randomGenerators.map;

import outputProviders.FileHandler;

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
public class RandomTextMapGenerator extends MapGenerator {

    //TODO I think now always identical line width? is get method correct?
    //TODO Special characters

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
                char c = (char) (random.nextInt(26) + 'a'); // TODO Find out what this means
                line.append(c);
            }
            lines.add(line.toString());
        }

        String fileName = generateRandomMapFileName(".txt");
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
     * @param mapLine Only line of the text map u want to create
     * @return String FilePath of the created textfile.
     */
    public String generateCustomTextMap(String mapLine) {
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
                char c = (char) (validChar.get(random.nextInt(4))); // Pick one valid char randomly
                line.append(c);
            }
            lines.add(line.toString());
        }

        // Name the file and find the path.
        String fileName = generateRandomMapFileName(".txt");
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
     * @param justOnePlayerCheck True if the map can only have one P in it
     * @param foodPresentCheck True if the map should have at least one F in it
     * @return Filepath of the generated text file as string.
     */
    public String generateRandomValidCharRectangularTextMap(boolean justOnePlayerCheck, boolean foodPresentCheck){
        List<String> lines = new ArrayList<>();
        Random random = new Random();

        // Pick random size
        int mapHeight = random.nextInt(maxHeight);
        int mapWidth = random.nextInt(maxWidth);

        // Intiate variables that record certain map attributes
        boolean justOnePlayer = false;
        boolean foodPresent = false;
        List<Integer> availablePositions = new ArrayList<>(); // Places that can be replaced by a F or P if needed.

        // For each position in the map, pick random valid char and record changed map attributes.
        for (int row = 0; row < mapHeight; row++) {
            StringBuilder line = new StringBuilder();

            for (int column = 0; column < mapWidth; column++) {
                char c = '-';
                boolean validCharChoosen = false;

                while (!validCharChoosen) {
                    c = (char) (validChar.get(random.nextInt(4))); // Pick one valid char randomly
                    if (justOnePlayerCheck && c == 'P' && !justOnePlayer) { // If we need to check, only append player if there is not one yet. Record that there is a player now.
                        line.append(c);
                        justOnePlayer = true;
                        validCharChoosen = true;
                    } else if (foodPresentCheck && c == 'F') { // If we need to check, record that there is food now.
                        foodPresent = true;
                        validCharChoosen = true;
                    } else if (!justOnePlayerCheck && !foodPresentCheck) { // If we don't need to check, add.
                        line.append(c);
                        validCharChoosen = true;
                    }
                }

                if( (!justOnePlayer && c != 'P') || (!foodPresent && c != 'F')){ // If this character is not just the only player or food
                    availablePositions.add(row * mapWidth + column);
                }
            }
            lines.add(line.toString());
        }

        // Check if 'P' is missing and add it randomly (by replacing another available one) if that's the case
        if (justOnePlayerCheck && !justOnePlayer) {
            if(!availablePositions.isEmpty()){
                int randomPosition = availablePositions.get(random.nextInt(availablePositions.size()));
                int randomRow = randomPosition / mapWidth ;
                StringBuilder randomLine = new StringBuilder(lines.get(randomRow));
                int randomColumn = randomPosition % mapWidth;
                randomLine.setCharAt(randomColumn, 'P');
                lines.set(randomRow, randomLine.toString());
            }
        }

        // Check if 'F' is missing and add it randomly (by replacing another available one) if that's the case
        if (foodPresentCheck && !foodPresent) {
            if(!availablePositions.isEmpty()){
                int randomPosition = availablePositions.get(random.nextInt(availablePositions.size()));
                int randomRow = randomPosition / mapWidth ;
                StringBuilder randomLine = new StringBuilder(lines.get(randomRow));
                int randomColumn = randomPosition % mapWidth;
                randomLine.setCharAt(randomColumn, 'F');
                lines.set(randomRow, randomLine.toString());
            }
        }

        // Name the file and find the path
        String fileName = generateRandomMapFileName(".txt");
        String filePath = FileHandler.actualMapsDirectoryPath + '\\' + fileName;

        // Write the random content to the file
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
