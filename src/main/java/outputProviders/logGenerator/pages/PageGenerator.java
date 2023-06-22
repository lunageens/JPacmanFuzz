package outputProviders.logGenerator.pages;

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
    default String generateHTMLPage(String templatePath, String destinationPath) {
        String alteredTemplate = alterHTMLTemplate(templatePath);
        writeHTMLPage(destinationPath, alteredTemplate);
        return alteredTemplate;
    }


    /**
     * Alter the HTML template of the page appropriate for the specific implementation.
     * @param templatePath The path to the template file.
     * @return String The altered HTML template, as String (after read in bytes and altered).
     */
    String alterHTMLTemplate(String templatePath);

    /**
     * Read the template HTML file to a byte string, ready for modification.
     * @param templatePathLocation The path to the template file.
     * @return String The template HTML file as a byte string, ready for modification.
     */
    default String readTemplate(String templatePathLocation) {
        String template;
        try {
            Path templatePath = Paths.get(templatePathLocation);
            template = Files.readString(templatePath); // Changed from readAllBytes
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
   default void writeHTMLPage(String destinationPath, String alteredTemplate) {
        try {
            Files.writeString(Paths.get(destinationPath), alteredTemplate); // Changed from write(Paths.get(destinationPath), alteredTemplate.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
