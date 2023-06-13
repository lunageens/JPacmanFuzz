package outputProviders.logHTMLFile;

import outputProviders.FileHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Collections;

import static outputProviders.LogFileHandler.parseCSVLine;

public class LogHTMLFileHandler {

    // Subclasses

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
    // Data

    /**
     * The path to the CSV file that contains the data of the log file.
     */
    private static final String CSVDataFilePath = FileHandler.logFullHistoryFilePath;

    /**
     * A list with as string the headers of the CSV file.
     */
    public static List<String> headers;

    /**
     * A list with items lists (row of the CSV  file) of strings (values in the row of the CSV file).
     */
    public static List<List<String>> rows;

    // Templates

    /**
     * Path to the template for the HTML file that contains the template for the home page of the report.
     */
    public static final String ReportHomeTemplatePath = System.getProperty("user.dir") + "/src/main/java/outputProviders/HTMLReport/report-home-template.html";

    /**
     * Path to the template for the HTML file that contains the template for the about the fuzzer page of the report.
     */
    public static final String ReportAboutFuzzerTemplatePath = System.getProperty("user.dir") + "/src/main/java/outputProviders/HTMLReport/report-aboutTheFuzzer-template.html";

    /**
     * Path to the template fo the HTML file that contains the template for the overview report and conclusions page of the report.
     */
    public static final String ReportOverviewTemplatePath = System.getProperty("user.dir") + "/src/main/java/outputProviders/HTMLReport/report-overviewAndConclusions-template.html";

    /**
     * Path to the template for the HTML file that contains the template for the all maps page of the report.
     */
    public static final String ReportAllMapsTemplatePath = System.getProperty("user.dir") + "/src/main/java/outputProviders/HTMLReport/report-allMaps-template.html";


    // Destinations

    /**
     * Path to the HTML file that contains the template for the home page of the report.
     */
    public static final String ReportHomePath = FileHandler.logFullHistoryHTMLHomeFilePath;

    /**
     * Path to the HTML file that contains the template for the about the fuzzer page of the report.
     */
    public static final String ReportAboutFuzzerPath = FileHandler.logFullHistoryHTMLAboutTheFuzzerFilePath;

    /**
     * Path to the HTML file that contains the template for the overview report and conclusions page of the report.
     */
    public static final String ReportOverviewPath = FileHandler.logFullHistoryHTMLReportAndFuzzLessonsFilePath;

    /**
     * Path to the HTML file that contains the template for the all maps page of the report.
     */
    public static final String ReportAllMapsPath = FileHandler.logFullHistoryHTMLAllMapsFilePath;


    /**
     * Constructor for the LogHTMLFileHandler class.
     * Initializes the subclasses of the LogHTMLFileHandler class.
     */
    public void LogFileHTMLHandler() {
       homePageGenerator = new HomePageGenerator();
       aboutFuzzerPageGenerator = new AboutFuzzerPageGenerator();
       overviewAndConclusionsPageGenerator = new OverviewAndConclusionsPageGenerator();
       allMapsPageGenerator = new AllMapsPageGenerator();
   }

    /**
     * Generates the HTML report by processing the log file, modifying the HTML templates of all pages, and writing the generated
     * HTMLs to the appropriate HTML files.
     */
    public void generateHTMLReport()  {
        // Read data in from CSV file and add it to the rigth class.
        processLogFile();

        // Generate the html file page by page
        homePageGenerator.writeHTMLPage(ReportHomePath,homePageGenerator.generateHTMLPage(ReportHomeTemplatePath, ReportHomePath));
        aboutFuzzerPageGenerator.writeHTMLPage(ReportAboutFuzzerPath,aboutFuzzerPageGenerator.generateHTMLPage(ReportAboutFuzzerTemplatePath, ReportAboutFuzzerPath));
        overviewAndConclusionsPageGenerator.writeHTMLPage(ReportOverviewPath,overviewAndConclusionsPageGenerator.generateHTMLPage(ReportOverviewTemplatePath, ReportOverviewPath));
        allMapsPageGenerator.writeHTMLPage(ReportAllMapsPath,allMapsPageGenerator.generateHTMLPage(ReportAllMapsTemplatePath, ReportAllMapsPath));
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
                List<String> row = parseCSVLine(line);
                if (isFirstLine) {
                    headers = row;
                    isFirstLine = false;
                    continue; // Skip the header line
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
     * @return The index of the column as int.
     */
    public static int getColumnIndex(String header) {
        return headers.indexOf(header);
    }

}
