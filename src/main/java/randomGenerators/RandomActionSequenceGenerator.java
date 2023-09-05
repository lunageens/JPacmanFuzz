package randomGenerators;

import dataProviders.ConfigFileReader;
import managers.FileReaderManager;

import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * Specifies the list of valid actions in the action sequence string
     */
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
     *
     * @param actionSequenceBuilder
     *         The action sequence string builder.
     */
    private static void addRandomValidAction(StringBuilder actionSequenceBuilder) {
        Random rand = new Random();
        int rand_randomInt = rand.nextInt(8);
        actionSequenceBuilder.append(validChar.get(rand_randomInt));
    }

    /**
     * Generates a random action sequence string based on predefined actions.
     * The length of the action sequence is randomly, but the maximum is determined by the maxLength parameter.
     *
     * @return The generated random action sequence string.
     */
    public static String generateRandomActionSequenceValidCharRandomLength() {
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
     * The problem with the generateAllPossibleCombinations() method is that when the length is a larger number,
     * the total of possible combinations grows exponentially and the program soon runs out of java heap space.
     * If u do not want all possible combinations, but just a few random ones, this method can help avoiding this problem.
     * It generates a random possible combination.
     *
     * @param length
     *         The length of the action sequence.
     * @param atLeastOneExit
     *         If true, the string will contain at least one exit action.
     * @param startWithExitCheck
     *         If true, the following will always be true for the result: If an S is present,
     *         one of the following characters in the string (that are after an S) will be an E.
     *
     * @return A random possible combination as String.
     */
    public static String generateRandomCombination(int length, boolean atLeastOneExit, boolean startWithExitCheck) {
        // Shuffle the characters in a random order first
        List<Character> shuffledChar = new ArrayList<>(validChar);
        Collections.shuffle(shuffledChar);
        // Add a random character of this shuffled list to the String until the maximum length is reached
        StringBuilder randomCombination = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char randomChar = shuffledChar.get(random.nextInt(shuffledChar.size()));
            randomCombination.append(randomChar);
        }
        String currentString = randomCombination.toString();
        // Do the checks if needed. If they fail the checks, start over.
        if (!atLeastOneExit || (currentString.contains("E"))) {
            if (!startWithExitCheck || checkStartWithExit(currentString)) {
                return currentString;
            }
        }
        return generateRandomCombination(length, atLeastOneExit, startWithExitCheck);
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
        int indexS = -1; // assume there is no s present
        // find next S in the sting. As long as there is one, keep checking.
        // The next S is found by taking the previous index, and searching from the character after that.
        while ((indexS = currentString.indexOf("S", indexS + 1)) != -1) {// if one or more s is present, check if there is an e after it. If there is, return false. Otherwise, return true.
            int indexE = currentString.indexOf("E", indexS + 1); // look for and E from the character after that S.
            if (indexE == -1) { // this current s does not have an e after it. no need to check other s.
                return false;
            }
        }
        return true; // no s present, no problem
    }

    /**
     * This method mutates the given action sequence, by replacing each character of the string in turn, with
     * each of the valid characters. It will return a list of all possible mutated action sequences.
     *
     * @param actionSequence
     *         The action sequence to mutate.
     *
     * @return A list of all possible mutated action sequences.
     */
    public static List<String> mutateActionSequence(String actionSequence) {
        List<String> mutatedActionSequences = new ArrayList<>();
        // niet vergeten eerst originele action sequence te doen!
        // do not check original sequence, since they will prob crash as well
        mutatedActionSequences.add(actionSequence);

        // make new action sequences
        // check every one of them
        for (int charIndex = 0; charIndex < actionSequence.length(); charIndex++) {
            char initialChar = actionSequence.charAt(charIndex);
            for (char newChar : validChar) {
                if (newChar != initialChar) {
                    // * Get new action sequence
                    // copy old action sequence and replace the char at the given index
                    StringBuilder builder = new StringBuilder(actionSequence);
                    builder.setCharAt(charIndex, newChar);
                    String alteredActionSequence = builder.toString();
                    // * Check new action sequence. If check is passed, write away
                    if (checkActionSequence(alteredActionSequence, true, true)) {
                        mutatedActionSequences.add(alteredActionSequence);
                    }
                }
            }
        }

        return mutatedActionSequences;
    }

    /**
     * This method checks if the given action sequence is valid.
     *
     * @param alteredActionSequence
     *         The action sequence to check.
     * @param atLeastOneExitCheck
     *         True if the action sequence should contain at least one exit.
     * @param startWithExitOrQuitCheck
     *         True if the action sequence needs to pass following check: if it has one or multiple 'S', there should
     *         not be one or more 'S' without at least one 'Q' or 'E' later on in the string.
     *
     * @return True if the action sequence is valid, for the performed checks.
     */
    private static boolean checkActionSequence(String alteredActionSequence, boolean atLeastOneExitCheck, boolean startWithExitOrQuitCheck) {
        // check if it contains at least one exit
        boolean atLeastOneExit = true;
        if (atLeastOneExitCheck) { // if we have to check
            if (!alteredActionSequence.contains("E")) { // does not contain one
                atLeastOneExit = false;
            }
        }

        // check pairs
        boolean startWithExitOrQuit = true;
        if (startWithExitOrQuitCheck) { // if we have to check
            int indexS = -1; // assume there is no s present, then stays true
            while ((indexS = alteredActionSequence.indexOf("S", indexS + 1)) != -1) {// if one or more s is present, check if there is an e after it. If there is, return false. Otherwise, return true.
                int indexE = alteredActionSequence.indexOf("E", indexS + 1); // look for and E from the character after that S.
                int indexQ = alteredActionSequence.indexOf("Q", indexS + 1); // look for an Q from the character after that S.
                if ((indexQ == -1) && (indexE == -1)) { // this current s does not have an e or q after it. no need to check other s.
                    startWithExitOrQuit = false;
                }
            }
        }

        return atLeastOneExit && startWithExitOrQuit;
    }

}
