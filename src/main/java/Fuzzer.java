import dataProviders.ConfigFileReader;
import managers.FileReaderManager;
import organizers.DirectoryHandler;
import organizers.FileHandler;
import outputProviders.IterationResult;
import outputProviders.LogFileHandler;
import randomGenerators.RandomActionSequenceGenerator;
import randomGenerators.map.MapGenerator;
import randomGenerators.map.RandomBinaryMapGenerator;
import randomGenerators.map.RandomMapGenerator;
import randomGenerators.map.RandomTextMapGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.IntStream;

/**
 * ! Note:
 * If u want to run the jpacman application for a particular map and
 * action sequence, without reporting, follow these steps:
 * <p>
 * 1) save the map under C:/ST/JPacmanFuzz/NameOfTheMap.txt
 * 2) open the terminal and navigate to this project
 * 3) run the following command:
 * java -jar jpacman-3.0.1.jar NameOfTheMap.txt SSE
 * SSE is an example. Any other action sequence can be used
 * 4) Commands to retrieve the exit code depends on which terminal and which
 * operating sequence you are using.
 * Printing exit code of last process ran in Windows powershell:
 * echo $LastExitCode
 */

/**
 * The Fuzzer class is the main class that runs the fuzzing process.
 * It generates random action sequences and maps, executes a game simulation, and collects and analyzes the results.
 */
public class Fuzzer {

    /**
     * Instance of ConfigFileReader used to read configurations in the properties file.
     */
    private static final ConfigFileReader configFileReader = FileReaderManager.getInstance().getConfigReader();

    /**
     * Number of iterations the fuzz should do (how many times to run the program with another unique random map file
     * and action sequence). Specified in configurations file.
     */
    private static int MAX_ITERATIONS = configFileReader.getMaxIterations();

    /**
     * If the program reaches this amount of time before doing all the specified iterations, we should also
     * stop fuzzing. Specified in configurations file.
     */
    private static final long TIME_BUDGET_MS = configFileReader.getMaxTime();

    /**
     * The copies of the original maps and the mutated versions in one list. Only used in mutational testing.
     */
    private static List<String> combinedMaps;

    /**
     * The copies of the original action sequence and the mutated version in one list. Only used in mutational testing.
     */
    private static List<String> combinedSequences;

    /**
     * The main entry point of the fuzzing process.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        /* * In case of random maps or sequences, we need a new file and sequence generator */
        RandomActionSequenceGenerator randomActionSequenceGenerator = new RandomActionSequenceGenerator();
        MapGenerator mapGenerator = new RandomMapGenerator();

        /* * New file handlers for the maps and logs, and then organizing result directory */
         // Create the needed directories and clean them up if that is needed
        FileHandler fileHandler = new FileHandler();
        fileHandler.initializeDirectories(); // Reads count as well
        LogFileHandler logFileHandler = new LogFileHandler();

        /* * Initialization result variables */
        // Store results of the process ran
        List<IterationResult> iterationResults = new ArrayList<>();
        Map<Integer, List<IterationResult>> iterationResultsByErrorCode = new HashMap<>();
        Map<String, List<IterationResult>> iterationResultsByOutputMessage = new HashMap<>();

        /* * Get the maps, action sequences and output messages first from the custom and then if additional random is needed */
        // In case of custom maps or sequence, Add your custom map file paths to this list
        // If 0 or not implemented, nothing is added.
        // Do this last -> otherwise not correct directories and handlers
        List<String> customMaps = getCustomMaps(configFileReader.getCustomMapsNr());
        List<String> customMapsAttributes = getCustomAttributesLog(configFileReader.getCustomMapsNr());
        List<String> customSequences = getCustomSequences(configFileReader.getCustomSequenceNr());
        // If one want to mutate maps or action sequences, use the original other input. See combine method for more information.
        if (configFileReader.getCombinedCustomMapsAndSequences()) {
            if (configFileReader.getCustomMapsNr() == 8 && configFileReader.getCustomSequenceNr() == 10) {
                combineMapsAndSequences(customMaps, customSequences);
                customMaps = combinedMaps;
                customSequences = combinedSequences;
            } else {
                System.out.println("One cannot combine unmutated maps and action sequences in a meaningful way. " +
                        "Please put customMapsNr = 8 and customSequenceNr = 10.");
            }
        }
        if (configFileReader.getMaxCustomIterations()) { // Added this. In case max iterations = number of custom maps and sequences
            MAX_ITERATIONS = Math.max(customMaps.size(), customSequences.size());
        }

        /* ! For each iteration with max_iterations */
        for (int i = 0; i < MAX_ITERATIONS; i++) {    // How many times does a random file and sequence has to be created?
            // * Use custom sequences and maps if asked. Otherwise, generate randomly.
            String mapFilePath;
            String actionSequence;
            if (customMaps.isEmpty()) {
                mapFilePath = mapGenerator.generateRandomMap();
            } // If no more, generate randomly with configs file type
            else {
                mapFilePath = customMaps.remove(0);
            } // Use custom map file
            if (!customSequences.isEmpty()) {
                actionSequence = customSequences.remove(0);
            } else {
                actionSequence = randomActionSequenceGenerator.generateRandomActionSequence();
            }

            // * Check combo map and actions if needed
            boolean isValidMove = true;
            if (IntStream.of(7, 8, 9, 10).anyMatch(j -> configFileReader.getCustomSequenceNr() == j)) { // Checks for out of bounds and monster
                isValidMove = IterationResult.isValidMove(mapFilePath, actionSequence);
            }

            /* * Try to execute pacman and retrieve exitcode and other results. Alter count of the correct exitcode.*/
            try {
                // Execute process fully or wait until timeout reached.
                Process process = executeJPacman(mapFilePath, actionSequence);
                ProcessTimeoutHandler timeoutHandler = new ProcessTimeoutHandler(process);
                timeoutHandler.start();
                // Retrieve output data process
                int exitCode = process.waitFor(); // Wait for the process to complete or timeout
                timeoutHandler.interrupt(); // Interrupt the timeout handler thread if it's still running (shorter execution)
                String outputMessages = readOutputMessages(process);
                if (timeoutHandler.isTimeoutReached()) { // Check if the timeout handler thread triggered the timeout (longer execution)
                    exitCode = -1;
                    outputMessages = "Time Limit of Iteration reached";
                }
                if (!isValidMove) {
                    exitCode = -1;
                    outputMessages = "Invalid Move";
                }
                String customAttribute = "";
                if (!customMapsAttributes.isEmpty()) {
                    customAttribute = customMapsAttributes.remove(0);
                }
                // Store output data process in iteration results list
                IterationResult iterationResult = new IterationResult(i + 1, mapFilePath, actionSequence,
                        exitCode, outputMessages, customAttribute);
                iterationResults.add(iterationResult);
                // Update iteration results by error code
                List<IterationResult> errorCodeResults = iterationResultsByErrorCode.getOrDefault(iterationResult.getErrorCode(), new ArrayList<>());
                errorCodeResults.add(iterationResult);
                iterationResultsByErrorCode.put(iterationResult.getErrorCode(), errorCodeResults);
                // Update iteration results by message output
                List<IterationResult> outputMessageResults = iterationResultsByOutputMessage.getOrDefault(iterationResult.getOutputMessages(), new ArrayList<>());
                outputMessageResults.add(iterationResult);
                iterationResultsByOutputMessage.put(iterationResult.getOutputMessages(), outputMessageResults);
                // Move map to correct permanent directory if needed.
                // Do not use get path method cuz already changed
                DirectoryHandler.moveMapFileToErrorDirectory(mapFilePath, iterationResult.getErrorCode());
            } catch (IOException | InterruptedException e) {
                System.out.println("Exception during process building.");
                e.printStackTrace();
            }

            /* ! Check if the total time budget has been exhausted */
            long endTime = System.currentTimeMillis();
            elapsedTime = endTime - startTime;
            if (elapsedTime >= TIME_BUDGET_MS) {
                System.out.println("Time limit reached.");
                break;
            }
        }

        /* * Generate logs and clean up directories if needed*/
        logFileHandler.generateActualLogs(iterationResults, iterationResultsByErrorCode, iterationResultsByOutputMessage, elapsedTime);
        if (FileHandler.cleanDirectories) {
            DirectoryHandler.cleanDirectory(FileHandler.previousLogsDirectoryPath);
            DirectoryHandler.cleanDirectory(FileHandler.previousMapsDirectoryPath);
        }
        if (FileHandler.logHistory) {
            logFileHandler.generateOverviewLogs(iterationResults, iterationResultsByErrorCode, iterationResultsByOutputMessage, elapsedTime);
        }
    }

    /**
     * Executes JPacman with the given map file and action sequence.
     *
     * @param mapFilePath    The file path of the map file.
     * @param actionSequence The random action sequence.
     * @return The process of the JPacman execution.
     * @throws IOException If an I/O error occurs.
     */
    private static Process executeJPacman(String mapFilePath, String actionSequence) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "jpacman-3.0.1.jar", mapFilePath, actionSequence);
        return processBuilder.start();
    }

    /**
     * Reads and returns the output messages from the process.
     *
     * @param process The process to read the output messages from.
     * @return The output messages as a string.
     * @throws IOException If an I/O error occurs.
     */
    private static String readOutputMessages(Process process) throws IOException {
        // Read and return the output messages from the process
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.replace("**** ", "");
            output.append(line).append("\n");
        }
        return output.toString();
    }

    /**
     * Based on the custom number in the configuration, get the filepath of the custom maps here.
     * <ul>
     *     <li>Case 1: Write a text map with one line. The line of the maps are specified in the custom attributes with the same number.</li>
     *     <li>Case 2: Write a binary map with one line. The line of the maps are specified in the custom attributes with the same number
     *     as the map file text and are encoded.</li>
     *     <li>Case 3: Try out different file types. The files can be found in the fuzz3_filetypes/custom_maps_inputCopy directory.</li>
     *     <li>Case 4: Write text files with only valid characters in it (P, M, 0, W, F). The files can be of all forms and the valid
     *     characters can occur 0, 1 or more in the file.</li>
     *     <li>Case 5: Write text files with only valid characters in it (P, M, O, W, F). The map is squared.</li>
     *     <li>Case 6: Read text files with only valid characters in it (P, M, O, W, F). These are corner cases for file content and form.
     *     The files can be found in fuzz4_validCharacterMaps/custom_maps.</li>
     *     <li>Case 7: Write text files with only valid characters in it (P, M, O, W, F). The map is squared.</li>
     *     The map holds exactly one player. The map holds at least one food. The map does not contain one or more empty lines.
     *     These maps should all be accepted.</li>
     *     <li>Case 8: Takes the first map that is stored in the directory fuzzresults_lessons/fuzz7_mutationalFuzzing/custom_maps_inputCopy.
     *     Will mutate each character of this map with each valid character of the pacman game.</li>
     * </ul>
     *
     * @param customNr
     *         The number specified in the configuration file.
     *
     * @return List of Strings that are the file paths of the custom maps.
     */
    private static List<String> getCustomMaps(int customNr) {
        List<String> customMaps = new ArrayList<>();
        switch (customNr) {
            case 1 -> { // Write text file with some specified one line of the map.
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight() + 1,
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth() + 1);
                List<String> customMapsAttributes = getCustomAttributesLog(customNr);
                for (String mapLine : customMapsAttributes) {
                    customMaps.add(randomTextMapGenerator.generateCustomTextMapOneLine(mapLine));
                }
            }
            case 2 -> { // Try to get valid binary file with encoding binary files.
                RandomBinaryMapGenerator randomBinaryMapGenerator = new RandomBinaryMapGenerator(
                        FileReaderManager.getInstance().getConfigReader().getMaxBinaryMapSize());
                List<String> customMapsAttributes = getCustomAttributesLog(customNr);
                for (String encodedFile : customMapsAttributes) {
                    customMaps.add(randomBinaryMapGenerator.generateCustomEncodedMap(encodedFile));
                }
            }
            case 3 -> { // Try different (in)valid file type from custom_maps directory of fuzz 3 (.../fuzzresults_lessons/fuzz3_filetypes/custom_maps_inputCopy_3).
                DirectoryHandler searcher = new DirectoryHandler();
                List<String> filePaths = searcher.getFilesInDirectory("custom_maps_inputCopy_3");
                customMaps.addAll(filePaths);
            }
            case 4 -> { //Try valid character maps, all forms, as many as specified max in configs
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight() + 1,
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth() + 1);
                int i;
                for (i = 0; i < MAX_ITERATIONS; i++) {
                    customMaps.add(randomTextMapGenerator.generateRandomValidCharRandomSizeTextMap());
                }
            }
            case 5 -> { // Try valid character maps, only squared, as many as specified max in configs
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight() + 1,
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth() + 1);
                int i;
                for (i = 0; i < MAX_ITERATIONS; i++) {
                    customMaps.add(
                            randomTextMapGenerator.generateRandomValidCharRectangularTextMap(false, false, true));
                }
            }
            case 6 -> { // Try valid character maps of all forms and content, using the corner cases map files of custom_maps directory of fuzz 4 (.../fuzzresults_lessons/fuzz4_validCharacterMaps/custom_maps_inputCopy_4).
                DirectoryHandler searcher = new DirectoryHandler();
                List<String> filePaths = searcher.getFilesInDirectory("custom_maps_inputCopy_4");
                customMaps.addAll(filePaths);
            }
            case 7 -> { // Try valid character maps, only squared, as many as specified max in configs, and checked for player food and size
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight() + 1,
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth() + 1);
                int i;
                for (i = 0; i < MAX_ITERATIONS; i++) {
                    customMaps.add(
                            randomTextMapGenerator.generateRandomValidCharRectangularTextMap(true, true, false));
                }
            }
            case 8 -> { // Mutate one original map that is stored in directory, write all mutated versions away and
                // store their filepaths.
                DirectoryHandler searcher = new DirectoryHandler();
                List<String> filePaths = searcher.getFilesInDirectory("custom_maps_inputCopy_7");
                if (filePaths.size() > 1) {
                    System.out.println("One can only mutate one map at a time. The first map is used.");
                }
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight(),
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth()
                );
                String firstFilePath = FileHandler.normalizeFilePath(filePaths.get(0), true, true);
                List<String> mutatedMapFilePaths = randomTextMapGenerator.mutateMap(firstFilePath);
                customMaps.addAll(mutatedMapFilePaths);
            }
            default -> { // Nothing to add
            }
        }
        return customMaps;
    }

    /**
     * Based on the configurations, give some information about the maps into the reports.
     * <ul>
     *     <li>Case 1: The ASCII character used to generate the one-character text file.</li>
     *     <li>Case 2: The encoded binary file (not used currently).</li>
     *     <li>Case 3: The description of the invalid file type.</li>
     *     <li>Case 4: Some one-line maps with corner cases of the valid characters and their occurrence in the  map.</li>
     *</ul>
     * @param customMapsAttributeNr The configuration that u want. Loosely based on customMapsNr in configuration, but not used everytime.
     * @return List of strings that are the information attached to the custom map.
     */
    private static List<String> getCustomAttributesLog(int customMapsAttributeNr){
        List<String> customMapsAttributes = new ArrayList<>();
        switch(customMapsAttributeNr) {
            case 1: { // Try to get valid ASCII characters
                for (int i = 0; i < 128; i++) {  // Add all letters alphabet, numbers, other characters?
                    if (i == 34){
                        String asciiCharacter = "\""; // ASCII Character " is programmed with \"
                        customMapsAttributes.add(asciiCharacter);
                    } else if (i == 39) {
                        String asciiCharacter = "'"; // ASCII Character ' is programmed with "\'";
                        customMapsAttributes.add(asciiCharacter);
                    } else if (i == 92) {
                        String asciiCharacter = "\\"; // ASCII Character \ is programmed with "\\";
                        customMapsAttributes.add(asciiCharacter);
                    } else{
                        char c = (char) i;
                        customMapsAttributes.add(String.valueOf(c));
                    }
                }
                break;
            } case 2: { //Try to get valid binary file with encoding binary files. Save Encoded string.
                customMapsAttributes.add("000WEOPM0");
                customMapsAttributes.add("000\nWE0\nPMO");
                customMapsAttributes.add("P");
                customMapsAttributes.add("OP");
                customMapsAttributes.add("0PM");
                customMapsAttributes.add("OPMW");
            } case 3: { // Try different (in)valid file types, see information in name.
                DirectoryHandler searcher = new DirectoryHandler();
                List<String> filePaths = searcher.getFilesInDirectory("custom_maps_inputCopy");
               for (String filePath : filePaths){
                      File file = new File(filePath);
                   String fileName = file.getName();
                   customMapsAttributes.add(fileName);
               }
                break;
            }
            case 4: { // Try to see who is more important - player, monster, food, ...
                customMapsAttributes.add("MW");
                customMapsAttributes.add("MWP");
                customMapsAttributes.add("OPF");
                customMapsAttributes.add("OPE");
            }
            default: { // Nothing to add
                break;
            }
        }
        return customMapsAttributes;
    }

    /**
     * Based on the configuration, get some custom action sequences.
     * <ul>
     *     <li>Case 1: For all files made, add a valid custom action sequence (SWE Start - Wait - Exit)</li>
     *     <li>Case 2: For all files made, add a random valid custom action sequence (with a valid length)</li>
     *     <li>Case 3: For the max length specified in the configuration file, add all possible combinations of that length
     *     that can be made with the valid action sequence characters (S, E, W, Q, U, D, L, R).</li>
     *     <li>Case 4: For the max length specified in the configuration file, add all possible combinations of that length
     *     that can be made with the valid action sequence characters (S, E, W, Q, U, D, L, R). However, only possible
     *     combinations that have at least one time the character 'E' in it will be tested. </li>
     *     <li>Case 5: For the max length specified in the configuration file, add all possible combinations of that length
     *     that can be made with the valid action sequence characters (S, E, W, Q, U, D, L, R). However, only possible
     *     combinations that have at least one time the character 'E' in it will be tested. Also, if the character 'S'
     *     is present, the character 'E' will be on one of the string characters after the 'S'.</li>
     *     <li>Case 6: For the max length specified in the configuration file, add all possible combinations of that length
     *     that can be made with the valid action sequence characters (S, E, W, Q, U, D, L, R). However, only possible
     *     combinations that have at least one time the character 'E' in it will be tested. Also, if the character 'S'
     *     is present, the character 'E' will be on one of the string characters after the 'S'. On top of that, each string
     *     starts with an 'S' and ends with an 'E'.</li>
     *     <li>Case 7: For the max length specified in the configuration file, add all possible combinations of that length
     *     that can be made with the valid action sequence characters (S, E, W, Q, U, D, L, R). However, only possible
     *     combinations that have at least one time the character 'E' in it will be tested. Also, if the character 'S'
     *     is present, the character 'E' will be on one of the string characters after the 'S'. On top of that, each string
     *     starts with an 'S' and ends with an 'E'. Actions (up, left, down, right) with that 'S' and 'E' will be checked:
     *     the player will not move out of the bounds of the map and will also not move to a wall cell. If that is the case,
     *     the iteration result will get the exit code -1 and an indicating output message. </li>
     *     <li>Case 8: For the max length specified in the configuration file, add all possible combinations of that length
     *     that can be made with the valid action sequence characters (S, E, W, Q, U, D, L, R). However, only possible
     *     combinations that have at least one time the character 'E' in it will be tested. Also, if the character 'S'
     *     is present, the character 'E' will be on one of the string characters after the 'S'. Actions (up, left, down, right) with that 'S' and 'E' will be checked:
     *     the player will not move out of the bounds of the map and will also not move to a wall cell. If that is the case,
     *     the iteration result will get the exit code -1 and an indicating output message. The first ?MAX_ITERATIONS items of the list of possible combinations
     *     will be executed. </li>
     *     <li>Case 9: For the max length specified in the configuration file, add possible combinations of that length
     *     that can be made with the valid action sequence characters (S, E, W, Q, U, D, L, R). However, only possible
     *     combinations that have at least one time the character 'E' in it will be tested. Also, if the character 'S'
     *     is present, the character 'E' will be on one of the string characters after the 'S'.
     *     Actions (up, left, down, right) with that 'S' and 'E' will be checked:
     *     the player will not move out of the bounds of the map and will also not move to a wall cell. If that is the case,
     *     the iteration result will get the exit code -1 and an indicating output message.
     *     For each execution (specified in ?MAX_ITERATIONS variable),
     *     an item of the list of all possible combinations will be picked randomly. </li>
     *     <li>Case 10: Takes the first line of the text file stored in the location fuzzresults_lessons/fuzz7_mutationalFuzzing/
     *     custom_actionSequences_inputCopy_7. Will consider this first line as an action sequence. Will replace each
     *     character of this string with each other valid action sequence character (S, E, W, Q, U, D, L, R) in turn.  </li>
     *     <li>Default: Nothing to add</li>
     * </ul>
     *
     * @param customNr
     *         The number specified in the configuration file.
     *
     * @return List of strings that are the custom action sequences.
     */
    private static List<String> getCustomSequences(int customNr){
        List<String> customSequences = new ArrayList<>();
        switch (customNr) {
            case 1: { //A ... times correct string that starts, wait, exit.
                int i;
                for (i = 0; i < MAX_ITERATIONS; i++) {
                    customSequences.add("SWE");
                }
            }
            case 2: {
                int i;
                for (i = 0; i < MAX_ITERATIONS; i++) {
                    customSequences.add(RandomActionSequenceGenerator.generateRandomActionSequenceValidCharRandomLength());
                }
            }
            case 3: {
                customSequences = RandomActionSequenceGenerator.generateAllPossibleCombinations(
                        FileReaderManager.getInstance().getConfigReader().getMaxActionSequenceLength(),
                        false, false);
            }
            case 4: {
                customSequences = RandomActionSequenceGenerator.generateAllPossibleCombinations(
                        FileReaderManager.getInstance().getConfigReader().getMaxActionSequenceLength(),
                        true, false);
            }
            case 5, 8: {
                customSequences = RandomActionSequenceGenerator.generateAllPossibleCombinations(
                        FileReaderManager.getInstance().getConfigReader().getMaxActionSequenceLength(),
                        true, true);
            }
            case 6, 7: {
                List<String> actionSequences = RandomActionSequenceGenerator.generateAllPossibleCombinations(
                        FileReaderManager.getInstance().getConfigReader().getMaxActionSequenceLength() - 2,
                        false, true);
                for (String actionSequence : actionSequences) {
                    String customSequence = "S" + actionSequence + "E";
                    customSequences.add(customSequence);
                }
            }
            case 9: {
                while (customSequences.size() < MAX_ITERATIONS) {
                    String randomCombination = RandomActionSequenceGenerator.generateRandomCombination(
                            FileReaderManager.getInstance().getConfigReader().getMaxActionSequenceLength(),
                            true, true);
                    customSequences.add(randomCombination);
                }
            }
            case 10: {
                //! Hardcoded filepath here
                String filePath = "fuzzresults_lessons/fuzz7_mutationalFuzzing/custom_actionSequences_inputCopy_7";
                String fileText = FileHandler.getFileText(filePath);
                String[] lines = fileText.split("/n");
                String actionSequence;
                if (lines == null) {
                    System.out.println("The file was empty or there was an error while reading the file.");
                    actionSequence = "";
                } else {
                    if (lines.length > 1) {
                        System.out.println("There were multiple lines in the action sequence file. The first one was used.");
                    }
                    actionSequence = lines[0];
                }
                List<String> mutatedActionSequences = RandomActionSequenceGenerator.mutateActionSequence(actionSequence);
                customSequences.addAll(mutatedActionSequences);
            }
            default: { // Nothing to add
                break;
            }
        }
        return customSequences;
    }

    /**
     * Firstly, the original map and action sequences gets paired.
     * All mutated versions of the map first gets paired with the original action sequence.
     * Then, all mutated versions of the action sequences gets paired with the original map.
     * The combinedMaps and combinedSequences are a list that contains all copies of the original and the mutated
     * versions in that particular order.
     *
     * @param customMaps
     *         List of the original map and all its mutated versions.
     * @param customSequences
     *         List of the original customSequences and all its mutated versions.
     */
    private static void combineMapsAndSequences(List<String> customMaps, List<String> customSequences) {
        combinedMaps = new ArrayList<>();
        combinedSequences = new ArrayList<>();
        String originalMap = customMaps.get(0);
        String originalSequence = customSequences.get(0);
        // for every mutated map, copy the original action sequence
        for (String map : customMaps) {
            combinedMaps.add(map); // first one will be the original map and original sequence
            combinedSequences.add(originalSequence);
        }

        // we cannot just add original map for each action sequence -> we will not be able to move to correct directory, ...
        // thus, make copy of original map
        String mapContent = FileHandler.getFileText(originalMap);
        List<String> lines = Arrays.asList(mapContent.split("\\n"));
        for (String line : lines) {  // remove new lines characters for correct measurement
            String newLine = line.replaceAll("\\r|\\n", "");
            lines.set(lines.indexOf(line), newLine);
        }

        // for every mutated action sequence, copy the original map
        for (String sequence : customSequences) {
            if (sequence != originalSequence) {// if the original and the mutated action sequence are the same, we already added this to the list.
                // make copy of original map
                String filePath = RandomTextMapGenerator.writeMapAway(lines, MapGenerator.generateRandomMapCopyFileName(".txt"));
                combinedMaps.add(filePath);
                combinedSequences.add(sequence);
            }
        }
    }

}





