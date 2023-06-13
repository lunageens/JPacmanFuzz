package managers;

import dataProviders.ConfigFileReader;

/**
 * The FileReaderManager class is responsible for managing the FileReader functionality in the Pacman project.
 * It provides a single point of access for obtaining the FileReader instance and the ConfigFileReader instance.
 * The FileReaderManager ensures that only one instance of ConfigFileReader is created and reused throughout the system.
 */
public class FileReaderManager {

    /**
     * One instance of FileReaderManager that used in the whole system.
     */
    private static FileReaderManager fileReaderManager = new FileReaderManager(); // only instance of this class

    /**
     * Instance of ConfigFileReader that is used.
     */
    private static ConfigFileReader configFileReader;

    /**
     * Private constructor of FileReaderManager
     */
    private FileReaderManager() { // private constructor to restrict initiation of the class from other classes
    }

    /**
     * Get one instance of FileReaderManager in whole system.
     * Will be used to globally access that one instance.
     *
     * @return FileReaderManager Instance of FileReaderManager that is in use
     */
    public static FileReaderManager getInstance() { // this is public, only global access point to get the instance
        return fileReaderManager;
    }

    /**
     * Get the one ConfigReader in whole system
     * If there is not a ConFigFileReader created yet, make a new one.
     * Otherwise, use the one we have.
     * This allows us to not make many instances of ConfigFileReader.
     *
     * @return ConfigFileReader The one instance of this class that is used in the system.
     */
    public ConfigFileReader getConfigReader() { // if we don't have one, make one. Otherwise, use the one we have
        return (configFileReader == null) ? new ConfigFileReader() : configFileReader;
    }
}