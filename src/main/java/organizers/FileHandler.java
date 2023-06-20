package organizers;

import dataProviders.ConfigFileReader;
import managers.FileReaderManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;

import static organizers.DirectoryHandler.cleanUpOldActual;

/**
 * The FileHandler class is responsible for handling file operations in the Pacman project.
 * It provides methods for initializing directories, cleaning directories, and managing file paths.
 */
public class FileHandler {

    // Variables: Configfile reader, Overall resulting directory path, Option clean directories, Option log history
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
     * Indicates whether log history files are generated.
     * Default: true.
     * <p>
     * If logHistory is set to true, log_history.csv and log_errorHistory.csv are generated in the main logs directory.
     * These files provide detailed information about the history of previous runs,
     * including timestamps, execution times, exit code counts, and error code and output message combinations.
     * </p>
     */
    public static final boolean logHistory = configFileReader.getLogHistory();

    // Variables: Map subdirectories paths (maps, actual_maps and previous_maps)
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

    // Variables: Log subdirectories paths (logs, overview_logs, actual_logs and previous_logs)
    /**
     * The path to the overall directory where the log files are located.
     * Path of the system project root + logFilePath=logs default config
     * Default: ${project.root}/fuzzresults/logs
     */
    public static final String logsDirectoryPath = configFileReader.getLogFileDirectoryPath();

    /**
     * The path to the directory where the overview log files are stored.
     * Default: ${project.root}/fuzzresults/logs/overview_logs/
     */
    public static final String overviewLogsDirectoryPath = logsDirectoryPath + "/overview_logs/";

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

    // Variables: Paths and name of files in actual_logs subdirectory
    // In actual_logs: log.txt, log.csv, and log_overview.csv

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
     * The name of the log file, not the full path!  Only the "log.csv" part.
     * Default: log.csv
     */
    public static final String logCSVFileName = configFileReader.getLogCSVFileName();

    /**
     * The path to the log.csv file.
     * Default: ${project.root}/fuzzresults/logs/actual_logs/log.csv
     */
    public static final String logFileCSVPath = actualLogsDirectoryPath + logCSVFileName;

    /**
     * The path to the log_overview.csv file.
     * Default: ${project.root}/fuzzresults/logs/actual_logs/log_overview.csv
     */
    public static final String csvFilePath = actualLogsDirectoryPath + "log_overview.csv";

    // Variables: Paths of files in overview_logs subdirectory
    // In overview_logs, always: fuzzCount.txt
    // In overview_logs, logHistory=true: log_errorHistory.csv, log_fullHistory.csv, log_history.csv and log_fullHistory_html/...
    // In log_fullHistory_html: index.html, aboutTheFuzzer.html, reportAndFuzzLessons.html, allMaps.html

    /**
     * Path to text file used for keeping track of the times the application has run, that persist across different runs
     * of the application and handles scenarios where the directories or files are deleted or cleaned up.
     * Default: ${project.root}/fuzzresults/logs/overview_logs/fuzzCount.txt
     */
    private static final String fuzzCountFilePath = overviewLogsDirectoryPath + "/fuzzCount.txt";

    /**
     * The path to the log_errorHistory.csv file.
     * Default: ${project.root}/fuzzresults/logs/log_errorHistory.csv
     */
    public static final String logErrorHistoryFilePath = overviewLogsDirectoryPath + "/log_errorHistory.csv";

    /**
     * The path to the log_fullHistory.csv file.
     * Default: ${project.root}/fuzzresults/logs/log_fullHistory.csv
     */
    public static final String logFullHistoryFilePath = overviewLogsDirectoryPath + "/log_fullHistory.csv";

    /**
     * The path to the log_history.csv file.
     * Default: ${project.root}/fuzzresults/logs/log_history.csv
     */
    public static final String logHistoryFilePath = overviewLogsDirectoryPath + "/log_history.csv";

    /**
     * The path to the log_fullHistory_html subdirectory.
     * Default: ${project.root}/fuzzresults/logs/overview_logs/log_fullHistory_html/
     */
    public static String logFullHistoryHTMLDirectoryPath = overviewLogsDirectoryPath + "/log_fullHistory_html/";

    /**
     * The path to the index.html file in the log_fullHistory_html subdirectory.
     * Default: ${project.root}/fuzzresults/logs/overview_logs/log_fullHistory_html/index.html
     */
    public static String logFullHistoryHTMLHomeFilePath = logFullHistoryHTMLDirectoryPath + "index.html";

    /**
     * The path to the aboutTheFuzzer.html file in the log_fullHistory_html subdirectory.
     * Default: ${project.root}/fuzzresults/logs/overview_logs/log_fullHistory_html/aboutTheFuzzer.html
     */
    public static String logFullHistoryHTMLAboutTheFuzzerFilePath = logFullHistoryHTMLDirectoryPath + "aboutTheFuzzer.html";

    /**
     * The path to the reportAndFuzzLessons.html file in the log_fullHistory_html subdirectory.
     * Default: ${project.root}/fuzzresults/logs/overview_logs/log_fullHistory_html/reportAndFuzzLessons.html
     */
    public static String logFullHistoryHTMLReportAndFuzzLessonsFilePath = logFullHistoryHTMLDirectoryPath + "reportAndFuzzLessons.html";

    /***
     * The path to the allMaps.html file in the log_fullHistory_html subdirectory.
     * Default: ${project.root}/fuzzresults/logs/overview_logs/log_fullHistory_html/allMaps.html
     */
    public static String logFullHistoryHTMLAllMapsFilePath = logFullHistoryHTMLDirectoryPath + "allMaps.html";

    /**
     * The path to the welcome.html file in the log_fullHistory_html subdirectory.
     */
    public static String logFullHistoryHTMLWelcomeFilePath = logFullHistoryHTMLDirectoryPath + "welcome.html";

    /**
     * Count the amount of times the fuzzer has run.
     *
     * <p>
     * We could also just count in fuzz class -> would not take into account cleanDirectories and the user deleting
     * * any fuzz-result directory.
     * * We could also just count the highest subDirectory of previous -> would not take into account the user deleting the
     * * actual_maps of his latest run or the user deleting any previous_ directories. We want the count to be accurate when deleting
     * * runs (e.g. deleting run_3 should still give us run_4 and not a new run_3). This is because otherwise the log files
     * * would have the wrong number in their overview files, since the lines of the deleted run_3 will still be in the log files.
     * </p>
     */
    public static int fuzzAttemptNr = 0;

    /**
     * Updates the fuzzAttemptNr with the correct number in its text file.
     * Initializes the necessary directories for file handling.
     * Creates the overall result, logs, and maps directories.
     * Moves existing files and directories from the actual_logs and actual_maps directories
     * to the previous_logs and previous_maps directories, respectively,
     * based on the clean-up rules.
     */
    public void initializeDirectories() {
        // Create overall maps and logs
        Path resultDirectory = Paths.get(resultDirectoryPath);
        Path logsDirectory = Paths.get(logsDirectoryPath);
        Path mapsDirectory = Paths.get(mapsDirectoryPath);
        boolean newRun = false; // u need this bcs u need to check if it existed before, but also need to create the paths
        // in order to write count away.
        try {
            if (!Files.exists(resultDirectory)) {
                Files.createDirectory(resultDirectory);
                newRun = true;
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

        // Create overview_logs directory path, to store fuzzCount and others away
        Path overviewLogsDirectory = Paths.get(overviewLogsDirectoryPath);
        Path overviewLogsHtmlDirectory = Paths.get(logFullHistoryHTMLDirectoryPath);
        try {
            if (!Files.exists(overviewLogsDirectory)) {
                Files.createDirectory(overviewLogsDirectory);
            }
            if (!Files.exists(overviewLogsHtmlDirectory)) {
                Files.createDirectory(overviewLogsHtmlDirectory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read and update the count of fuzz attempts, to use it in name previous
        // If is our first run or user deletes whole fuzz results (never done by program) -> set to 1
        // if cleanDirectories is true -> user still wants to see overview maybe, and if set to false correct run number
        // if logHistory is false -> user still wants correct clean directories maybe, and if set to true the overview files
        // should have correct run attempt nr even when files of previous runs are not recorded on the overview files
        // (because user maybe did store them in directories).
        int fuzzAttemptNr = readFuzzCount();
        if (newRun) {
            fuzzAttemptNr = 1;
        } else {
            fuzzAttemptNr++;
        }
        writeFuzzCount(fuzzAttemptNr);
        FileHandler.fuzzAttemptNr = fuzzAttemptNr;

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
     * Reads the fuzzAttemptNr from the fuzzCountFile.
     *
     * @return int fuzzAttemptNr
     */
    private static int readFuzzCount() {
        try {
            Path fuzzAttemptNrFilePath = Paths.get(fuzzCountFilePath);
            if (!Files.exists(fuzzAttemptNrFilePath)) {
                return 1;
            }
            String content = Files.readString(fuzzAttemptNrFilePath).trim();
            return Integer.parseInt(content);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Writes the updated fuzzAttemptNr to the fuzzCountFile.
     *
     * @param fuzzAttemptNr
     *         The updated fuzzAttemptNr.
     */
    private static void writeFuzzCount(int fuzzAttemptNr) {
        try {
            Path fuzzCountPath = Paths.get(fuzzCountFilePath);
            if (!Files.exists(fuzzCountPath) && fuzzAttemptNr > 1) {
                throw new RuntimeException("The fuzz count is not correct, because you deleted the fuzzCount.txt but not" +
                        "the remaining logs and maps directories. This is not allowed. Please delete the fuzzresults directory completely " +
                        "to restart the count and run again.");
            }
            Files.writeString(fuzzCountPath, Integer.toString(fuzzAttemptNr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the name of the map file on this path
     * @param filePath Relative filepath
     * @return String filename of the map.
     */
    public static String getFileName(String filePath) {
        File file = new File(filePath);
        return file.getName();
    }

    /***
     * Gives the full text of the text file
     * @param filePath Relative filepath
     * @return String full text of file
     */
    // Not sure of this works? I think it does not.
    public static String getFileText(String filePath) {
        StringBuilder fileText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileText.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileText.toString();
    }

    /**
     * Returns a clean-up file path as string, taking into account different uses of / and \ and
     * if the path should be relative
     * @param filePath String filePath that needs to be cleaned up
     * @param backlash boolean if backslash should be used instead of forward slash
     * @param relative boolean if the path should be relative
     * @return String file path that only has single \ or / in it
     */
    public static String normalizeFilePath(String filePath, boolean backlash, boolean relative) {
        if (relative){
            Path absolutePath = Paths.get(filePath);
            Path currentPath = Paths.get("").toAbsolutePath(); // where project is saved.
            Path relativePath = currentPath.relativize(absolutePath);
            filePath = relativePath.toString();
        }
        String normalizedPath = filePath.replace("/", "\\");   // Replace forward slashes with backslashes
        normalizedPath = normalizedPath.replace("//", "\\");  // Replace double forward slashes with a single backslash
        normalizedPath = normalizedPath.replace("/\\", "\\"); // Replace mixed slashes (/\) with a single backslash
        normalizedPath = normalizedPath.replace("\\/", "\\"); // Replace mixed slashes (\/) with a single backslash
        normalizedPath = normalizedPath.replace("\\\\", "\\"); // Replace double backslashes with a single backslash
        normalizedPath = normalizedPath.replace("/\\\\", "\\"); // Replace mixed slashes (/\\) with a single backslash
        normalizedPath = normalizedPath.replace("\\/\\", "\\"); // Replace mixed slashes (\\/) with a single backslash
        if (!backlash){normalizedPath = normalizedPath.replace("\\", "/"); }   // Replace backslash with forward slashes
        return normalizedPath;
    }

    /**
     * Default values of normalizeFilePath are forward slash and absolute path
     * @param filePath String absolute file path
     * @return filePath String absolute file path with /
     */
    public static String normalizeFilePath(String filePath){
        return normalizeFilePath(filePath, false, false);
    }

    /**
     * Returns a cleaned-up file path as Path object, taking into account different uses of / and \
     * @param filePath String absolute file path
     * @param backlash boolean if backslash should be used instead of forward slash
     * @return filePath Path object
     */
    public static Path normalizeFilePath(String filePath, boolean backlash) {
        return Paths.get(normalizeFilePath(filePath, backlash, false)); // Cannot have relative with path type
    }

    /**
     * Returns a cleaned-up file path as Path object, taking into account different uses of / and \
     * @param filePath Path object
     * @param backlash  if backslash should be used instead of forward slash
     * @return filePath Path object
     */
    public static Path normalizeFilePath(Path filePath, boolean backlash) {
        return Paths.get(normalizeFilePath(filePath.toString(), backlash, false)); // Cannot have relative with path type
    }

    /**
     * Class that holds information about a file type.
     */
    public static class FileTypeResolver {

        /**
         * Returns the file information type object for the given extension. Creates new one for unknown extensions.
         * @param extension Extension of the file
         * @return Fileinformation object for the given extension.
         */
        public static FileInformation getFileInformation(String extension) {
            return switch (extension.toLowerCase()) {
                case ".bmp" -> new FileInformation("Image file", ".bmp", "Bitmap", "Graphics format");
                case ".gif" ->
                        new FileInformation("Image file", ".gif", "Graphics Interchange Format", "Graphics format");
                case ".heif" ->
                        new FileInformation("Image file", ".heif", "High Efficiency Image Format File", "Graphics format");
                case ".heic" ->
                        new FileInformation("Image file", ".heic", "High Efficiency Image Format File", "Graphics format");
                case ".jpg" ->
                        new FileInformation("Image file", ".jpg", "Joint Photographic Experts Group", "Graphics format");
                case ".jpeg" ->
                        new FileInformation("Image file", ".jpeg", "Joint Photographic Experts Group", "Graphics format");
                case ".png" ->
                        new FileInformation("Image file", ".png", "Portable Network Graphics", "Graphics format");
                case ".psd" -> new FileInformation("Image file", ".psd", "Photoshop Document", "Graphics format");
                case ".svg" -> new FileInformation("Image file", ".svg", "Scalable Vector Graphics", "Graphics format");
                case ".tif" -> new FileInformation("Image file", ".tif", "Tagged Image File Format", "Graphics format");
                case ".tiff" ->
                        new FileInformation("Image file", ".tiff", "Tagged Image File Format", "Graphics format");
                case ".doc" -> new FileInformation("Text file", ".doc", "Microsoft Word Text Document", "Text file");
                case ".docx" -> new FileInformation("Text file", ".docx", "Microsoft Word Text Document", "Text file");
                case ".md" -> new FileInformation("Text file", ".md", "Markdown Documentation", "Text file");
                case ".odt" -> new FileInformation("Text file", ".odt", "OpenDocument Text", "Text file");
                case ".pdf" -> new FileInformation("Text file", ".pdf", "Portable Document Format", "Document format");
                case ".ppt" ->
                        new FileInformation("Text file", ".ppt", "Microsoft PowerPoint Presentation", "Presentation file");
                case ".pptx" ->
                        new FileInformation("Text file", ".pptx", "Microsoft PowerPoint Presentation", "Presentation file");
                case ".rtf" -> new FileInformation("Text file", ".rtf", "Rich Text Format", "Text file");
                case ".txt" -> new FileInformation("Text file", ".txt", "Unformatted Text Document", "Text file");
                case ".xls" -> new FileInformation("Text file", ".xls", "Microsoft Excel Document", "Spreadsheet file");
                case ".xlsx" ->
                        new FileInformation("Text file", ".xlsx", "Microsoft Excel Document", "Spreadsheet file");
                case ".flac" -> new FileInformation("Audio file", ".flac", "Free Lossless Audio Codec", "Audio file");
                case ".mp3" -> new FileInformation("Audio file", ".mp3", "MPEG Audio Layer 3", "Audio file");
                case ".aac" -> new FileInformation("Audio file", ".aac", "Advanced Audio Coding", "Audio file");
                case ".ogg" ->
                        new FileInformation("Audio file", ".ogg", "Multimedia Container Format", "Container format");
                case ".wma" -> new FileInformation("Audio file", ".wma", "Windows Media Audio", "Audio file");
                case ".wav" -> new FileInformation("Audio file", ".wav", "Waveform Audio File Format", "Audio file");
                case ".wave" -> new FileInformation("Audio file", ".wave", "Waveform Audio File Format", "Audio file");
                case ".avi" -> new FileInformation("Video file", ".avi", "Audio Video Interleave", "Video format");
                case ".flv" -> new FileInformation("Video file", ".flv", "Flash Video", "Video format");
                case ".mov" ->
                        new FileInformation("Video file", ".mov", "Apple's QuickTime Player File", "Video format");
                case ".mp4" -> new FileInformation("Video file", ".mp4", "MPEG-4 Content", "Video format");
                case ".dll" -> new FileInformation("System file", ".dll", "Dynamic Link Library", "System file");
                case ".drv" -> new FileInformation("System file", ".drv", "Driver files", "Driver file");
                case ".ini" ->
                        new FileInformation("System file", ".ini", "Initializing Program Settings Configuration File", "Initialization file");
                case ".tmp" ->
                        new FileInformation("System file", ".tmp", "Temporary Automatically Generated Data File", "Temporary file");
                case ".jar" -> new FileInformation("Compressed data file", ".jar", "Java Archive", "Archive file");
                case ".rar" ->
                        new FileInformation("Compressed data file", ".rar", "Compressed Files and Folders", "Archive file");
                case ".zip" ->
                        new FileInformation("Compressed data file", ".zip", "Compressed Files and Folders", "Archive file");
                case ".bat" -> new FileInformation("Executable file", ".bat", "Batch File", "Batch file");
                case ".com" -> new FileInformation("Executable file", ".com", "Command", "Executable file");
                case ".exe" -> new FileInformation("Executable file", ".exe", "Executable", "Executable file");
                case ".css" -> new FileInformation("Web file", ".css", "Cascading Style Sheets", "Stylesheet language");
                case ".html" ->
                        new FileInformation("Web file", ".html", "Hypertext Markup Language", "Document format");
                case ".js" -> new FileInformation("Web file", ".js", "JavaScript", "JavaScript code file");
                case ".php" -> new FileInformation("Web file", ".php", "Hypertext Preprocessor", "Script file");
                case ".xml" -> new FileInformation("Web file", ".xml", "Extensible Markup Language", "Document format");
                case ".eml" -> new FileInformation("Email file", ".eml", "Text-based Email", "Email file");
                case ".msg" -> new FileInformation("Email file", ".msg", "Microsoft Outlook Email", "Email file");
                case ".ost" ->
                        new FileInformation("Email file", ".ost", "Microsoft Outlook Data File with locally stored data", "Data file");
                case ".pst" ->
                        new FileInformation("Email file", ".pst", "Microsoft Outlook Data File with personal information", "Container file");
                default -> new FileInformation("Unknown file type", extension, "", "");
            };
        }

        /**
         * Returns the file type of the map file, based on its suffix in the file path.
         *
         * @param fileInformation Object containing the file type information.
         *
         * @param includeFullFileName
         *         True if the full file name should be included.
         * @param includeGenericType
         *         True if the generic type of the file type should be included.
         * @param includeFormatType
         *         True if the format type of the file type should be included.
         *
         * @return String, with all information
         */
        public static String formatFileInformation(FileInformation fileInformation,
                                                   boolean includeFullFileName, boolean includeGenericType, boolean includeFormatType) {
            StringBuilder formattedInformation = new StringBuilder();
            if (includeFullFileName) {
                formattedInformation.append(fileInformation.fullFileType);
                formattedInformation.append(" ");
            }
            if (includeGenericType) {
                formattedInformation.append("(");
                formattedInformation.append(fileInformation.genericFileType);
                formattedInformation.append(") ");
            }
            if (includeFormatType) {
                formattedInformation.append("as ");
                formattedInformation.append(fileInformation.formatFileType);
                formattedInformation.append(" ");
            }
            // Remove the last space
            formattedInformation.deleteCharAt(formattedInformation.length() - 1);
            return formattedInformation.toString();
        }

        /**
         * File type information class.
         */
        private static class FileInformation {

            /**
             * Generic file type. e.g. image
             */
            private final String genericFileType;
            /**
             * File extension.
             */
            private final String extension;
            /**
             * Format of the file type.
             */
            private final String formatFileType;
            /**
             * Full file type in words.
             */
            public String fullFileType;

            public FileInformation(String genericFileType, String extension, String fullFileType, String formatFileType) {
                this.fullFileType = fullFileType;
                this.genericFileType = genericFileType;
                this.extension = extension;
                this.formatFileType = formatFileType;
            }
        }
    }

}
