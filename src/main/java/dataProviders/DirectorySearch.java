package dataProviders;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


/**
 * A utility class for searching and retrieving files within a directory.
 */
public class DirectorySearch {


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
}

