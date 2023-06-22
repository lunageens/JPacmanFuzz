package outputProviders.logGenerator;

import organizers.FileHandler;
import outputProviders.logGenerator.pages.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class is responsible for the overall creation of the HTML page files that contains the report.
 */
public class LogHTMLFileHandler {

    // Subclasses initializers
    /**
     * Instance of subclass that generates the home page of the report.
     */
    private HomePageGenerator homePageGenerator;

    /**
     * Instance of subclass that generates the about the fuzzer page of the report.
     */
    private AboutFuzzerPageGenerator aboutFuzzerPageGenerator;

    /**
     *  Instance of subclass that generates the overview report and conclusions page of the report.
     */
    private OverviewAndConclusionsPageGenerator overviewAndConclusionsPageGenerator;

    /**
     * Instance of subclass that generates the all maps page of the report.
     */
    private AllMapsPageGenerator allMapsPageGenerator;

    /**
     * Instance of the subclass that generated the welcome page of the report.
     */
    private WelcomePageGenerator welcomePageGenerator;

    // Data of CSV file variables
    /**
     * A list with as string the headers of the CSV file. These are already formatted as wished.
     */
    public static List<String> headers;

    /**
     * A list with items lists (row of the CSV  file) of strings (values in the row of the CSV file).
     */
    public static List<List<String>> rows;

    // Templates paths
    /**
     * Path to the template for the HTML file that contains the template for the home page of the report.
     */
    public static final String ReportHomeTemplatePath = System.getProperty("user.dir") + "/src/main/resources/report-home-template.html";

    /**
     * Path to the template for the HTML file that contains the template for the about the fuzzer page of the report.
     */
    public static final String ReportAboutFuzzerTemplatePath = System.getProperty("user.dir") + "/src/main/resources/report-aboutTheFuzzer-template.html";

    /**
     * Path to the template fo the HTML file that contains the template for the overview report and conclusions page of the report.
     */
    public static final String ReportOverviewTemplatePath = System.getProperty("user.dir") + "/src/main/resources/report-overviewAndConclusions-template.html";

    /**
     * Path to the template for the HTML file that contains the template for the all maps page of the report.
     */
    public static final String ReportAllMapsTemplatePath = System.getProperty("user.dir") + "/src/main/resources/report-allMaps-template.html";

    /**
     * Path to the template for the HTML file that containt the template for the welcome page of the report
     */
    public static final String ReportWelcomeTemplatePath = System.getProperty("user.dir") + "/src/main/resources/report-welcome-template.html";


    // Destinations
    /**
     * Path to the HTML file that contains the generated home page of the report.
     */
    public static final String ReportHomePath = FileHandler.logFullHistoryHTMLHomeFilePath;

    /**
     * Path to the HTML file that contains the generated about the fuzzer page of the report.
     */
    public static final String ReportAboutFuzzerPath = FileHandler.logFullHistoryHTMLAboutTheFuzzerFilePath;

    /**
     * Path to the HTML file that contains the generated overview report and conclusions page of the report.
     */
    public static final String ReportOverviewPath = FileHandler.logFullHistoryHTMLReportAndFuzzLessonsFilePath;

    /**
     * Path to the HTML file that contains the generated all maps page of the report.
     */
    public static final String ReportAllMapsPath = FileHandler.logFullHistoryHTMLAllMapsFilePath;

    /**
     * Path to the HTML file that contains the generated welcome page of the report.
     */
    public static final String ReportWelcomePath = FileHandler.logFullHistoryHTMLWelcomeFilePath;

    /**
     * Constructor for the LogHTMLFileHandler class.
     * Do not initialize subclasses here!
     */
    public void LogFileHTMLHandler() {
   }

    /**
     * Generates the HTML report by processing the log file, modifying the HTML templates of all pages, and writing the generated
     * HTMLs to the appropriate HTML files.
     */
    public void generateHTMLReport()  {
        // Read data in from CSV file and add it to the rigth class.
        processLogFile();

        // Initialize the subclasses of the LogHTMLFileHandler class.
        // Do not put this in constructor because it is not possible to initialize subclasses in constructors (circular dependencies).
        homePageGenerator = new HomePageGenerator();
        aboutFuzzerPageGenerator = new AboutFuzzerPageGenerator();
        overviewAndConclusionsPageGenerator = new OverviewAndConclusionsPageGenerator();
        allMapsPageGenerator = new AllMapsPageGenerator();
        welcomePageGenerator = new WelcomePageGenerator();

        // Generate the html file page by page
        homePageGenerator.writeHTMLPage(ReportHomePath,homePageGenerator.generateHTMLPage(ReportHomeTemplatePath, ReportHomePath));
        aboutFuzzerPageGenerator.writeHTMLPage(ReportAboutFuzzerPath,aboutFuzzerPageGenerator.generateHTMLPage(ReportAboutFuzzerTemplatePath, ReportAboutFuzzerPath));
        overviewAndConclusionsPageGenerator.writeHTMLPage(ReportOverviewPath,overviewAndConclusionsPageGenerator.generateHTMLPage(ReportOverviewTemplatePath, ReportOverviewPath));
        allMapsPageGenerator.writeHTMLPage(ReportAllMapsPath,allMapsPageGenerator.generateHTMLPage(ReportAllMapsTemplatePath, ReportAllMapsPath));
        welcomePageGenerator.writeHTMLPage(ReportWelcomePath,welcomePageGenerator.generateHTMLPage(ReportWelcomeTemplatePath, ReportWelcomePath));
    }

    /**
     * Processes the log file and extracts headers and rows of data.
     */
    public void processLogFile() {
        headers = new ArrayList<>();
        rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FileHandler.logFullHistoryFilePath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                List<String> row = LogCSVFileHandler.parseCSVLine(line);
                if (isFirstLine) {
                    headers = row;
                    isFirstLine = false;
                } else{
                    rows.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the unique values of a specific column in the CSV data.
     *
     * @param columnIndex The index of the column.
     * @return A list of unique values in the specified column.
     */
    public static List<String> getUniqueValues(int columnIndex) {
        Set<String> values = new HashSet<>();
        for (List<String> row : rows) {
            values.add(row.get(columnIndex));
        }

        // Makes the values in alphabetical order
        List<String> valuesList = new ArrayList<>(values);
        Collections.sort(valuesList);

        return valuesList;
    }

    /**
     * Gets the index of a specific column in the CSV data.
     * @param header The header of the column.
     * @return The index of the column in rows as int.
     */
    public static int getColumnIndex(String header) {
        return headers.indexOf(header);
    }

    /**
     * Returns  a string with the first letter of each word in the string capitalized, and the spaces between the
     * words removed.
     * E.g. "this is a test" -> "ThisIsATest"
     *
     * @param header
     *         The string to be converted
     *
     * @return The converted string.
     */
    public static String getIDName(String header) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : header.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
