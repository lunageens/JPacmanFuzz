package outputProviders.logHTMLFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static outputProviders.logHTMLFile.LogHTMLFileHandler.*;

/**
 *  Generates the home page.
 */
public class HomePageGenerator extends LogHTMLFileHandler implements PageGenerator  {

    public String alterHTMLTemplate(String templatePathLocation) {

        String template = readTemplate(templatePathLocation);

        // Replace title and introduction text
        template = template.replace("{{Title}}", "Jpacman Fuzzing report");
        template = template.replace("{{IntroductionText}}", "This report provides an overview of the Jpacman fuzzing results.");

        // Generate the search box
        String searchBox = "<input type=\"text\" id=\"search-bar\" placeholder=\"Search...\">";
        template = template.replace("{{SearchBox}}", searchBox);

        // Generate the filters
        StringBuilder filtersBuilder = new StringBuilder();
        for (String header : headers) {
            List<String> uniqueValues = getUniqueValues(headers.indexOf(header));
            StringBuilder optionsBuilder = new StringBuilder();
            optionsBuilder.append("<option value=\"\">All</option>");
            for (String value : uniqueValues) {
                optionsBuilder.append("<option value=\"").append(value).append("\">").append(value).append("</option>");
            }
            String filter = "<select class=\"form-select filter\" data-filter-name=\"" + header + "\">" + optionsBuilder.toString() + "</select>";
            filtersBuilder.append("<div class=\"filter-group\">");
            filtersBuilder.append("<label>").append(header).append("</label>");
            filtersBuilder.append(filter);
            filtersBuilder.append("</div>");
        }
        String filters = filtersBuilder.toString();
        template = template.replace("{{Filters}}", filters);

        // Generate the data table
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append("<div class =\"table-responsive\">");
        tableBuilder.append("<table class=\"table table-sm !important\">");
        tableBuilder.append("<thead class = \"thead-dark !important\"><tr>");
        for (String header : headers) {
            tableBuilder.append("<th>").append(header).append("</th>");
        }
        tableBuilder.append("</tr></thead>");
        tableBuilder.append("<tbody>");
        for (List<String> row : rows) {
            tableBuilder.append("<tr>");
            for (String cell : row) {
                tableBuilder.append("<td>").append(cell).append("</td>");
            }
            tableBuilder.append("</tr>");
        }
        tableBuilder.append("</tbody>");
        tableBuilder.append("</table>");
        tableBuilder.append("</div>");
        String dataTable = tableBuilder.toString();
        template = template.replace("{{DataTable}}", dataTable);

        return template;
    }

}
