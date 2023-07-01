package randomGenerators;

import dataProviders.ConfigFileReader;
import managers.FileReaderManager;

import java.util.ArrayList;
import java.util.List;
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

    private static List<Character> validChar;
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
        validChar = new ArrayList<>();
        validChar.add('E'); // Exit
        validChar.add('Q'); // Quit
        validChar.add('S'); // Start
        validChar.add('W'); // Sleep
        validChar.add('U'); // Up
        validChar.add('L'); // Left
        validChar.add('D'); // Down
        validChar.add('R'); // Right
    }

    /**
     * Add random valid character to the action sequence string builder.
     * @param actionSequenceBuilder The action sequence string builder.
     */
    private void addRandomValidAction(StringBuilder actionSequenceBuilder) {
        Random rand = new Random();
        int rand_randomInt =  rand.nextInt(8);
        actionSequenceBuilder.append(validChar.get(rand_randomInt));
    }

    /**
     * Generates a random action sequence string based on predefined actions.
     * The length of the action sequence is randomly, but the maximum is determined by the maxLength parameter.
     *
     * @return The generated random action sequence string.
     */
    public String generateRandomActionSequenceValidCharRandomLength() {
        StringBuilder actionSequenceBuilder = new StringBuilder();
        Random random = new Random();
        // Generate a random length for the action sequence
        int length = random.nextInt(FileReaderManager.getInstance().getConfigReader().getMaxActionSequenceLength() + 1);
        for (int i = 0; i < length; i++) {
            addRandomValidAction(actionSequenceBuilder);
        }
        return actionSequenceBuilder.toString();
    }

    /**
     * Generates a random action sequence string based on predefined actions,
     * with the length of the action sequence is determined by the configuration file.
     *
     * @return The generated random action sequence string.
     */
    public String generateRandomActionSequence() {
        StringBuilder actionSequenceBuilder = new StringBuilder();
        // Generate a random action sequence
        for (int i = 0; i < maxLength + 1; i++) {
            addRandomValidAction(actionSequenceBuilder);
        }
        return actionSequenceBuilder.toString();
    }

    /**
     * Generates a list of all possible combination of the given characters with the inputted length.
     *
     * @param length
     *         The length of the action sequence.
     * @param atLeastOneExit
     *         If true, the list will contain at least one exit action.
     * @param startWithExitCheck
     *         If true, the list will contain several strings. If an S is present, one of the following characters in
     *         the string (that are after an S) will be an E.
     *
     * @return A list of all possible combination of the given characters with the inputted length.
     */
    public static List<String> generateAllPossibleCombinations(int length, boolean atLeastOneExit, boolean startWithExitCheck) {
        List<String> combinations = new ArrayList<>();
        generateCombinationsHelper("", length, combinations, atLeastOneExit, startWithExitCheck);
        return combinations;
    }

    /**
     * The generateCombinationsHelper method is a recursive helper function that appends each character from the
     * validChar array to the current string and continues the recursion until the desired length is reached.
     *
     * @param currentString
     *         The current string that is being appended to.
     * @param length
     *         The length of the action sequence.
     * @param combinations
     *         The list of all possible combination of the given characters with the inputted length.
     * @param atLeastOneExit
     *         If true, the list will contain at least one exit action.
     */
    private static void generateCombinationsHelper(String currentString, int length, List<String> combinations, boolean atLeastOneExit, boolean startWithExitCheck) {
        if (currentString.length() == length) { // if the current string is long enough
            if (!atLeastOneExit || (currentString.contains("E"))) {
                if (!startWithExitCheck || checkStartWithExit(currentString)) {
                    combinations.add(currentString); // add it to the list of combinations
                }
            }
            return; // and return it
        }
        for (Character character : validChar) { // if it is not long enough, take one of the characters from the validChar array
            generateCombinationsHelper(currentString + character, length, combinations, atLeastOneExit, startWithExitCheck); // and do again
        }
    }

    /**
     * Checks if the following condition is met: if there is an S in the string, an E is followed somewhere after that.
     *
     * @param currentString
     *         The current string to check.
     *
     * @return True if there is no S without an E that follows.
     */
    private static boolean checkStartWithExit(String currentString) {
        int indexS = currentString.indexOf("S");
        if (indexS != -1) { // if s is present, check if there is an e after it. If there is, return false. Otherwise, return true.
            int indexE = currentString.indexOf("E", indexS + 1);
            return indexE != -1;
        }
        return true; // no s present, no problem
    }
}
