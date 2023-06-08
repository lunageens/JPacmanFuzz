import dataProviders.ConfigFileReader;
import managers.FileReaderManager;
import outputProviders.FileHandler;
import outputProviders.IterationResult;
import outputProviders.LogFileHandler;
import outputProviders.MapFileHandler;
import randomGenerators.RandomActionSequenceGenerator;
import randomGenerators.map.MapGenerator;
import randomGenerators.map.RandomMapGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Fuzzer class is the main class that runs the fuzzing process.
 * <p>
 * It generates random action sequences and maps, executes a game simulation,
 * <p>
 * and collects and analyzes the results.
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

        // In case of custom maps or sequences
        // Add your custom map file paths to this list
        List<String> customMaps = new ArrayList<>();
        // Add your custom action sequences to this list
        List<String> customSequences = new ArrayList<>();

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

        // How many times does a random file and sequence has to be created?
        for (int i = 0; i < MAX_ITERATIONS; i++) {

            // Use custom sequences and maps
            // If no more, generate randomly with configs
            String mapFilePath;
            String actionSequence;
            if (!customMaps.isEmpty()){mapFilePath = customMaps.remove(0);} // Use custom map file
            else {mapFilePath = mapGenerator.generateRandomMap();} // generate randomly
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

                // Store output data process in iteration results list
                IterationResult iterationResult = new IterationResult(i + 1, mapFilePath, actionSequence, exitCode, outputMessages);
                iterationResults.add(iterationResult);

                // Update iteration results by error code
                List<IterationResult> errorCodeResults = iterationResultsByErrorCode.getOrDefault(exitCode, new ArrayList<>());
                errorCodeResults.add(iterationResult);
                iterationResultsByErrorCode.put(exitCode, errorCodeResults);

                // Update iteration results by message output
                List<IterationResult> outputMessageResults = iterationResultsByOutputMessage.getOrDefault(outputMessages, new ArrayList<>());
                outputMessageResults.add(iterationResult);
                iterationResultsByOutputMessage.put(outputMessages, outputMessageResults);

                // Clean up when process ran without problems
                // Move map to correct permanent directory if needed
                if (exitCode == 0) {
                    mapFileHandler.deleteMapFile(mapFilePath);
                } else {
                    mapFileHandler.moveMapFileToErrorDirectory(mapFilePath, exitCode);
                }
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
        logFileHandler.generateLogOverview(iterationResultsByErrorCode, iterationResultsByOutputMessage);

        if (FileHandler.cleanDirectories) {
            fileHandler.cleanDirectory(FileHandler.previousLogsDirectoryPath);
            fileHandler.cleanDirectory(FileHandler.previousMapsDirectoryPath);
        }

        if (FileHandler.logHistory) {
            logFileHandler.generateLogHistory(iterationResultsByErrorCode, elapsedTime);
            logFileHandler.generateLogErrorHistory(iterationResultsByOutputMessage);
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

}


