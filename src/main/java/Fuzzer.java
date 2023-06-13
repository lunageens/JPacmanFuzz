import dataProviders.ConfigFileReader;
import dataProviders.DirectorySearch;
import managers.FileReaderManager;
import outputProviders.FileHandler;
import outputProviders.IterationResult;
import outputProviders.LogFileHandler;
import outputProviders.MapFileHandler;
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

     The main entry point of the fuzzing process.

     @param args The command-line arguments.
     */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        // In case of random maps or sequences, we need a new file and sequence generator
        RandomActionSequenceGenerator randomActionSequenceGenerator = new RandomActionSequenceGenerator();
        MapGenerator mapGenerator = new RandomMapGenerator();

        // New file handlers
        // Create the needed directories and clean them up if that is needed
        FileHandler fileHandler = new FileHandler();
        fileHandler.initializeDirectories();
        LogFileHandler logFileHandler = new LogFileHandler();
        MapFileHandler mapFileHandler = new MapFileHandler();

        // Store results of the process ran
        List<IterationResult> iterationResults = new ArrayList<>();
        Map<Integer, List<IterationResult>> iterationResultsByErrorCode = new HashMap<>();
        Map<String, List<IterationResult>> iterationResultsByOutputMessage = new HashMap<>();

        // In case of custom maps or sequences
        // Add your custom map file paths to this list
        // If 0 or not implemented, nothing is added
        // Do this last -> otherwise not correct directories and handlers
        List<String> customMaps = getCustomMaps(configFileReader.getCustomMapsNr());
        List<String> customMapsAttributes = getCustomAttributesLog(configFileReader.getCustomMapsNr());
        List<String> customSequences = getCustomSequences(configFileReader.getCustomSequenceNr());

        // How many times does a random file and sequence has to be created?
        for (int i = 0; i < MAX_ITERATIONS; i++) {

            // Use custom sequences and maps
            // If no more, generate randomly with configs file type
            String mapFilePath;
            String actionSequence;
            if (customMaps.isEmpty()) {mapFilePath = mapGenerator.generateRandomMap();} // generate randomly
            else {mapFilePath = customMaps.remove(0);} // Use custom map file
            if (!customSequences.isEmpty()){actionSequence = customSequences.remove(0);}
            else {actionSequence = randomActionSequenceGenerator.generateRandomActionSequence();}

            // Try to execute pacman and retrieve exitcode and other results. Alter count of the correct exitcode.
            // If wrong exit code, save file.
            try {
                // Retrieve output data process
                Process process = executeJPacman(mapFilePath, actionSequence);
                int exitCode = process.waitFor();
                if ((exitCode != 0) && (exitCode != 1) && (exitCode != 10)) {
                    exitCode = -1;
                } // If unknown, -1

                String outputMessages = readOutputMessages(process);
                if (outputMessages.isEmpty()){outputMessages = "None;";}

                String customAttribute = "N.A.";
                if (!customMapsAttributes.isEmpty()){customAttribute = customMapsAttributes.remove(0);}

                // Store output data process in iteration results list
                IterationResult iterationResult = new IterationResult(i + 1, mapFilePath, actionSequence, exitCode, outputMessages, customAttribute);
                iterationResults.add(iterationResult);

                // Update iteration results by error code
                List<IterationResult> errorCodeResults = iterationResultsByErrorCode.getOrDefault(exitCode, new ArrayList<>());
                errorCodeResults.add(iterationResult);
                iterationResultsByErrorCode.put(exitCode, errorCodeResults);

                // Update iteration results by message output
                List<IterationResult> outputMessageResults = iterationResultsByOutputMessage.getOrDefault(outputMessages, new ArrayList<>());
                outputMessageResults.add(iterationResult);
                iterationResultsByOutputMessage.put(outputMessages, outputMessageResults);

                // Clean up when process ran without problems -> use delelete for exit code 0
                // Move map to correct permanent directory if needed
                mapFileHandler.moveMapFileToErrorDirectory(mapFilePath, exitCode);

            } catch (IOException |
                     InterruptedException e) {
                System.out.println("Exception during process building.");
                e.printStackTrace();
            }

            // Check if time budget has been exhausted
            long endTime = System.currentTimeMillis();
            elapsedTime = endTime - startTime;
            if (elapsedTime >= TIME_BUDGET_MS) {
                System.out.println("Time limit reached.");
                break;
            }

        }

        // Write the text logfile.
        logFileHandler.writeIterationResults(iterationResults);
        logFileHandler.writeSummary(iterationResults);
        logFileHandler.close();

        // Write the CSV logfile.
        logFileHandler.generateLogCSVFile(iterationResults);

        // Write the CSV overview logfile.
        logFileHandler.generateLogOverview(iterationResultsByErrorCode, iterationResultsByOutputMessage);

        if (FileHandler.cleanDirectories) {
            fileHandler.cleanDirectory(FileHandler.previousLogsDirectoryPath);
            fileHandler.cleanDirectory(FileHandler.previousMapsDirectoryPath);
        }

        if (FileHandler.logHistory) {
            logFileHandler.generateLogHistory(iterationResultsByErrorCode, elapsedTime);
            logFileHandler.generateLogErrorHistory(iterationResultsByOutputMessage);
            logFileHandler.generateFullLogHistory(iterationResults);
            logFileHandler.generateFullLogHistoryHTMLReport();
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
     * Case 1: Write a text map with one line. The line of the maps are specified in the custom attributes with the same number.
     * Case 2: Write a binary map with one line. The line of the maps are specified in the custom attributes with the same number than text and are encoded.
     * Case 3: Try out different file types. The files can be found in the fuzz3_filetypes/custom_maps_inputCopy directory.
     * Case 4: Write text files with only valid characters in it (P, M, 0, W, F). The files can be of all forms and the valid characters can occur 0, 1 or more in the file.
     * Case 5: Write text files with only valid characters in it (P, M, O, W, F). The map is squared and the player can only occur once.
     * @param customNr The number specified in the configuration file.
     * @return List of Strings that are the file paths of the custom maps.
     */
    private static List<String> getCustomMaps(int customNr){
        List<String> customMaps = new ArrayList<>();
        switch(customNr) {
            case 1: { // Write text file with this
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight(), FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth());
                List<String> customMapsAttributes = getCustomAttributesLog(customNr);
                for (String mapLine : customMapsAttributes){
                    customMaps.add(randomTextMapGenerator.generateCustomTextMap(mapLine));
                }
                break;
            } case 2: { // Try to get valid binary file with encoding binary files.
                RandomBinaryMapGenerator randomBinaryMapGenerator = new RandomBinaryMapGenerator(FileReaderManager.getInstance().getConfigReader().getMaxBinaryMapSize());
                List<String> customMapsAttributes = getCustomAttributesLog(customNr);
                for (String encodedFile : customMapsAttributes){
                    customMaps.add(randomBinaryMapGenerator.generateCustomEncodedMap(encodedFile));
                }
                break;
            } case 3: { // Try different (in)valid file type
                DirectorySearch searcher = new DirectorySearch();
                List<String> filePaths = searcher.getFilesInDirectory("custom_maps_inputCopy");
                for (String filePath : filePaths) { // Don't add the files that give error in Fuzzer
                    // TODO Encrypted file? Permissions files?
                    customMaps.add(filePath);
                }
                break;
            }case 4:{ //Try valid character maps, all forms, as many as specified max in configs
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight(), FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth());
                int i;
                for(i=0; i < MAX_ITERATIONS; i++){
                    customMaps.add(randomTextMapGenerator.generateRandomValidCharRandomSizeTextMap());
                }
                break;
            } case 5: { // Try valid character maps, only squared, as many as specified max in configs
                RandomTextMapGenerator randomTextMapGenerator = new RandomTextMapGenerator(FileReaderManager.getInstance().getConfigReader().getMaxTextMapHeight(), FileReaderManager.getInstance().getConfigReader().getMaxTextMapWidth());
                int i;
                for(i=0; i < MAX_ITERATIONS; i++){
                    customMaps.add(randomTextMapGenerator.generateRandomValidCharRectangularTextMap(true, true));
                }
                break;
            } default: {
                // Nothing to add
                break;
            }
        }
        return customMaps;
    }

    /**
     * Based on the configurations, give some information about the maps into the reports.
     * Case 1: The ASCII character used to generate the one-character text file.
     * Case 2: The String used to generate the encoded binary file (not used currently).
     * Case 3: The description of the invalid file type.
     * Case 4: Some one-line maps with corner cases of the valid characters and their occurrence in the  map.
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
                        String asciiCharacter = "\'"; // ASCII Character ' is programmed with "\'";
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
                DirectorySearch searcher = new DirectorySearch();
                List<String> filePaths = searcher.getFilesInDirectory("custom_maps_inputCopy");
               for (String filePath : filePaths){
                      File file = new File(filePath);
                      String fileName = file.getName();
                      customMapsAttributes.add(fileName);
                  }
                break;
            } case 4: { // Try to see who is more important - player, monster, food, ...
                customMapsAttributes.add("MW");
                customMapsAttributes.add("MWP");
                customMapsAttributes.add("OPF");
                customMapsAttributes.add("OPE");
            }
            default: {
                // Nothing to add
                break;
            }
        }
        return customMapsAttributes;
    }

    /**
     * Based on the configuration, get some custom action sequences.
     * Case 1: For all files made, add a valid custom action sequence (SWE Start - Wait - Exit)
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
                    customSequences.add(randomActionSequenceGenerator.generateRandomActionSequenceValidCharRandomLength(FileReaderManager.getInstance().getConfigReader().getMaxActionSequenceLength()));
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


