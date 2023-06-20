package randomGenerators.map;

import dataProviders.ConfigFileReader;
import enums.MapFileType;
import managers.FileReaderManager;

import java.util.Random;

/**
 * This class generates random maps based on the MapGenerator abstract class.
 * It provides functionality to generate both text-based and binary maps.
 */
public class RandomMapGenerator extends MapGenerator {

    /**
     * The ConfigFileReader instance used for reading configuration properties.
     */
    private ConfigFileReader configFileReader;

    /**
     * The RandomBinaryMapGenerator instance used for generating random binary files.
     */
    private RandomBinaryMapGenerator randomBinaryMapGenerator;

    /**
     * The RandomTextMapGenerator instance used for generating random text files.
     */
    private RandomTextMapGenerator randomTextMapGenerator;

    /**
     * File type used in this run (not for this particular map file) - more only text, only binary, of combination.
     * Specified in configuration settings.
     */
    public static MapFileType fileType = FileReaderManager.getInstance().getConfigReader().getMapFilesType();

    /**
     * Constructs a RandomMapGenerator object.
     * It initializes the necessary dependencies and generators based on the configuration settings.
     */
    public RandomMapGenerator() {
        configFileReader = FileReaderManager.getInstance().getConfigReader();
        this.randomBinaryMapGenerator = new RandomBinaryMapGenerator(configFileReader.getMaxBinaryMapSize());
        this.randomTextMapGenerator = new RandomTextMapGenerator(configFileReader.getMaxTextMapHeight(), configFileReader.getMaxTextMapWidth());
    }

    /**
     * If text only in configs is true, makes new text file.
     * If that is not true, randomly choose to make text or binary file
     * Generates a random map file, either text-based or binary, based on the configuration settings.
     *
     * @return The file path of the generated map file.
     */
    @Override
    public String generateRandomMap() {
        String filePath = null;
        switch (fileType) {
            case TEXT -> filePath = randomTextMapGenerator.generateRandomMap();
            case BINARY -> filePath = randomBinaryMapGenerator.generateRandomMap();
            case ALL -> {
                Random random = new Random();
                boolean isTextType = random.nextBoolean();
                if (isTextType) {
                    filePath = randomTextMapGenerator.generateRandomMap();
                } else {
                    filePath = randomBinaryMapGenerator.generateRandomMap();
                }
            }
        }
        return filePath;
    }
}
