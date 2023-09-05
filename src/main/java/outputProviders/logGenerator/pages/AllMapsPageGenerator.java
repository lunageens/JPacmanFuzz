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
     * The total amount of cards visible.
     */
    int sum;

    /**
     * Alters the template for this specific page.
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
        List<List<String>> exitCodeMinus1Rows = new ArrayList<>();
        for (List<String> row : rows) {
            int exitCode = Integer.parseInt(row.get(3));
            if (exitCode == -1) {
                exitCodeMinus1Rows.add(row);
            } else if (exitCode == 0) {
                exitCode0Rows.add(row);
            } else if (exitCode == 1) {
                exitCode1Rows.add(row);
            } else if (exitCode == 10) {
                exitCode10Rows.add(row);
            }
        }
        // Each separate exit code list should be sorted by run and iteration order.
        exitCode0Rows = sortExitCodeRows(exitCode0Rows);
        exitCode1Rows = sortExitCodeRows(exitCode1Rows);
        exitCode10Rows = sortExitCodeRows(exitCode10Rows);
        exitCodeMinus1Rows = sortExitCodeRows(exitCodeMinus1Rows);

        // * Step 2: Count exit codes and calculate exact and formatted percentages
        sum = rows.size();
        double exitCode0CountExactPercentage = (double) exitCode0Rows.size() / sum * 100;
        int exitCode0CountPercentage = (int) Math.round(exitCode0CountExactPercentage);
        double exitCode1CountExactPercentage = (double) exitCode1Rows.size() / sum * 100;
        int exitCode1CountPercentage = (int) Math.round(exitCode1CountExactPercentage);
        double exitCode10CountExactPercentage = (double) exitCode10Rows.size() / sum * 100;
        int exitCode10CountPercentage = (int) Math.round(exitCode10CountExactPercentage);
        double exitCodeMinus1CountExactPercentage = (double) exitCodeMinus1Rows.size() / sum * 100;
        int exitCodeMinus1CountPercentage = (int) Math.round(exitCodeMinus1CountExactPercentage);

        // * Step 3: Collect output messages of exit code 10 and -1
        // Keep count of each different output message and their different rows
        Set<String> outputMessages10 = getOutputMessages(exitCode10Rows);
        Map<String, List<List<String>>> outputMessages10Map = getOutputMessagesMap(exitCode10Rows);
        Set<String> outputMessagesMinus1 = getOutputMessages(exitCodeMinus1Rows);
        Map<String, List<List<String>>> outputMessagesMinus1Map = getOutputMessagesMap(exitCodeMinus1Rows);

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

        // * Step 4.2: Replace Exit Code 0 Section
        // * Replace the progress bar
        // <progress class="progress is-small is-info" value=0 max="100">0%</progress>
        String progressBar0 = "<progress id=\"ProgressBar0\" class=\"progress is-small is-info\" value=" + exitCode0CountPercentage + " max=\"100\">" + exitCode0CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBar0}}", progressBar0);
        // * Replace the cards
        String cards0 = buildCards(0, exitCode0Rows, exitCode0CountExactPercentage);
        template = template.replace("{{Cards0}}", cards0);

        // * Step 4.3: Replace Exit Code 1 Section (Idem)
        // * Replace the progress bar
        String progressBar1 = "<progress  id=\"ProgressBar1\" class=\"progress is-small is-info\" value=" + exitCode1CountPercentage + " max=\"100\">" + exitCode1CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBar1}}", progressBar1);
        // * Replace the cards
        String cards1 = buildCards(1, exitCode1Rows, exitCode1CountExactPercentage);
        template = template.replace("{{Cards1}}", cards1);

        // * Step 4.4 Replace Exit Code 10 Section (Also sorted per output message)
        // * Replace the progress bar
        String progressBar10 = "<progress id=\"ProgressBar10\"  class=\"progress is-small is-info\" value=" + exitCode10CountPercentage + " max=\"100\">" + exitCode10CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBar10}}", progressBar10);
        // * Replace the buttons
        String buttons10 = buildButtons(outputMessages10, exitCode10CountExactPercentage);
        template = template.replace("{{Buttons10}}", buttons10);
        // * Replace the cards and titles.
        String cards10 = buildCardsPerOutputMessage(10, exitCode10CountExactPercentage, outputMessages10, outputMessages10Map);
        template = template.replace("{{TitlesAndCards10}}", cards10);
        // * Replace the percentage output messages
        StringBuilder percentages = new StringBuilder();
        for (String message : outputMessages10) {
            String hrefMessage = "#" + getIDName(message);
            String idMessagePercentage = "PercentageTotal" + getIDName(message);
            // no value is needed, will be calculated in js script automatically
            percentages.append("<div class=\"level-item has-text-centered has-text-info\">");
            percentages.append("<div>");
            // <p class = "has-text-weight-bold"><a href = "#NoColumns">No columns </a></p>
            percentages.append("<p class=\"has-text-weight-bold\">").append("<a href=\"").append(hrefMessage).append("\">").append(message).append("</a></p>");
            percentages.append("<p class=\"title has-text-info-dark\" id=\"").append(idMessagePercentage).append("\"></p>");
            percentages.append("</div>");
            percentages.append("</div>");
        }
        String percentagesOutput = percentages.toString();
        template = template.replace("{{OutputMessagesPercentages}}", percentagesOutput);

        // * Step 4.5: Replace the Exit Code -1 Section (Also sorted per output message)
        // * Replace the progress bar
        String progressBarMinus1 = "<progress id=\"ProgressBar-1\" class=\"progress is-small is-info\" value=" + exitCodeMinus1CountPercentage + " max=\"100\">" + exitCodeMinus1CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBarMinus1}}", progressBarMinus1);
        // * Replace the buttons
        String buttonsMinus1 = buildButtons(outputMessagesMinus1, exitCodeMinus1CountExactPercentage);
        template = template.replace("{{ButtonsMinus1}}", buttonsMinus1);
        // * Replace the cards and titles.
        String cardsMinus1 = buildCardsPerOutputMessage(-1, exitCodeMinus1CountExactPercentage, outputMessagesMinus1, outputMessagesMinus1Map);
        template = template.replace("{{TitlesAndCardsMinus1}}", cardsMinus1);

        return template;
    }

    /**
     * Sorts the rows of a particular exit code first by run order, and sub sequentially by iteration order.
     * @param rows List of rows with the same exit code to sort
     * @return The list of rows in correct order.
     */
    private List<List<String>> sortExitCodeRows(List<List<String>> rows) {
        rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.get(2))));
        rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.get(0))));
        return rows;
    }

    /**
     * Makes a set of different output messages from the rows with the same exit code. A set is a collection with
     * unique elements.
     *
     * @param exitCodeRows
     *         List of rows with the same exit code
     *
     * @return Set of different output messages from the rows with the same exit code.
     */
    private Set<String> getOutputMessages(List<List<String>> exitCodeRows) {
        Set<String> outputMessages = new TreeSet<>();
        for (List<String> row : exitCodeRows) {
            String outputMessage = row.get(4);
            if (!outputMessage.trim().isEmpty()) {
                outputMessages.add(outputMessage);
            }
        }
        return outputMessages;
    }

    /**
     * Makes a map of each unique output message that occurs in the rows with the same exit code, linked to all the
     * rows with that exit code and that output message.
     * @param exitCodeRows List of rows with the same exit code
     * @return Map of each unique output message that occurs in the rows, linked to all the rows with that exit code and
     * that output message.
     */
    private Map<String, List<List<String>>> getOutputMessagesMap(List<List<String>> exitCodeRows) {
        Map<String, List<List<String>>> outputMessagesMap = new HashMap<>(); // Keep count of each different output message and their different rows
        for (List<String> row : exitCodeRows) {
            String outputMessage = row.get(4);
            if (!outputMessage.trim().isEmpty()) {
                outputMessagesMap.putIfAbsent(outputMessage, new ArrayList<>());
                outputMessagesMap.get(outputMessage).add(row);
            }
        }
        return outputMessagesMap;
    }

    /**
     * Builds the cards for each different input with the rows that have that output message. Also builds the
     * no cards message (hidden or displayed, depending on the amount of other cards).
     *
     * @param exitCode
     *         The shared exitcode of the rows.
     * @param exitCodeRows
     *         All the rows with that exit code.
     * @param exitCodeCountExactPercentage
     *         The exact percentage that represents the amount of rows with this exit code in
     *         comparison to all the rows.
     *
     * @return String with HTML code for the cards.
     */
    private String buildCards(int exitCode, List<List<String>> exitCodeRows, double exitCodeCountExactPercentage) {
        StringBuilder cardsBuilder = new StringBuilder();
        if (exitCodeCountExactPercentage > 0) {
            // * Add hidden no cards message
            String noCardsMessage = buildNoCardsMessage(false, exitCode);
            cardsBuilder.append(noCardsMessage);
            // * Add cards
            for (List<String> row : exitCodeRows) {
                String card = buildCard(row);
                cardsBuilder.append(card);
            }
        } else {
            // * Add displayed no cards message
            String noCardsMessage = buildNoCardsMessage(true, exitCode);
            cardsBuilder.append(noCardsMessage);
        }
        String cards = cardsBuilder.toString();
        return cards;
    }

    /**
     * Builds the cards for each different row with that exit code. Sorts these cards per output message and adds a
     * title and a progress bar for each unique output message.  Also builds the no cards message (hidden or displayed,
     * depending on the amount of other cards).
     *
     * @param exitCode
     *         The shared exitcode of the rows.
     * @param exitCodeCountExactPercentage
     *         The exact percentage that represents the amount of rows with this exit code
     *         in comparison to all the rows.
     * @param outputMessages
     *         Set of unique different output messages occurring in those rows.
     * @param outputMessagesMap
     *         Map that links each unique output message of that set to the rows that have that output
     *         message.
     *
     * @return String with HTML code for the cards, progressbars and titles.
     */
    private String buildCardsPerOutputMessage(int exitCode, double exitCodeCountExactPercentage, Set<String> outputMessages,
                                              Map<String, List<List<String>>> outputMessagesMap) {
        StringBuilder cardsBuilder = new StringBuilder();
        // if there are any exit code rows, make buttons, hide no cards message, make section per output message
        if (exitCodeCountExactPercentage > 0) {
            // * Add hidden no cards message
            String noCardsMessage = buildNoCardsMessage(false, exitCode);
            cardsBuilder.append(noCardsMessage);

            // * Per output message: replace title + progress bar and add cards
            for (String message : outputMessages) {
                // Begin full section
                List<List<String>> rowsWithMessage = outputMessagesMap.get(message);
                String idMessage = getIDName(message);
                cardsBuilder.append("<div id=\"Total").append(idMessage).append("\" class=\"field\">");
                // Subtitle and progress bar
                cardsBuilder.append("<br>");
                cardsBuilder.append("<section id=\"").append(idMessage).append("\" class=\"section\">");
                int count = rowsWithMessage.size();
                int percentage = (int) Math.round((double) count / sum * 100);
                String progressBar = "<progress id=\"ProgressBar" + idMessage + "\" class=\"progress is-small is-link\" value=" + percentage + " max=\"100\">" + percentage + "%</progress>";
                cardsBuilder.append(progressBar);
                cardsBuilder.append("<h2 class=\"subtitle\">Inputs with output message <a class=\"has-text-weight-bold\">").append(message).append("</a>:</h2>");
                cardsBuilder.append("</section>");
                // Add Cards
                for (List<String> row : rowsWithMessage) {
                    String card = buildCard(row);
                    cardsBuilder.append(card);
                }
                cardsBuilder.append("</div>");
            }
        } else { // If there are not on display, no cards message
            String noCardsMessage = buildNoCardsMessage(true, exitCode);
            cardsBuilder.append(noCardsMessage);
        }
        String cards = cardsBuilder.toString();
        return cards;
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
    private String buildCard(List<String> row) {
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
    private String buildNoCardsMessage(boolean displayed, int exitCode) {
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

    /**
     * Given a set of different output messages, returns a string that describes the HTML code needed for buttons
     * of this output message. If there are no outputMessages, returns an empty string.
     *
     * @param outputMessages
     *         Set of unique output-messages in string form.
     *
     * @return String with HTML Code for the buttons.
     */
    private String buildButtons(Set<String> outputMessages, double exitCodeCountExactPercentage) {
        String buttons;
        // * Replace buttons output messages above with buttons if there are any needed
        if (exitCodeCountExactPercentage > 0) {
            StringBuilder buttonBuilder = new StringBuilder();
            for (String outputMessage : outputMessages) {
                String button = buildButton(outputMessage);
                buttonBuilder.append(button);
            }
            buttons = buttonBuilder.toString();
        } else {
            buttons = "";
        }
        return buttons;
    }

    /**
     * Generates HTML code for buttons
     *
     * @param outputMessage
     *         Output message of the button (e.g. "Widths Mismatch")
     *
     * @return String HTML Code for the button with that text
     */
    /*
       <div class="navbar-item is-small px-0"> <!-- Enkel bij de eerste px-0? Sorteer alfabetisch -->
                   <div class="button is-info is-rounded mx-1">
                       <a class="has-text-white has-text-weight-bold " href="#WidthsMismatch">Widths Mismatch</a>
                   </div>
               </div>
        */
    private String buildButton(String outputMessage) {
        String idSection = getIDName(outputMessage);
        String idButton = getIDName(outputMessage) + "Button";
        String s = "<div id=\"" + idButton + "\" class=\"navbar-item is-small px-0\">" +
                "<div class=\"button is-info is-rounded mx-1\">" +
                "<a class=\"has-text-white has-text-weight-bold \" href=\"#" + idSection + "\">" + outputMessage + "</a>" +
                "</div>" +
                "</div>";
        return s;
    }

}