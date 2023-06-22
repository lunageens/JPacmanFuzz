package outputProviders.logGenerator.pages;

import organizers.FileHandler;
import outputProviders.logGenerator.LogHTMLFileHandler;
import outputProviders.logInputter.IterationResultFormatter;

import java.util.*;

/**
 * Generates the AllMapsPage.
 */
public class AllMapsPageGenerator extends LogHTMLFileHandler implements PageGenerator {

    private Map<List<String>, Integer> rowIndexMap;

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

        /* ! Sort per error message (in 0, 1, 10 and -1 - don't use -1) and then per output message (in run order). */
        // * Step 1: Filter and sort by exit code
        List<List<String>> exitCode0Rows = new ArrayList<>();
        List<List<String>> exitCode1Rows = new ArrayList<>();
        List<List<String>> exitCode10Rows = new ArrayList<>();
        rowIndexMap = new HashMap<>(); // Map to store the original index of each row
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            int exitCode = Integer.parseInt(row.get(3));
            if (exitCode == -1) {
                continue;
            } // Disregard rows with exit code -1
            rowIndexMap.put(row, i); // Store the original index of the row
            if (exitCode == 0) {
                exitCode0Rows.add(row);
            } else if (exitCode == 1) {
                exitCode1Rows.add(row);
            } else if (exitCode == 10) {
                exitCode10Rows.add(row);
            }
        }
        // Each separate exit code list should be sorted by run and iteration order.
        Collections.sort(exitCode0Rows, Comparator.comparingInt(row -> Integer.parseInt(row.get(0))));
        Collections.sort(exitCode0Rows, Comparator.comparingInt(row -> Integer.parseInt(row.get(2))));
        Collections.sort(exitCode1Rows, Comparator.comparingInt(row -> Integer.parseInt(row.get(0))));
        Collections.sort(exitCode1Rows, Comparator.comparingInt(row -> Integer.parseInt(row.get(2))));
        Collections.sort(exitCode10Rows, Comparator.comparingInt(row -> Integer.parseInt(row.get(0))));
        Collections.sort(exitCode10Rows, Comparator.comparingInt(row -> Integer.parseInt(row.get(2))));

        // * Step 2: Count exit codes and calculate percentages
        int sum = rows.size(); // ! Also unknown!
        int exitCode0CountPercentage=  ( int) Math.round((double) exitCode0Rows.size() / sum * 100);
        int exitCode1CountPercentage =  ( int) Math.round((double) exitCode1Rows.size() / sum * 100);
        int exitCode10CountPercentage = ( int) Math.round((double) exitCode10Rows.size() / sum * 100);

        // * Step 3: Collect output messages
        Set<String> outputMessages = new TreeSet<>();
        Map<String, List<List<String>>> outputMessagesMap = new HashMap<>();
        for (List<String> row : exitCode10Rows) {
            String outputMessage = row.get(4);
            if (!outputMessage.trim().isEmpty()) {
                outputMessages.add(outputMessage);
                outputMessagesMap.putIfAbsent(outputMessage, new ArrayList<>());
                outputMessagesMap.get(outputMessage).add(row);
            }
        }

        // ! Exit Code 0
        // * Replace the progress bar
        /*
        <progress class="progress is-small is-info" value=0 max="100">0%</progress>
         */
        String progressBar0 = "<progress class=\"progress is-small is-info\" value=" + exitCode0CountPercentage + " max=\"100\">" + exitCode0CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBar0}}", progressBar0);
        // * Replace the cards
        StringBuilder cardsBuilder0 = new StringBuilder();
        if (exitCode0CountPercentage > 0) {
            for (List<String> row : exitCode0Rows) {
                String card = generateCard(row);
                cardsBuilder0.append(card);
            }
        } else {
            String noCardsMessage = getNoCardsMessage();
            cardsBuilder0.append(noCardsMessage);
        }
        String cards = cardsBuilder0.toString();
        template = template.replace("{{Cards0}}", cards);

        // ! Exit Code 1 (idem)
        // * Replace the progress bar
        String progressBar1 = "<progress class=\"progress is-small is-info\" value=" + exitCode1CountPercentage + " max=\"100\">" + exitCode1CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBar1}}", progressBar1);
        // * Replace the cards
        StringBuilder cardsBuilder1 = new StringBuilder();
        if (exitCode1CountPercentage > 0) {
            for (List<String> row : exitCode1Rows) {
                String card = generateCard(row);
                cardsBuilder1.append(card);
            }
        } else {
            String noCardsMessage = getNoCardsMessage();
            cardsBuilder1.append(noCardsMessage);
        }
        cards = cardsBuilder1.toString();
        template = template.replace("{{Cards1}}", cards);

        // ! Exit Code 10 (also sorted per output message)
        // * Replace the progress bar
        String progressBar10 = "<progress class=\"progress is-small is-info\" value=" + exitCode10CountPercentage + " max=\"100\">" + exitCode10CountPercentage + "%</progress>";
        template = template.replace("{{ProgressBar10}}", progressBar10);

        StringBuilder cardsBuilder10 = new StringBuilder();
        if (exitCode10CountPercentage > 0) {
            // * Replace buttons output messages
            StringBuilder buttonBuilder = new StringBuilder();
            for (String outputMessage : outputMessages) {
                String button = generateButton(outputMessage);
                buttonBuilder.append(button);
            }
            String buttons =  buttonBuilder.toString();
            template = template.replace("{{Buttons10}}", buttons);

            // * Per output message - replace title and cards
            for (String message : outputMessages) {
                // * Title
            /*
             <br>
        <section id="WidthsMismatch" class="section">
            <h2 class="subtitle">Inputs with output message <a class="has-text-weight-bold"> Widths mismatch </a>:</h2>
        </section>
             */
                String idMessage = getIDName(message);
                cardsBuilder10.append("<br>");
                cardsBuilder10.append("<section id=\"").append(idMessage).append("\" class=\"section\">");
                cardsBuilder10.append("<h2 class=\"subtitle\">Inputs with output message <a class=\"has-text-weight-bold\">").append(message).append("</a>:</h2>");
                cardsBuilder10.append("</section>");

                // * Cards
                List<List<String>> rowsWithMessage = outputMessagesMap.get(message);
                for (List<String> row : rowsWithMessage) {
                    String card = generateCard(row);
                    cardsBuilder10.append(card);
                }
            }
        } else {
            String noCardsMessage = getNoCardsMessage();
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
        StringBuilder button = new StringBuilder();
        button.append("<div class=\"navbar-item is-small px-0\">");
        button.append("<div class=\"button is-info is-rounded mx-1\">");
        String idButton = getIDName(outputMessage);
        button.append("<a class=\"has-text-white has-text-weight-bold \" href=\"#").append(idButton).append("\">").append(outputMessage).append("</a>");
        button.append("</div>");
        button.append("</div>");
        return button.toString();
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
        card.append("<br>");
        card.append("<div class=\"card has-text-info\">");

        card.append("<header class=\"card-header has-text-white\">");
        int fuzzAttemptNr = Integer.parseInt(row.get(0));
        String mapFileName = row.get(6);
        card.append("<p class=\"card-header-title\">Fuzz Attempt  ").append(fuzzAttemptNr).append(": ").append(mapFileName).append("</p>");

        int initialIndex = rowIndexMap.get(row); // Retrieve the original index
        card.append("<a href=\"#collapsible-card").append(initialIndex).append("\" data-action=\"collapse\" data-target=\"collapsible-card").append(initialIndex).append("\" class=\"card-header-icon\" aria-label=\"more options\">");
        card.append("<span class=\"icon is-white is-clickable\">");
        card.append("<i class=\"fas fa-angle-down \" ></i>");
        card.append("<i class=\"fas fa-angle-up is-hidden\"></i>");
        card.append("</span>");
        card.append("</a>");
        card.append("</header>");

        card.append("<div id=\"collapsible-card").append(initialIndex).append("\" class=\"is-collapsible\">");
        card.append("<div class=\"card-content\">");
        String actionSequence = row.get(5); // ! Alter this if needed different format
        card.append("<p><a class=\"has-text-weight-bold\">The action sequence: </a></p><p>").append(actionSequence).append("</p><br>");
        card.append("<p><a class=\"has-text-weight-bold\">The map:</a></p>");

        card.append("<br><div class=\"box\">");
        String mapPath = row.get(9); // ! Alter this if different format needed
        String absoluteMapPath = FileHandler.normalizeFilePath(mapPath);
        String relativeMapPath = "/" + IterationResultFormatter.getFormattedMapFilePath(absoluteMapPath, false, true, false, true, false, false);
        card.append("<div id=\"").append(relativeMapPath).append("\" class=\"container\"></div>");  // ? Correct path? Yes, needed to append / for it to work in HTML
        card.append("</div>");
        card.append("</div>");

        card.append("<footer class=\"card-footer\">");
        card.append("<p class=\"card-footer-item is-small has-text-info level-left\">");
        card.append("<span>");
        card.append("<strong>Absolute file path: </strong>");
        card.append(absoluteMapPath);
        card.append("</span>");
        card.append("</p>");
        card.append("</footer>");

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
    private String getNoCardsMessage() {
        return "<br>" +
                "<div class=\"card has-background-info-light is-spaced is-align-content-center pl-4 pr-4 pt-1 pb-4\">" +
                "<br>" +
                "<button class=\"delete is-pulled-right is-small\"></button>" +
                "<span>" +
                "There were no inputs that had this exit code." +
                "</span>" +
                "<br>" +
                "</div>";
    }

}