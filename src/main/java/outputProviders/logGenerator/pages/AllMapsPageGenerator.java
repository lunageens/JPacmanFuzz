package outputProviders.logGenerator.pages;

import organizers.FileHandler;
import outputProviders.logGenerator.LogHTMLFileHandler;
import outputProviders.logInputter.IterationResultFormatter;

import java.util.*;

/**
 * Generates the AllMapsPage.
 */
public class AllMapsPageGenerator extends LogHTMLFileHandler implements PageGenerator {

    /**
     * Alters the template for this specific page.
     * Currently, this method does nothing.
     * It is left here in case we want to add functionality in the future.
     *
     * @param templatePath
     *         The path to the template.
     *
     * @return The altered template.
     */
    @Override
    public String alterHTMLTemplate(String templatePath) {
        String template = readTemplate(templatePath);

        // * Step 1: Filter and sort by exit code
        // Sort per error message (in 0, 1, 10 and -1 - don't use -1) and then per output message (in run order).
        List<List<String>> exitCode0Rows = new ArrayList<>();
        List<List<String>> exitCode1Rows = new ArrayList<>();
        List<List<String>> exitCode10Rows = new ArrayList<>();
        for (List<String> row : rows) {
            int exitCode = Integer.parseInt(row.get(3));
            if (exitCode == -1) {
                continue;
            } // Disregard rows with exit code -1
            if (exitCode == 0) {
                exitCode0Rows.add(row);
            } else if (exitCode == 1) {
                exitCode1Rows.add(row);
            } else if (exitCode == 10) {
                exitCode10Rows.add(row);
            }
        }
        // Each separate exit code list should be sorted by run and iteration order.
        exitCode0Rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.get(2))));
        exitCode0Rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.get(0))));
        exitCode1Rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.get(2))));
        exitCode1Rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.get(0))));
        exitCode10Rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.get(2))));
        exitCode10Rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.get(0))));

        // * Step 2: Count exit codes and calculate percentages
        int sum = rows.size(); // ! Also unknown!
        int exitCode0CountPercentage = (int) Math.round((double) exitCode0Rows.size() / sum * 100);
        int exitCode1CountPercentage = (int) Math.round((double) exitCode1Rows.size() / sum * 100);
        int exitCode10CountPercentage = (int) Math.round((double) exitCode10Rows.size() / sum * 100);

        // * Step 3: Collect output messages
        Set<String> outputMessages = new TreeSet<>();
        Map<String, List<List<String>>> outputMessagesMap = new HashMap<>(); // Keep count of each different output message and their different rows
        for (List<String> row : exitCode10Rows) {
            String outputMessage = row.get(4);
            if (!outputMessage.trim().isEmpty()) {
                outputMessages.add(outputMessage);
                outputMessagesMap.putIfAbsent(outputMessage, new ArrayList<>());
                outputMessagesMap.get(outputMessage).add(row);
            }
        }

        // * Step 4: Alter the template
        // * Step 4.1: Replace the filter options with the different fuzz-attempts in the top section
        List<String> fuzzAttempts = getUniqueValues(0);
        StringBuilder filterOptions = new StringBuilder();
        for (String fuzzAttempt : fuzzAttempts) {
            String filterOption = "<option value=\"" + fuzzAttempt + "\">" + fuzzAttempt + "</option>";
            filterOptions.append(filterOption);
        }
        String filterOptionsString = filterOptions.toString();
        template = template.replace("{{FuzzAttemptFilterOptions}}", filterOptionsString);

        // * Step 4.2: Replace percentage unknown
        int exitCodeMinus1CountPercentage = (int) Math.round((double) (rows.size() - exitCode0Rows.size() - exitCode1Rows.size() - exitCode10Rows.size()) / sum * 100);
        String percentageUnknown = exitCodeMinus1CountPercentage + "%";
        // ! dit klopt niet want niet aangepast en zelfs als het klopt hier zal het bij gefiltered neit meer kloppen
        template = template.replace("{{PercentageUnknown}}", percentageUnknown);

        // * Step 4.3: Replace Exit Code 0 Section
        // * Replace the progress bar
        // <progress class="progress is-small is-info" value=0 max="100">0%</progress>
        String progressBar0 = "<progress id=\"ProgressBar0\" class=\"progress is-small is-info\" value=" + exitCode0CountPercentage + " max=\"100\">" + exitCode0CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBar0}}", progressBar0);
        // * Replace the cards
        StringBuilder cardsBuilder0 = new StringBuilder();
        if (exitCode0CountPercentage > 0) {
            String noCardsMessage = getNoCardsMessage(false, 0);
            cardsBuilder0.append(noCardsMessage);
            for (List<String> row : exitCode0Rows) {
                String card = generateCard(row);
                cardsBuilder0.append(card);
            }
        } else {
            String noCardsMessage = getNoCardsMessage(true, 0);
            cardsBuilder0.append(noCardsMessage);
        }
        String cards = cardsBuilder0.toString();
        template = template.replace("{{Cards0}}", cards);

        // * Step 4.4: Replace Exit Code 1 Section (Idem)
        // * Replace the progress bar
        String progressBar1 = "<progress  id=\"ProgressBar1\" class=\"progress is-small is-info\" value=" + exitCode1CountPercentage + " max=\"100\">" + exitCode1CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBar1}}", progressBar1);
        // * Replace the cards
        StringBuilder cardsBuilder1 = new StringBuilder();
        if (exitCode1CountPercentage > 0) {
            String noCardsMessage = getNoCardsMessage(false, 1);
            cardsBuilder0.append(noCardsMessage);
            for (List<String> row : exitCode1Rows) {
                String card = generateCard(row);
                cardsBuilder1.append(card);
            }
        } else {
            String noCardsMessage = getNoCardsMessage(true, 1);
            cardsBuilder1.append(noCardsMessage);
        }
        cards = cardsBuilder1.toString();
        template = template.replace("{{Cards1}}", cards);

        // * Step 4.5 Replace Exit Code 10 Section (Also sorted per output message)
        // * Replace the progress bar
        String progressBar10 = "<progress id=\"ProgressBar10\"  class=\"progress is-small is-info\" value=" + exitCode10CountPercentage + " max=\"100\">" + exitCode10CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBar10}}", progressBar10);
        StringBuilder cardsBuilder10 = new StringBuilder();
        if (exitCode10CountPercentage > 0) { // if there are exit code 10s, make buttons, hide no cards message, make section per output message
            // * Replace buttons output messages above
            StringBuilder buttonBuilder = new StringBuilder();
            for (String outputMessage : outputMessages) {
                String button = generateButton(outputMessage);
                buttonBuilder.append(button);
            }
            String buttons = buttonBuilder.toString();
            template = template.replace("{{Buttons10}}", buttons);
            // * Add hidden no cards message
            String noCardsMessage = getNoCardsMessage(false, 10);
            cardsBuilder10.append(noCardsMessage);
            // * Per output message - replace title + progress bar and cards
            for (String message : outputMessages) {
                List<List<String>> rowsWithMessage = outputMessagesMap.get(message);
                String idMessage = getIDName(message);
                cardsBuilder10.append("<div id=\"Total").append(idMessage).append("\" class=\"field\">");
                // * Subtitle and progress bar
                cardsBuilder10.append("<br>");
                cardsBuilder10.append("<section id=\"").append(idMessage).append("\" class=\"section\">");
                int count = rowsWithMessage.size();
                int percentage = (int) Math.round((double) count / sum * 100);
                String progressBar = "<progress id=\"ProgressBar" + idMessage + "\" class=\"progress is-small is-link\" value=" + percentage + " max=\"100\">" + percentage + "%</progress>";
                cardsBuilder10.append(progressBar);
                cardsBuilder10.append("<h2 class=\"subtitle\">Inputs with output message <a class=\"has-text-weight-bold\">").append(message).append("</a>:</h2>");
                cardsBuilder10.append("</section>");
                // * Cards
                for (List<String> row : rowsWithMessage) {
                    String card = generateCard(row);
                    cardsBuilder10.append(card);
                }
                cardsBuilder10.append("</div>");
            }
        } else { // If there are not on display no cards message
            String noCardsMessage = getNoCardsMessage(true, 10);
            cardsBuilder10.append(noCardsMessage);
        }
        String cards10 = cardsBuilder10.toString();
        template = template.replace("{{TitlesAndCards10}}", cards10);

        return template;
    }

    /**
     * Generates HTML code for buttons
     * @param outputMessage Output message of the button (e.g. "Widths Mismatch")
     * @return String HTML Code for the button with that text
     */
    /*
       <div class="navbar-item is-small px-0"> <!-- Enkel bij de eerste px-0? Sorteer alfabetisch -->
                   <div class="button is-info is-rounded mx-1">
                       <a class="has-text-white has-text-weight-bold " href="#WidthsMismatch">Widths Mismatch</a>
                   </div>
               </div>
        */
    private String generateButton(String outputMessage) {
        String idSection = getIDName(outputMessage);
        String idButton = getIDName(outputMessage) + "Button";
        String s = "<div id=\"" + idButton + "\" class=\"navbar-item is-small px-0\">" +
                "<div class=\"button is-info is-rounded mx-1\">" +
                "<a class=\"has-text-white has-text-weight-bold \" href=\"#" + idSection + "\">" + outputMessage + "</a>" +
                "</div>" +
                "</div>";
        return s;
    }


    /**
     * Generates a card from a row
     *
     * @param row
     *         Row in the data
     *
     * @return String containing the HTML card
     */
    /*
    <br>
      <div class="card has-text-info">
          <header class="card-header has-text-white">
              <p class="card-header-title">Run 1: map_1.txt</p>
              <a href="#collapsible-card1" data-action="collapse" data-target="collapsible-card1" class="card-header-icon" aria-label="more options">
          <span class="icon is-white is-clickable">
              <i class="fas fa-angle-down " ></i>
              <i class="fas fa-angle-up is-hidden"></i>
          </span>
              </a>
          </header>
          <div id="collapsible-card1" class="is-collapsible">
              <div class="card-content">
                  <p><a class="has-text-weight-bold">The action sequence: </a></p><p>SWE</p><br>
                  <p><a class="has-text-weight-bold">The map:</a></p>
                  <br><div class="box"> <!-- Let the ID Be the relative path! -->
                      <div id="/fuzzresults/maps/previous_maps/run_1/exitcode0_accepted/map_1.txt" class="container"></div>
                  </div>
              </div>
              <footer class="card-footer">
                  <p class="card-footer-item is-small has-text-info level-left">
                  <span>
                      <strong>Absolute file path:</strong>
                      C:/ST/JPacmanFuzz/fuzzresults/maps/previous_maps/run_1/exitcode10_rejected/map_1.txt
              </span>
                  </p>
              </footer>
          </div>
      </div>
       */
    private String generateCard(List<String> row) {
        StringBuilder card = new StringBuilder();
        card.append("<div class=\"field\">");
        card.append("<br>");
        card.append("<div class=\"card has-text-info\">");
        // Card header title
        card.append("<header class=\"card-header has-text-white\">");
        int fuzzAttemptNr = Integer.parseInt(row.get(0));
        String mapFileName = row.get(6);
        card.append("<p class=\"card-header-title\">Fuzz Attempt  ").append(fuzzAttemptNr).append(": ").append(mapFileName).append("</p>");
        // Card header icons
        int initialIndex = getRowIndex(row); // Retrieve the original index
        card.append("<a href=\"#collapsible-card").append(initialIndex).append("\" data-action=\"collapse\" data-target=\"collapsible-card").append(initialIndex).append("\" class=\"card-header-icon\" aria-label=\"more options\">");
        card.append("<span class=\"icon is-white is-clickable\">");
        card.append("<i class=\"fas fa-angle-down \" ></i>");
        card.append("<i class=\"fas fa-angle-up is-hidden\"></i>");
        card.append("</span>");
        card.append("</a>");
        card.append("</header>");
        // Card content action sequence
        card.append("<div id=\"collapsible-card").append(initialIndex).append("\" class=\"is-collapsible\">");
        card.append("<div class=\"card-content\">");
        String actionSequence = row.get(5); // ! Alter this if needed different format
        card.append("<p><a class=\"has-text-weight-bold\">The action sequence: </a></p><p>").append(actionSequence).append("</p><br>");
        card.append("<p><a class=\"has-text-weight-bold\">The map:</a></p>");
        // Card content map
        // Will be replaced in js script.
        card.append("<br><div class=\"box\">");
        String mapPath = row.get(9); // ! Alter this if different format needed
        String absoluteMapPath = FileHandler.normalizeFilePath(mapPath);
        String relativeMapPath = "/" + IterationResultFormatter.getFormattedMapFilePath(absoluteMapPath, false, true, false, true, false, false);
        card.append("<div id=\"").append(relativeMapPath).append("\" class=\"container\"></div>");  // ? Correct path? Yes, needed to append / for it to work in HTML
        card.append("</div>");
        card.append("</div>");
        // Card footer
        card.append("<footer class=\"card-footer\">");
        card.append("<p class=\"card-footer-item is-small has-text-info level-left\">");
        card.append("<span>");
        card.append("<strong>Absolute file path: </strong>");
        card.append(absoluteMapPath);
        card.append("</span>");
        card.append("</p>");
        card.append("</footer>");
        // Closing tags
        card.append("</div>");
        card.append("</div>");
        card.append("</div>");

        return card.toString();
    }

    /**
     * Generates the message to display when there are no cards
     *
     * @return String containing the HTML message
     */
    /*
     <br>
        <div class="card has-background-info-light is-spaced is-align-content-center pl-4 pr-4 pt-1 pb-4">
            <br>
            <button class="delete is-pulled-right is-small"></button>
            <span>
                    There were no inputs that had this exit code.
            </span>
            <br>
        </div>
     */
    private String getNoCardsMessage(boolean displayed, int exitCode) {
        String display = "";
        if (!displayed) {
            display = " style=\"display:none;\"";
        }
        String s = "<br>" +
                "<div " +
                "id=\"ExitCode" + exitCode + "NoCards" + "\"" +
                display +
                " class=\"card has-background-info-light is-spaced is-align-content-center pl-4 pr-4 pt-1 pb-4\">" +
                "<br>" +
                "<button class=\"delete is-pulled-right is-small\"></button>" +
                "<span>" +
                "There were no inputs that had this exit code." +
                "</span>" +
                "<br>" +
                "</div>";
        return s;
    }

}