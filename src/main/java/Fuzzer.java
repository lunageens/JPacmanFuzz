import java.io.File;
import java.io.IOException;

public class Fuzzer {

    // TODO configurations and reader?
    private static final int MAX_ITERATIONS = 100;
    private static final long TIME_BUDGET_MS = 15 * 60000; // 15 Minutes

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // How many times does a random file and sequence has to be created?
        for (int i = 0; i < MAX_ITERATIONS; i++) {

            // Generate inputs
            RandomMap randomMap = new RandomMap();
            File mapFile = randomMap.getMapFile();
            RandomActionSequence randomActionSequence = new RandomActionSequence();
            String actionSequence = randomActionSequence.getActionSequence();

            // Execute Jpacman with the generated inputs
            int exitCode = executeJpacman(mapFile.getPath(), actionSequence);

            // Check the exit code and handle accordingly
            if (exitCode == 0) {  // Normal termination
                System.out.println("Program terminated normally.");
            } else if (exitCode == 10) {  // Normal termination
                System.out.println("Program rejected the input.");
            } else if (exitCode == 1) {    // Crash
                System.out.println("Program crashed.");
            } else {  // Handle other exit codes
                System.out.println("Unknown exit code: " + exitCode);
            }

            // Clean-up file and string
            cleanup(randomMap, randomActionSequence);

            // Check if time budget is exhausted
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            if (elapsedTime >= TIME_BUDGET_MS) {
                System.out.println("Time limit reached.");
                break;
            }

            if (i == MAX_ITERATIONS - 1){ System.out.println("Maximum iterations reached.");}
        }

    }
    private static int executeJpacman(String mapFile, String actionSequence) {
        try {
            Process process = new ProcessBuilder("java", "-jar", "jpacman-3.0.1.jar", mapFile, actionSequence).start(); // command
            process.waitFor(); // wait for process to complete
            return process.exitValue(); // obtain exit value
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static void cleanup(RandomMap randomMap, RandomActionSequence randomActionSequence) {
        randomMap.deleteMapFile();
        randomActionSequence.deleteActionSequence();
        // Clean up any temporary files or resources used during the fuzzing process
        // For example, you can delete any generated map files that are no longer needed
        // You can use the File.delete() method to delete the files
        // Example: new File("path/to/file.map").delete();
    }

}
