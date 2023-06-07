import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fuzzer {

    // TODO configurations and reader?
    private static final int MAX_ITERATIONS = 100;
    private static final long TIME_BUDGET_MS = 15 * 60000; // 15 Minutes

    private static File logFile;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        int exitCode0 = 0;
        int exitCode10 = 0;
        int exitCode1 = 0;
        int exitCodeUnknown = 0;

        // How many times does a random file and sequence has to be created?
        for (int i = 0; i < MAX_ITERATIONS; i++) {

            // Pick random file type
            Random random = new Random();
            boolean fileType = random.nextBoolean();

            // Generate random inputs for the map file
            File mapFile;
            String mapFileType;
            String identicalLineWidths = "N.A.";
            if (fileType) {
                mapFileType = "Binary";
                RandomBinaryMap randomBinaryMap = new RandomBinaryMap();
                mapFile = randomBinaryMap.getRandomBinaryFile();
            } else {
                mapFileType = "Text";
                RandomTextMap randomTextMap = new RandomTextMap();
                mapFile = randomTextMap.getMapFile();
                identicalLineWidths = randomTextMap.getIdenticalLineWidths();
            }

            // Generate random action sequence
            RandomActionSequence randomActionSequence = new RandomActionSequence();
            String actionSequence = randomActionSequence.getActionSequence();

            // Execute Jpacman with the generated inputs
            List results = executeJpacman(mapFile.getPath(), actionSequence);
            int exitCode = (int) results.get(0);
            String outputMessages = (String) results.get(1);

            // Check the exit code and handle accordingly
            if (exitCode == 0) {  // Normal termination
                System.out.println("Program terminated normally.");
                exitCode0 = exitCode0 + 1;
                writeLogEntry(exitCode, outputMessages, mapFile, mapFileType, identicalLineWidths, actionSequence);
                cleanup(mapFile, randomActionSequence);  // Clean-up file and string
            } else {// if not terminated normally, write log

                if (exitCode == 10) {  // Normal termination
                    System.out.println("Program rejected the input.");
                    exitCode10 = exitCode10 + 1;
                } else if (exitCode == 1) {    // Crash
                    System.out.println("Program crashed.");
                    exitCode1 = exitCode1 + 1;
                } else {  // Handle other exit codes
                    System.out.println("Unknown exit code: " + exitCode);
                    exitCodeUnknown = exitCodeUnknown + 1;
                }
            }



            // Check if time budget is exhausted
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            if (elapsedTime >= TIME_BUDGET_MS) {
                System.out.println("Time limit reached.");
                break;
            }

            if (i == MAX_ITERATIONS - 1){ System.out.println("Maximum iterations reached.");}
        }

        if (exitCode0 != 0){System.out.println("Program terminated normally " + exitCode0 + " time(s).");}
        if (exitCode10 != 10){System.out.println("Program rejected the output " + exitCode10 + " time(s).");}
        if (exitCode1 != 0){System.out.println("Program crashed " + exitCode1 + " time(s).");}
        if (exitCodeUnknown != 0){System.out.println("Program exited with unknown code " + exitCodeUnknown + " time(s).");}
    }

    private static List executeJpacman(String mapFile, String actionSequence) {
        List results = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "jpacman-3.0.1.jar", mapFile, actionSequence);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start(); // command

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder outputBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null){ outputBuilder.append(line).append("\n");}

            int exitCode = process.waitFor(); // wait for process to complete
            results.add(exitCode);

            String outputMessages = null;
            if (exitCode != 0){ // check if it is crash or rejection
                outputMessages = outputBuilder.toString(); }
            results.add(outputMessages);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            results.add(-1);
            results.add("Exception during process building");
        }
        return results;
    }

    // TODO fix dat dit terug werkt met methods per klasse
    private static void cleanup(File mapFile, RandomActionSequence randomActionSequence) {
        mapFile.delete();
        // Clean up any temporary files or resources used during the fuzzing process
        // For example, you can delete any generated map files that are no longer needed
        // You can use the File.delete() method to delete the files
        // Example: new File("path/to/file.map").delete();
    }

    public static File getLogFile(){
        if (logFile == null){ logFile = createLogFile(); }
        return logFile;
    }

    public static File createLogFile() {
        try {
            // tODO Configs logdirecotry
            File logDirectory =new File("C:\\ST\\JPacmanFuzz");
            File logFile = File.createTempFile("log_", ".txt", logDirectory);
            Fuzzer.logFile = logFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Fuzzer.logFile;
    }


    public static void writeLogEntry(int exitCode, String outputMessages, File mapFile, String mapFileType, String identicalLineWidths, String actionSequence){
        File logFile = getLogFile();
        try {
            // Write the information to the log file
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
            writer.write("----------------------------------------------------");
            writer.write("Exit Code: " + exitCode + "\n");
            writer.write("Output Messages:\n");
            writer.write(outputMessages);
            writer.write("Map File path: " + mapFile.getAbsolutePath() + "\n");
            writer.write("Map File type: " + mapFileType);
            writer.write("Map File had identical line widths: " + identicalLineWidths);
            writer.write("Action Sequence: " + actionSequence + "\n");

            writer.close();

            System.out.println("Log entry saved: " + logFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


