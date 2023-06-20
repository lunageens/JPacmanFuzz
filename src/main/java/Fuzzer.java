import dataProviders.ConfigFileReader;
import organizers.DirectoryHandler;
import managers.FileReaderManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO Add website templates and alter generator classes accordingly.
// TODO Functionality and website documentation fuzz 5 action sequence strings.
// TODO Functionality and website documentation fuzz 6 mutation fuzzing.
// TODO Fuzzing with external program?
// TODO Push site to netifly, alter markdown read me.
// TODO Update Pseudocode from what I have in website.
// TODO Generate final javadoc. Check that everything is documented.
// TODO How to push dist to Git? Problems with fileEncrypted?
// TODO Should I write manual on building site? Maybe few sentences in README?
// TODO Links to directories in HTML should work? Check all links!
// TODO Overview page of html report -> full history by error count? we can use it as well for progress bars.

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
    private static final int MAX_ITERATIONS = configFileReader.getMaxIterations();

    /**
     * If the program reaches this amount of time before doing all the specified iterations, we should also
     * stop fuzzing. Specified in configurations file.
     */
    private static final long TIME_BUDGET_MS = configFileReader.getMaxTime();

    /**
     * The main entry point of the fuzzing process.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        /* In case of random maps or sequences, we need a new file and sequence generator */
        RandomActionSequenceGenerator randomActionSequenceGenerator = new RandomActionSequenceGenerator();
        MapGenerator mapGenerator = new RandomMapGenerator();

        /* New file handlers for the maps and logs, organizing result directory */
         // Create the needed directories and clean them up if that is needed
        FileHandler fileHandler = new FileHandler();
        fileHandler.initializeDirectories(); // Reads count as well
        LogFileHandler logFileHandler = new LogFileHandler();

        /* Initialization result variables */
        // Store results of the process ran
        List<IterationResult> iterationResults = new ArrayList<>();
        Map<Integer, List<IterationResult>> iterationResultsByErrorCode = new HashMap<>();
        Map<String, List<IterationResult>> iterationResultsByOutputMessage = new HashMap<>();

        /* Get the maps and output messages first from the custom and then if additional random is needed*/
        // In case of custom maps or sequence, Add your custom map file paths to this list
        // If 0 or not implemented, nothing is added.
        // Do this last -> otherwise not correct directories and handlers
        List<String> customMaps = getCustomMaps(configFileReader.getCustomMapsNr());
        List<String> customMapsAttributes = getCustomAttributesLog(configFileReader.getCustomMapsNr());
        List<String> customSequences = getCustomSequences(configFileReader.getCustomSequenceNr());
        for (int i = 0; i < MAX_ITERATIONS; i++) {    // How many times does a random file and sequence has to be created?
            // Use custom sequences and maps.
            String mapFilePath;
            String actionSequence;
            if (customMaps.isEmpty()) {mapFilePath = mapGenerator.generateRandomMap();} // If no more, generate randomly with configs file type
            else {mapFilePath = customMaps.remove(0);} // Use custom map file
            if (!customSequences.isEmpty()){actionSequence = customSequences.remove(0);}
            else {actionSequence = randomActionSequenceGenerator.generateRandomActionSequence();}

            /* Try to execute pacman and retrieve exitcode and other results. Alter count of the correct exitcode.*/
            try {
                // Retrieve output data process
                Process process = executeJPacman(mapFilePath, actionSequence);
                int exitCode = process.waitFor();
                String outputMessages = readOutputMessages(process);
                String customAttribute = "";
                if (!customMapsAttributes.isEmpty()){customAttribute = customMapsAttributes.remove(0);}
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
            } catch (IOException |
                     InterruptedException e) {
                System.out.println("Exception during process building.");
                e.printStackTrace();
            }
            /* Check if time budget has been exhausted */
            long endTime = System.currentTimeMillis();
            elapsedTime = endTime - startTime;
            if (elapsedTime >= TIME_BUDGET_MS) {
                System.out.println("Time limit reached.");
                break;
            }
        }
        /* Generate logs and clean up directories if needed*/
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
     *     The files can be found in fuzz4_validCharacterMaps/custom_maps.
     *     </li>
     *     <li>Case 7: Write text files with only valid characters in it (P, M, O, W, F). The map is squared.</li>
     *     The map holds exactly one player. The map holds at least one food. The map does not contain one or more empty lines.
     *     These maps should all be accepted.</li>
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
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight(),
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth());
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
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight(),
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth());
                int i;
                for (i = 0; i < MAX_ITERATIONS; i++) {
                    customMaps.add(randomTextMapGenerator.generateRandomValidCharRandomSizeTextMap());
                }
            }
            case 5 -> { // Try valid character maps, only squared, as many as specified max in configs
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight(),
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth());
                int i;
                for (i = 0; i < MAX_ITERATIONS; i++) {
                    customMaps.add(
                            randomTextMapGenerator.generateRandomValidCharRectangularTextMap(false, false, true));
                }
            }
            case 6 -> { // Try valid character maps of all forms and content, using the corner cases map files of custom_maps directory of fuzz 4 (.../fuzzresults_lessons/fuzz4_validCharacterMaps/custom_maps_inputCopy_4).
                DirectoryHandler search = new DirectoryHandler();
                List<String> filePaths = search.getFilesInDirectory("custom_maps_inputCopy_4");
                customMaps.addAll(filePaths);
            }
            case 7 -> { // Try valid character maps, only squared, as many as specified max in configs, and checked for player food and size
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight(),
                        FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth());
                int i;
                for (i = 0; i < MAX_ITERATIONS; i++) {
                    customMaps.add(
                            randomTextMapGenerator.generateRandomValidCharRectangularTextMap(true, true, false));
                }
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
     * </ul>
     *
     * @param customNr The number specified in the configuration file.
     * @return List of strings that are the custom action sequences.
     */
    private static List<String> getCustomSequences(int customNr){
        List<String> customSequences = new ArrayList<>();
        switch (customNr){
            case 1: { //A hundred times correct string that starts, wait, exit.
                int i;
                for (i = 0; i < MAX_ITERATIONS; i++){
                     customSequences.add("SWE");
                }
                break;
            } case 2:{
                RandomActionSequenceGenerator randomActionSequenceGenerator = new RandomActionSequenceGenerator();
                int i;
                for(i=0; i < MAX_ITERATIONS; i++){
                    customSequences.add(randomActionSequenceGenerator.generateRandomActionSequenceValidCharRandomLength());
                }
            }
            default: {
                // Nothing to add
                break;
            }
        }
        return customSequences;
    }
}


