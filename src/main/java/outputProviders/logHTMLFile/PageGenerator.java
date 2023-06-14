package outputProviders.logHTMLFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Interface for generating an HTML page.
 */
public interface PageGenerator {

    /**
     * Generates the  HTML page from the template and writes it to a file by calling the appropriate methods.
     * @param templatePath The path to the template file.
     * @param destinationPath The path to the file to write the HTML to.
     * @return String The altered HTML template, as String (after read in bytes and altered).
     */
    public default String generateHTMLPage(String templatePath, String destinationPath) {
        String alteredTemplate = alterHTMLTemplate(templatePath);
        writeHTMLPage(destinationPath, alteredTemplate);
        return alteredTemplate;
    }


    /**
     * Alter the HTML template of the page appropriate for the specific implementation.
     * @param templatePath The path to the template file.
     * @return String The altered HTML template, as String (after read in bytes and altered).
     */
    public abstract String alterHTMLTemplate(String templatePath);

    /**
     * Read the template HTML file to a byte string, ready for modification.
     * @param templatePathLocation The path to the template file.
     * @return String The template HTML file as a byte string, ready for modification.
     */
    public default String readTemplate(String templatePathLocation) {
        String template;
        try {
            Path templatePath = Paths.get(templatePathLocation);
            template = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return template;
    }

    /**
     * Write the generated HTML to a file.
     * @param destinationPath The path to the file to write the HTML to.
     * @param alteredTemplate The altered HTML template, as String (after read in bytes and altered).
     */
    public default void writeHTMLPage(String destinationPath, String alteredTemplate) {
        try { // TODO nog aanpassen path
            Files.write(Paths.get(destinationPath), alteredTemplate.getBytes(StandardCharsets.UTF_8));
            System.out.println("Report generated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
