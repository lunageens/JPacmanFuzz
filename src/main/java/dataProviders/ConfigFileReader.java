package dataProviders;

import enums.MapFileType;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * The ConfigFileReader class is responsible for reading and retrieving configuration properties from the Configuration.properties file.
 * It provides methods to access various configuration settings used in the Pacman project.
 */
public class ConfigFileReader {

    /**
     * The path to the configuration file.
     */
    private final String CONFIG_FILE_PATH = "configs//Configuration.properties";

    /**
     * The default directory name for storing the results.
     */
    private static final String DEFAULT_RESULT_DIRECTORY = "fuzzresults";

    /**
     * The default directory name for storing the map files.
     */
    private final String DEFAULT_MAP_DIRECTORY = "maps";

    /**
     * The default directory name for storing the log files.
     */
    private final String DEFAULT_LOG_DIRECTORY = "logs";

    /**
     * The default name for the log file.
     */
    private final String DEFAULT_LOG_NAME = "log";

    /**
     * The Properties object to hold the configuration properties.
     */
    private Properties properties;


    /**
     * Constructs a ConfigFileReader object and loads the properties from the configuration file.
     * If the configuration file is not found, a RuntimeException is thrown.
     */
    public ConfigFileReader() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(CONFIG_FILE_PATH)); // try loading property file
            properties = new Properties();
            try {
                properties.load(reader); // try reading properties file
                reader.close();
            } catch (IOException e) {
                System.out.println("Problems with reading the properties file");
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Configuration.properties not found at " + CONFIG_FILE_PATH);
        }
    }

    /**
     * Retrieves the map file type specified in the configuration file.
     *
     * @return The map file type (TEXT, BINARY, or ALL).
     */
    public MapFileType getMapFilesType() {
        String fileType = properties.getProperty("fileType");
        if (Objects.equals(fileType, "txt")) {
            return MapFileType.TEXT;
        }
        if (Objects.equals(fileType, "bin")) {
            return MapFileType.BINARY;
        }
        if (Objects.equals(fileType, "all")){
            return MapFileType.ALL;
        }
        return MapFileType.ALL;
    }

    /**
     * Retrieves the name of the log file specified in the configuration file.
     * If the name is not specified or empty, it returns the default log file name.
     *
     * @return The name of the log file.
     */
    public String getLogFileName() {

        String name = properties.getProperty("logFileName");
        if (name == null || name.isEmpty()) {
            name = DEFAULT_LOG_NAME;
        }

        return name + ".txt";
    }

    public String getLogCSVFileName() {
        String name = properties.getProperty("logFileName");
        if (name == null || name.isEmpty()) {
            name = DEFAULT_LOG_NAME;
        }

        return name + ".csv";
    }

    /**
     * Retrieves the directory path for storing the log files.
     * If the path is not specified, it returns the default log directory name within the resulting directory path.
     *
     * @return The directory path for storing the log files.
     */
    public String getLogFileDirectoryPath() {
        // Get current working directory
        String resultRoot = getResultingDirectoryPath();

        // Get path in configuration file, direcorty name logs?
        String logFilePathProject = properties.getProperty("logFilePath");
        if (logFilePathProject == null || logFilePathProject.isEmpty()) {
            logFilePathProject = DEFAULT_LOG_DIRECTORY;
        }

        return resultRoot + "/" + logFilePathProject;
    }

    /**
     * Retrieves the directory path for storing the map files.
     * If the path is not specified, it returns the default map directory name within the resulting directory path.
     *
     * @return The directory path for storing the map files.
     */
    public String getMapsFileDirectoryPath() {
        // Get current results directory
        String resultRoot = getResultingDirectoryPath();

        // Get path in configuration file, direcoty name maps?
        String mapFilePathProject = properties.getProperty("mapFilePath");
        if (mapFilePathProject == null || mapFilePathProject.isEmpty()) {
            mapFilePathProject = DEFAULT_MAP_DIRECTORY;
        }

        return resultRoot + "/" + mapFilePathProject;
    }

    /**
     * Retrieves the resulting directory path where all the output files and directories will be stored.
     * If the path is not specified, it returns the default resulting directory name within the project root directory.
     *
     * @return The resulting directory path.
     */
    public String getResultingDirectoryPath() {
        // Get current working directory
        String projectRoot = System.getProperty("user.dir");

        // Get path in configuration file, directory name maps?
        String mapResultProject = properties.getProperty("resultPath");
        if (mapResultProject == null || mapResultProject.isEmpty()) {
            mapResultProject = DEFAULT_RESULT_DIRECTORY;
        }

        return projectRoot + "/" + mapResultProject;
    }


    /**
     * Retrieves the maximum number of iterations specified in the configuration file.
     * If the maximum iterations property is not found or cannot be parsed as an integer, it returns a default value of 100.
     *
     * @return The maximum number of iterations.
     */
    public int getMaxIterations() {
        return Integer.parseInt(properties.getProperty("maxIterations", "100"));
    }

    /**
     * Retrieves the maximum time specified in the configuration file.
     * If the maximum time property is not found or cannot be parsed as a long, it returns a default value of 900000 (15 minutes).
     *
     * @return The maximum time in milliseconds.
     */
    public long getMaxTime() {
        return Long.parseLong(properties.getProperty("maxTime", "900000"));
    }

    /**
     * Retrieves the maximum height of a text-based map specified in the configuration file.
     * If the maximum height property is not found or cannot be parsed as an integer, it returns a default value of 20.
     *
     * @return The maximum height of a text-based map.
     */
    public int getMaxTextMapHeight() {
        return Integer.parseInt(properties.getProperty("maxTextMapHeight", "20"));
    }

    /**
     * Retrieves the maximum width of a text-based map specified in the configuration file.
     * If the maximum width property is not found or cannot be parsed as an integer, it returns a default value of 20.
     *
     * @return The maximum width of a text-based map.
     */
    public int getMaxTextMapWidth() {
        return Integer.parseInt(properties.getProperty("maxTextMapWidth", "20"));
    }

    /**
     * Retrieves the maximum size of a binary map specified in the configuration file.
     * If the maximum size property is not found or cannot be parsed as an integer, it returns a default value of 1000.
     *
     * @return The maximum size of a binary map.
     */
    public int getMaxBinaryMapSize() {
        return Integer.parseInt(properties.getProperty("maxBinaryMapSize", "1000"));
    }

    /**
     * Retrieves the maximum length of an action sequence specified in the configuration file.
     * If the maximum length property is not found or cannot be parsed as an integer, it returns a default value of 5.
     *
     * @return The maximum length of an action sequence.
     */
    public int getMaxActionSequenceLength() {
        return Integer.parseInt(properties.getProperty("maxActionSequenceLength", "5"));
    }

    /**
     * Retrieves the flag indicating whether to clean the directories specified in the configuration file.
     * If the clean directories property is not found or cannot be parsed as a boolean, it returns false.
     *
     * @return true if the directories should be cleaned, false otherwise.
     */
    public boolean getCleanDirectories() {
        String cleanDir = properties.getProperty("cleanDirectories");
        if (cleanDir != null) return Boolean.valueOf(cleanDir);
        return false;
    }

    /**
     * Retrieves the flag indicating whether to log the history of actions specified in the configuration file.
     * If the log history property is not found or cannot be parsed as a boolean, it returns true.
     *
     * @return true if the history should be logged, false otherwise.
     */
    public boolean getLogHistory() {
        String logHistory = properties.getProperty("logHistory");
        if (logHistory != null) return Boolean.valueOf(logHistory);
        return true;
    }

    public int getCustomMapsNr(){
        String customMapsNr = properties.getProperty("customMapsNr", "0");
        return Integer.parseInt(customMapsNr);
    }

    public int getCustomSequenceNr(){
        String customSeqNr = properties.getProperty("customSequenceNr", "0");
        return Integer.parseInt(customSeqNr);
    }


}


