package outputProviders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static outputProviders.FileHandler.actualMapsDirectoryPath;

/**
 * The MapFileHandler class provides methods for handling map files, including moving map files to error directories
 * based on their exit codes and deleting map files.
 */
public class MapFileHandler {

    /**
     * Constructs a MapFileHandler object.
     */
    public MapFileHandler() {
    }

    /**
     * Moves a map file from the overall actual directory (or other place) to an error directory based on its exit code.
     *
     * @param mapFilePath The path of the map file to move.
     * @param exitCode    The exit code associated with the map file.
     */
    public void moveMapFileToErrorDirectory(String mapFilePath, int exitCode) {
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
            ;

            // Move files to correct folder
            Files.move(sourcePath, destinationPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a map file. used for files with exit code accepted.
     *
     * @param mapFilePath The path of the map file to delete.
     */
    public void deleteMapFile(String mapFilePath) {
        File inputFile = new File(mapFilePath);
        boolean isDeleted = inputFile.delete();
        if (!isDeleted) {
            System.out.println("The file on path " + mapFilePath + " has not been deleted successfully.");
        }
    }

}
