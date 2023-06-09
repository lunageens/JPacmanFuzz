package outputProviders.logGenerator.pages;

import outputProviders.logGenerator.LogHTMLFileHandler;

/**
 * Generates the AboutFuzzerPage.
 */
public class AboutFuzzerPageGenerator extends LogHTMLFileHandler implements PageGenerator {

    /**
     * Alters the template for this specific page.
     * Currently, this method does nothing.
     * It is left here in case we want to add functionality in the future.
     *
     * @param templatePath The path to the template.
     * @return The altered template.
     */
    @Override
    public String alterHTMLTemplate(String templatePath) {
        String template = readTemplate(templatePath);
        // * Nothing to change here
        return template;
    }

}
