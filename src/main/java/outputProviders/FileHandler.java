package outputProviders;

import dataProviders.ConfigFileReader;
import managers.FileReaderManager;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * The FileHandler class is responsible for handling file operations in the Pacman project.
 * It provides methods for initializing directories, cleaning directories, and managing file paths.
 */
public class FileHandler {

    /**
     * The ConfigFileReader instance used for reading configuration properties.
     */
    private static final ConfigFileReader configFileReader = FileReaderManager.getInstance().getConfigReader();

    /**
     * The ConfigFileReader instance used for reading configuration properties.
     * Default: ${project.root}/fuzzresults
     */
    private static final String resultDirectoryPath = configFileReader.getResultingDirectoryPath();

    /**
     * Indicates whether the user wants to clean directories.
     * Default: false.
     * <p>
     * When cleanDirectories is set to true, the maps directory and logs directory are cleaned, removing all files
     * except sample.map, log_history.csv, and log_errorHistory.csv.
     * </p>
     * <p>
     * If cleanDirectories is set to false, subdirectories (actual_logs and previous_logs) are created in the logs
     * directory to store the current and previous log and CSV files, respectively.
     * Similarly, subdirectories (actual_maps and previous_maps) are created in the maps
     * directory to store the current and previous map files, respectively.
     * </p>
     */
    public static final boolean cleanDirectories = configFileReader.getCleanDirectories();

    /**
     * The path to the overall directory where the map files are located.
     * Path of the system project root + mapsFilePath=maps default config
     * Default: ${project.root}/fuzzresults/maps
     */
    public static final String mapsDirectoryPath = configFileReader.getMapsFileDirectoryPath();


    /**
     * The path to the directory where the actual log files (of this Fuzzer run) are stored.
     * Default: ${project.root}/fuzzresults/maps/actual_maps/
     */
    public static final String actualMapsDirectoryPath = mapsDirectoryPath + "/actual_maps/";


    /**
     * The path to the directory where the previous log files are stored. These were already there before
     * Fuzz started to run, whether that be in actual_maps or previous_maps.
     * Default: ${project.root}/fuzzresults/maps/previous_maps/
     */
    public static final String previousMapsDirectoryPath = mapsDirectoryPath + "/previous_maps/";

    /**
     * The path to the overall directory where the log files are located.
     * Path of the system project root + logFilePath=logs default config
     * Default: ${project.root}/fuzzresults/logs
     */
    public static final String logsDirectoryPath = configFileReader.getLogFileDirectoryPath();

    /**
     * Indicates whether log history files are generated.
     * Default: true.
     * <p>
     * If logHistory is set to true, log_history.csv and log_errorHistory.csv are generated in the main logs directory.
     * These files provide detailed information about the history of previous runs,
     * including timestamps, execution times, exit code counts, and error code and output message combinations.
     * </p>
     */
    public static final boolean logHistory = configFileReader.getLogHistory();

    /**
     * The path to the log_history.csv file.
     * Default: ${project.root}/fuzzresults/logs/log_history.csv
     */
    public static final String logHistoryFilePath = logsDirectoryPath + "/log_history.csv";

    /**
     * The path to the log_errorHistory.csv file.
     * Default: ${project.root}/fuzzresults/logs/log_errorHistory.csv
     */
    public static final String logErrorHistoryFilePath = logsDirectoryPath + "/log_errorHistory.csv";

    /**
     * The path to the directory where the actual (of this Fuzzer run) log files are stored.
     * Default: ${project.root}/fuzzresults/logs/actual_logs/
     */
    public static final String actualLogsDirectoryPath = logsDirectoryPath + "/actual_logs/";

    /**
     * The path to the directory where the previous log files are stored. These were already there before
     * Fuzz started to run, whether that be in actual_logs or previous_logs.
     * Default: ${project.root}/fuzzresults/logs/previous_logs/
     */
    public static final String previousLogsDirectoryPath = logsDirectoryPath + "/previous_logs/";

    /**
     * The name of the log file, not the full path! Only the "log.txt" part.
     * Default: log.txt
     */
    public static final String logFileName = configFileReader.getLogFileName();

    /**
     * The path to the log.txt file.
     * Default: ${project.root}/fuzzresults/logs/actual_logs/log.txt
     */
    public static final String logFilePath = actualLogsDirectoryPath + logFileName;

    /**
     * The path to the log_overview.csv file.
     * Default: ${project.root}/fuzzresults/logs/actual_logs/log_overview.csv
     */
    public static final String csvFilePath = actualLogsDirectoryPath + "log_overview.csv";


    /**
     * Empty constructor of FileHandler class.
     */
    public FileHandler() {
    }


    /**
     * Initializes the necessary directories for file handling.
     * Creates the overall result, logs, and maps directories.
     * Moves existing files and directories from the actual_logs and actual_maps directories to the previous_logs and previous_maps directories, respectively,
     * based on the clean-up rules.
     */
    public void initializeDirectories() {

        // Create overall maps and logs
        Path resultDirectory = Paths.get(resultDirectoryPath);
        Path logsDirectory = Paths.get(logsDirectoryPath);
        Path mapsDirectory = Paths.get(mapsDirectoryPath);
        try {
            if (!Files.exists(resultDirectory)) {
                Files.createDirectory(resultDirectory);
            }
            if (!Files.exists(logsDirectory)) {
                Files.createDirectory(logsDirectory);
            }
            if (!Files.exists(mapsDirectory)) {
                Files.createDirectory(mapsDirectory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create actual_maps, actual_logs
        // If needed, move the files in actual to correct previous
        // If needed, create previous_maps, previous_logs
        // If needed, create previous_maps/run_x , previous_logs/run_x
        Path actualLogsDirectory = Paths.get(actualLogsDirectoryPath);
        Path previousLogDirectory = Paths.get(previousLogsDirectoryPath);
        Path actualMapsDirectory = Paths.get(actualMapsDirectoryPath);
        Path previousMapDirectory = Paths.get(previousMapsDirectoryPath);
        try {
            // If actual_logs exists, move to previous_logs/run_x or create new previous_logs/run_1. If not, create one.
            cleanUpOldActual(actualLogsDirectory, previousLogDirectory);

            // If actual_maps exists, move to previous_maps/run_x or create new previous_logs/run_1. If not, create one.
            cleanUpOldActual(actualMapsDirectory, previousMapDirectory);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Cleans up the old actual directories and by moving their contents to the appropriate previous directories (run_x).
     * If the previous directories don't exist, they are created.
     *
     * @param actualDirectory   The path of the actual directory.
     * @param previousDirectory The path of the previous directory.
     * @throws IOException If an I/O error occurs during the cleanup process.
     */
    private void cleanUpOldActual(Path actualDirectory, Path previousDirectory) throws IOException {
        // if actual doesn't exists yet, make new one
        if (!Files.exists(actualDirectory)) {
            Files.createDirectory(actualDirectory);
        }

        // if actual_logs or actual_maps already existed, there is files in there from previous runs
        // we dont want it to overwrite our new results
        // therefore, we move it to previous_logs or previous_maps with a sub directory run_x
        // In case of maps, per sub directory run_x also subdirectories with errorCodes
        else {

            // If there is not a previous directory, make one. The subdirectory where our actual files should be in
            // is called run_1.
            // If there is a previous directory, get the number of the higest run (e.g. seven directories of previous aight runs
            // so now in our actual eight directory should move to prevous/run_8.
            // Create run directory in previous directory
            String destinationPath;
            if (!Files.exists(previousDirectory)) { // if previous does not exist yet, make one
                Files.createDirectory(previousDirectory);
                destinationPath = previousDirectory + "/run_1"; // dest - first run
            } else {
                int latestRun = getHighestSubDirectory(previousDirectory) + 1;
                destinationPath = previousDirectory + "/run_" + latestRun; // dist - runs that were already there plus this one
            }
            Path destinationDirectory = Paths.get(destinationPath);
            Files.createDirectory(destinationDirectory); // create destination directory: previous_logs/run_x

            // Move actual_X directories (and the files they include) within actual directory to destination run directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(actualDirectory)) {

                // For all files and directories that are in the actual directory
                for (Path actualSubPath : stream) { // these are files and directories mixed

                    // if subitem of actual directory is a directory with files in
                    if (Files.isDirectory(actualSubPath) && hasFiles(actualSubPath)) {

                        // Make a directory that has the same name but is in previous_logs/run_x/
                        String subDirectoryName = actualSubPath.getFileName().toString(); // name of sub actual directory (e.g., exitcode10_rejected)
                        String subDirectoryDestinationPath = destinationPath + "/" + subDirectoryName; // previous_logs/run_x/name of sub actual directory
                        Path destinationSubDirectory = Paths.get(subDirectoryDestinationPath);
                        Files.createDirectory(destinationSubDirectory);

                        // Look for files in that sub directory of actual
                        // Move them to our newly created sub directory of destination
                        try (DirectoryStream<Path> fileStream = Files.newDirectoryStream(actualSubPath)) {
                            for (Path file : fileStream) {
                                String fileName = file.getFileName().toString(); // get file name only
                                String fileDestinationPath = subDirectoryDestinationPath + "/" + fileName; // make path to our new destination
                                Path destinationFile = Paths.get(fileDestinationPath);
                                Files.move(file, destinationFile); // actually move file
                            }
                        }
                    }

                    // If the sub item of the actual is just a normal file
                    else if (Files.isRegularFile(actualSubPath)) {
                        String fileName = actualSubPath.getFileName().toString();
                        String fileDestinationPath = destinationPath + "/" + fileName;
                        Path destinationFile = Paths.get(fileDestinationPath);
                        Files.move(actualSubPath, destinationFile);
                    } else {
                        System.out.println("A sub-item was found but not moved.");
                    }
                }
            }

            assert !hasFiles(actualDirectory);
            assert Files.exists(destinationDirectory);
            assert hasFiles(destinationDirectory);

        }
    }

    /**
     * Gets the highest numbered subdirectory name in the specified parent directory.
     * E.g., run_9
     *
     * @param parentDirectory The parent directory path.
     * @return The highest numbered subdirectory, or -1 if no numbered subdirectories exist.
     */
    private int getHighestSubDirectory(Path parentDirectory) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parentDirectory)) {
            String maxDirectory = null;
            int maxNumber = -1;

            // for all sub directories in parent
            for (Path subDirectory : stream) {

                // if it is a direcotry (and not a file)
                if (Files.isDirectory(subDirectory)) {
                    String directoryName = subDirectory.getFileName().toString();

                    // If it is a name that has the same as our name eg run_5, retrieve number at the end
                    if (directoryName.matches(".*_\\d+$")) {
                        String[] parts = directoryName.split("_");
                        int number = Integer.parseInt(parts[1]);

                        if (number > maxNumber) {
                            maxNumber = number;
                            maxDirectory = directoryName;
                        }
                    }
                }
            }
            return maxNumber;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Checks if the specified directory contains any files.
     *
     * @param directory The directory path.
     * @return {@code true} if the directory contains files, {@code false} otherwise.
     * @throws IOException If an I/O error occurs while accessing the directory.
     */
    private boolean hasFiles(Path directory) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) { // as soon as we find one regular file we can stop looping
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Cleans the specified directory by deleting its files and subdirectories recursively.
     *
     * @param pathToNeededCleanedDirectory The path to the directory that needs to be cleaned.
     */
    public void cleanDirectory(String pathToNeededCleanedDirectory) {
        try {
            Path toCleanMapsDirectory = Paths.get(pathToNeededCleanedDirectory);

            if (Files.exists(toCleanMapsDirectory)) {
                Files.walkFileTree(toCleanMapsDirectory, new SimpleFileVisitor<>() {

                    // Delete files
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // If u want to keep certain files
                        // Specify here
                        // if (!file.toString().equals(fileToKeepPath)) {
                        Files.deleteIfExists(file);
                        // }
                        return FileVisitResult.CONTINUE;
                    }

                    // Delete directories
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }

                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
