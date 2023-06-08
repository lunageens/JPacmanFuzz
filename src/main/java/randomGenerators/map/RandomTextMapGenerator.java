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
     * Constructs a RandomTextMapGenerator with the specified maximum height and width.
     *
     * @param maxHeight The maximum height of the generated text-based maps.
     * @param maxWidth  The maximum width of the generated text-based maps.
     */
    public RandomTextMapGenerator(int maxHeight, int maxWidth) {
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
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

}
