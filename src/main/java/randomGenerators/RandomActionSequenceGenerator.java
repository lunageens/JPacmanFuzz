package randomGenerators;

import dataProviders.ConfigFileReader;
import managers.FileReaderManager;

import java.util.Random;

/**
 * The RandomActionSequenceGenerator class is responsible for generating random action sequences
 * based on predefined actions. It utilizes a pseudo-random number generator to select actions
 * from a set of available options and constructs a sequence string.
 * <p>
 * The class provides the following functionality:
 * Reading the maximum action sequence length from a configuration file.
 * Generating a random action sequence string based on predefined actions.
 * <p>
 * Usage:
 * Create an instance of RandomActionSequenceGenerator.
 * Call the generateRandomActionSequence() method to generate a random action sequence.
 * <p>
 * Note:
 * The maximum length of the action sequence is determined by the configuration file.
 * The class uses a pseudo-random number generator to select actions from the available options.
 * <p>
 * Example:
 * RandomActionSequenceGenerator generator = new RandomActionSequenceGenerator();
 * String actionSequence = generator.generateRandomActionSequence();
 * <p>
 * This class is part of the randomGenerators package, which provides utilities for generating random data
 * or sequences in a controlled manner.
 */
public class RandomActionSequenceGenerator {

    // TODO Variate in length
    //TODO Special characters

    /**
     * The ConfigFileReader instance used for reading configuration properties.
     */
    private static ConfigFileReader configFileReader = FileReaderManager.getInstance().getConfigReader();

    /**
     * Specifies the maximum length of an action sequence string
     */
    private int maxLength;

    /**
     * Constructs a RandomActionSequenceGenerator object.
     * It initializes the maximum length of the action sequence from the configuration file.
     */
    public RandomActionSequenceGenerator() {
        this.maxLength = configFileReader.getMaxActionSequenceLength();
    }

    /**
     * Generates a random action sequence string based on predefined actions.
     * @return The generated random action sequence string.
     */
    public String generateRandomActionSequence() {
        StringBuilder actionSequenceBuilder = new StringBuilder();
        Random random = new Random();

        // Generate a random action sequence
        for (int i = 0; i < 5; i++) {
            int rand = random.nextInt(8);
            switch (rand) {
                case 0:
                    actionSequenceBuilder.append('E'); // Exit
                    break;
                case 1:
                    actionSequenceBuilder.append('Q'); // Quit
                    break;
                case 2:
                    actionSequenceBuilder.append('S'); // Start
                    break;
                case 3:
                    actionSequenceBuilder.append('W'); // Sleep
                    break;
                case 4:
                    actionSequenceBuilder.append('U'); // Up
                    break;
                case 5:
                    actionSequenceBuilder.append('L'); // Left
                    break;
                case 6:
                    actionSequenceBuilder.append('D'); // Down
                    break;
                case 7:
                    actionSequenceBuilder.append('R'); // Right
                    break;
            }
        }
        String actionSequence = actionSequenceBuilder.toString();
        return actionSequence;
    }

    public String generateRandomActionSequenceValidCharRandomLength(int maxLength){
        StringBuilder actionSequenceBuilder = new StringBuilder();
        Random random = new Random();

        // Generate a random action sequence
        int length = random.nextInt(FileReaderManager.getInstance().getConfigReader().getMaxActionSequenceLength() + 1);
        for (int i = 0; i < length; i++) {
            int rand = random.nextInt(8);
            switch (rand) {
                case 0:
                    actionSequenceBuilder.append('E'); // Exit
                    break;
                case 1:
                    actionSequenceBuilder.append('Q'); // Quit
                    break;
                case 2:
                    actionSequenceBuilder.append('S'); // Start
                    break;
                case 3:
                    actionSequenceBuilder.append('W'); // Sleep
                    break;
                case 4:
                    actionSequenceBuilder.append('U'); // Up
                    break;
                case 5:
                    actionSequenceBuilder.append('L'); // Left
                    break;
                case 6:
                    actionSequenceBuilder.append('D'); // Down
                    break;
                case 7:
                    actionSequenceBuilder.append('R'); // Right
                    break;
            }
        }
        String actionSequence = actionSequenceBuilder.toString();
        return actionSequence;
    }
}
