package organizers;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static organizers.FileHandler.actualMapsDirectoryPath;


/**
 * A utility class for searching and retrieving files within a directory.
 */
public class DirectoryHandler {
    /**
     * Retrieves the file paths of all files within the specified directory and its subdirectories.
     *
     * @param directoryName the name of the directory to search for
     * @return a list of file paths as strings
     */
    public List<String> getFilesInDirectory(String directoryName) {
        List<String> filePaths = new ArrayList<>();

        // Get the current working directory
        String projectDirectory = System.getProperty("user.dir");
        File projectDir = new File(projectDirectory);

        searchFiles(projectDir, directoryName, filePaths);
        return filePaths;
    }

    /**
     * Recursively adds the file paths of all files within the specified directory and its subdirectories
     * to the filePaths list.
     *
     * @param directory the current directory being processed
     * @param filePaths the list to store the file paths found
     */
    public void addFilesInDirectory(File directory, List<String> filePaths) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    filePaths.add(file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    addFilesInDirectory(file, filePaths);
                }
            }
        }
    }

    /**
     * Recursively searches for the specified directory within the given directory and its subdirectories.
     * When the directory is found, adds the file paths of all files within that directory to the filePaths list.
     *
     * @param directory    the current directory being searched
     * @param directoryName the name of the directory to search for
     * @param filePaths    the list to store the file paths found
     */
    public void searchFiles(File directory, String directoryName, List<String> filePaths) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().equalsIgnoreCase(directoryName)) {
                        addFilesInDirectory(file, filePaths);
                    } else {
                        searchFiles(file, directoryName, filePaths);
                    }
                }
            }
        }
    }

    /**
     * Checks if the specified directory contains any files.
     *
     * @param directory The directory path.
     * @return {@code true} if the directory contains files, {@code false} otherwise.
     * @throws IOException If an I/O error occurs while accessing the directory.
     */
    public static boolean hasFiles(Path directory) throws IOException {
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
     * Gets the highest numbered subdirectory name in the specified parent directory.
     * E.g., run_9
     * Is not in use anymore. We use the fuzzAttemptNr instead. Now also works when renaming directories.
     *
     * @param parentDirectory
     *         The parent directory path.
     *
     * @return The highest numbered subdirectory, or -1 if no numbered subdirectories exist.
     */
    private int getHighestSubDirectory(Path parentDirectory) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parentDirectory)) {
            String maxDirectory = null;
            int maxNumber = -1;
            // for all subdirectories in parent, check if number > maxNumber
            for (Path subDirectory : stream) {
                // if it is a direcotry (and not a file)
                if (Files.isDirectory(subDirectory)) {
                    String directoryName = subDirectory.getFileName().toString();
                    // If it is a name that has the same as our name e.g. run_5, retrieve number at the end
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
     * Cleans up the old actual directories and by moving their contents to the appropriate previous directories (run_x).
     * If the previous directories don't exist, they are created.
     *
     * @param actualDirectory
     *         The path of the actual directory.
     * @param previousDirectory
     *         The path of the previous directory.
     *
     * @throws IOException
     *         If an I/O error occurs during the cleanup process.
     */
    static void cleanUpOldActual(Path actualDirectory, Path previousDirectory) throws IOException {
        // if actual doesn't exist yet, make new one
        if (!Files.exists(actualDirectory)) {
            Files.createDirectory(actualDirectory);
        }
        // if actual_logs or actual_maps already existed, there is files in there from previous runs
        // we don't want it to overwrite our new results
        // therefore, we move it to previous_logs or previous_maps with a subdirectory run_x
        // In case of maps, per subdirectory run_x also subdirectories with errorCodes
        else {
            // If there is not a previous directory, make one. The subdirectory where our actual files should be in
            // is called run_1.
            // If there is a previous directory, get the number of the highest run (e.g. seven directories of previous eight runs)
            // so now in our actual eight directory should move to previous/run_8.
            // Create run directory in previous directory
            String destinationPath;
            if (!Files.exists(previousDirectory)) { // if previous does not exist yet, make one
                Files.createDirectory(previousDirectory);
            }
            //  int latestRun = getHighestSubDirectory(previousDirectory) + 1;
            int latestRun = FileHandler.fuzzAttemptNr - 1;
            destinationPath = previousDirectory + "/run_" + latestRun; // dist - runs that were already there plus this one
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
                        // Look for files in that subdirectory of actual
                        // Move them to our newly created subdirectory of destination
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
     * Moves a map file from the overall actual directory (or other place) to an error directory based on its exit code.
     * See also method getMapFilePath in FileHandler class.
     *
     * @param mapFilePath The path of the map file to move.
     * @param exitCode    The exit code associated with the map file.
     */
    public static void moveMapFileToErrorDirectory(String mapFilePath, int exitCode) {
        try {
            // Determine the path the file is on now
            Path sourcePath = Paths.get(mapFilePath);
            String mapFileName = sourcePath.getFileName().toString();
            // Determine the path that the file should ultimately have, based on its exit code
            String exitDirectoryName;
            switch (exitCode) {
                case 0 -> exitDirectoryName = "exitcode0_accepted";
                case 1 -> exitDirectoryName = "exitcode1_crash";
                case 10 -> exitDirectoryName = "exitcode10_rejected";
                default -> exitDirectoryName = "exitcodeX_unknown";
            }
            String destinationPathText = actualMapsDirectoryPath + "/" + exitDirectoryName + "/" + mapFileName;
            Path destinationPath = Paths.get(destinationPathText);
            Path destinationDirectory = destinationPath.getParent();
            // If we haven't made that parent directory exitcodex_x yet, make one
            if (!Files.exists(destinationDirectory)) {
                Files.createDirectory(destinationDirectory);
            }
            // Move files to correct folder
            Files.move(sourcePath, destinationPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleans the specified directory by deleting its files and subdirectories recursively.
     *
     * @param pathToNeededCleanedDirectory The path to the directory that needs to be cleaned.
     */
    public static void cleanDirectory(String pathToNeededCleanedDirectory) {
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

