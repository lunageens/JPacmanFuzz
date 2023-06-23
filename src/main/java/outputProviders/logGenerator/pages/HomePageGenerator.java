package outputProviders.logGenerator.pages;

import outputProviders.logGenerator.LogHTMLFileHandler;

import java.util.List;

import static outputProviders.logInputter.IterationResultFormatter.*;

/**
 *  Generates the home page.
 */
public class HomePageGenerator extends LogHTMLFileHandler implements PageGenerator {

    /**
     * Column in the CSV that has the exit code.
     */
    static int exitCodeColumnIndex = 0;

    /**
     * Column in the CSV that has the map file type.
     */
    static int mapFileTypeColumnIndex = 0;

    /**
     * Column in the CSV that has the map file custom attribute.
     */
    static int mapFileCustomAttributeColumnIndex = 0;

    /**
     * Column in the CSV that has the absolute map file path.
     */
    static int absoluteMapFilePathColumnIndex = 0;

    /**
     * Column in the CSV that has the relative map file path.
     */
    static int relativeMapFilePathColumnIndex = 0;

    /**
     * Constructor of home page.
     */
    public HomePageGenerator() {
        super();
        exitCodeColumnIndex = getColumnIndex("Exit Code");
        mapFileTypeColumnIndex = getColumnIndex("Map File Type");
        mapFileCustomAttributeColumnIndex = getColumnIndex("Map File Custom Attribute");
        absoluteMapFilePathColumnIndex = getColumnIndex("Absolute Map File Path");
        relativeMapFilePathColumnIndex = getColumnIndex("Relative Map File Path");
    }

    /**
     * Alters the html template of the home page class. We replace filters, headers and data in the table with the
     * appropriate values.
     * @param templatePathLocation The path to the template file.
     * @return The altered template as one long string.
     */
    // * For " in string write \"
    public String alterHTMLTemplate(String templatePathLocation) {

        String template = readTemplate(templatePathLocation);

        // * Generate the filters
        StringBuilder filtersBuilder = new StringBuilder();
        for (String header : headers) {
            List<String> uniqueValues = getUniqueValues(headers.indexOf(header));
            String idVar = getIDName(header);

            // Top part
            /*
            <div id="FuzzAttemptNrFilter" class="level-item mx-2 my-2">  id name + filter
            <div class="field">
            <label class="label has-text-info">Fuzz Attempt Number </label>  name in table
            <div class="select is-info is-rounded">
            <label>
            <select class="is-hovered filter" data-filter-target="FuzzAttemptNr"> id name
             */
            // ? Did not add space to header - I don't know if that matters
            // ! Check if correct value of date and time and timestamp header
            // Skip timeStamp as filter but not in table, make this hidden not delete (otherwise filters will not work)
            // Skip relative path in both table and filter, make this hidden not delete (otherwise filters will not work)
            if ((header.equals("Date and Time")) || (header.equals("Relative Map File Path"))) {
                filtersBuilder.append("<div id=\"").append(idVar).append("Filter").append("\" class=\"level-item mx-2 my-2\" style=\"display:none\">");
            } else {
                filtersBuilder.append("<div id=\"").append(idVar).append("Filter").append("\" class=\"level-item mx-2 my-2\">");
            }
            filtersBuilder.append("<div class=\"field\">");
            filtersBuilder.append("<label class=\"label has-text-info\">").append(header).append("</label>");
            filtersBuilder.append("<div class=\"select is-info is-rounded\">");
            filtersBuilder.append("<label>");
            filtersBuilder.append("<select class=\"is-hovered filter\" data-filter-target=\"").append(idVar).append("\">");

            // Values part
            /*
            <option value="">All</option>
            <option value="1">1</option>
            <option value="2">2</option>
             */
            StringBuilder optionsBuilder = new StringBuilder();
            optionsBuilder.append("<option value=\"\">All</option>");
            for (String value : uniqueValues) {
                switch (header) {
                    case "Exit Code" ->  // Change format exitCode with full text in filters and table
                            value = getFormattedErrorCode(Integer.parseInt(value), true, false, false, false, false);
                    case "Map File Type" ->  // Change format map file type to one word only in filters and table
                        // from ".txt [Unformatted Text Document]" to "Unformatted Text Document"
                        // ? I don't know if this will work, without space or not?
                            value = getFormattedMapFileType(extractFileTypeExtension(value), false, true, true, false, false, false, false, false, false);
                }
                optionsBuilder.append("<option value=\"").append(value).append("\">").append(value).append("</option>");
            }
            String options = optionsBuilder.toString();
            filtersBuilder.append(options);

            // Bottom part
            /*
            </select>
            </label>
            </div>
            </div>
             */
            filtersBuilder.append("</select>");
            filtersBuilder.append("</label>");
            filtersBuilder.append("</div>");
            filtersBuilder.append("</div>");
            filtersBuilder.append("</div>");

        }
        String filters = filtersBuilder.toString();
        template = template.replace("{{Filters}}", filters);

        // * Generate the data table headers
        // ! Skip relative path
        /*   <th class="sorting has-text-white">FuzzAttemptNr</th> */
        StringBuilder tableHeadersBuilder = new StringBuilder();
        for (String header : headers) {
            if (!header.equals("Relative Map File Path")) {
                tableHeadersBuilder.append("<th class=\"sorting has-text-white\">").append(header).append("</th>");
            }
        }
        String headers = tableHeadersBuilder.toString();
        template = template.replace("{{Headers}}", headers);

        // * Generate the data table results
        // Change to same format for exit code, map file type, and map file custom attribute
        // Be careful with format absolute
        // Be careful with format relative
        /*
        <tr>
        <td>1</td>
        <td>11-06-2023 20:25</td>
        <td>1</td>
        <td>10</td>
        <td>Widths mismatch</td>
        <td>SWE</td>
        <td>map_1.txt</td>
        <td>TXT</td>
        <td>MW</td>
        <td class="is-link has-background-grey-lighter">
         <a href="allMaps.html#collapsible-card1">C:\ST\JPacmanFuzz\fuzzresults\maps\previous_maps\run_1\exitcode10_rejected\map_1.txt</a> take id in rows
         </td>
         </tr>
         */
        StringBuilder tableBuilder = new StringBuilder();
        for (List<String> row : rows) {
            tableBuilder.append("<tr>");
            int columnIndex = 0;
            for (String cell : row) {
                int rowIndex = getRowIndex(row);
                if (columnIndex == exitCodeColumnIndex) { // Change format exitCode with full text in filters and table
                    cell = getFormattedErrorCode(Integer.parseInt(cell), true, false, false, false, false);
                    tableBuilder.append("<td>").append(cell).append("</td>");
                } else if (columnIndex == mapFileTypeColumnIndex) { // Change format map file type to one word only in filters and table
                    cell = getFormattedMapFileType(extractFileTypeExtension(cell), false, true, true, false, false, false, false, false, false);
                    tableBuilder.append("<td>").append(cell).append("</td>");
                } else if (columnIndex == absoluteMapFilePathColumnIndex) { // Add link
                    tableBuilder.append("<td class=\"is-link has-background-grey-lighter\">");
                    tableBuilder.append("<a href=\"allMaps.html#collapsible-card").append(rowIndex).append("\" >").append(cell).append(" </a>");
                    tableBuilder.append("</td>");
                } else if (columnIndex != relativeMapFilePathColumnIndex) { // do nothing for relative, we don't want that in the table.
                    tableBuilder.append("<td>").append(cell).append("</td>");
                }
                columnIndex++;
            }
            tableBuilder.append("</tr>");
        }
        String dataTable = tableBuilder.toString();
        template = template.replace("{{DataTable}}", dataTable);

        return template;
    }




}
