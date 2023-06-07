import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class RandomMap {

    // TODO Random max?
    private static final int MAX_MAP_WIDTH = 20;
    private static final int MAX_MAP_HEIGHT = 20;


    private int MAP_WIDTH;
    private int MAP_HEIGHT;

    private char[][] map;

    private File mapFile;

    public boolean mapFileCreated = false;
    public boolean mapFileDeleted = false;


    /**
     * Makes a Random Map File.
     */
    public RandomMap() {

        Random random = new Random();

        // TODO Check with 0 index and so on
        this.MAP_HEIGHT = random.nextInt(MAX_MAP_HEIGHT);
        this.MAP_WIDTH = random.nextInt(MAX_MAP_WIDTH);

        char[][] map = new char[MAP_WIDTH][MAP_HEIGHT];

        // Populate the map with random elements
        // TODO Set wall random as well. Rows and col starts at 0!
        for (int row = 0; row < MAP_WIDTH; row++) {
            for (int col = 0; col < MAP_HEIGHT; col++) {
                // Set empty cell, wall, monster, or Pacman randomly
                int rand = random.nextInt(4); // 0 (included), 1, 2 and 3 (4 not included)
                    if (rand < 1){ map[row][col] = 'W';
                    } else if (rand < 2) { map[row][col] = 'M'; // Monster
                    } else if (rand < 3) { map[row][col] = 'P'; // Pacman - Can only be one pacman?
                    } else {  map[row][col] = '0'; // Empty cell
                }
            }
        }
        this.map = map;
    }

    /**
     * Create file with random map. Set this as class variable. Class variable in null when error occurred.
     * After executing this method (successfully or not), the boolean mapFileCreated is set to true.
     */
    private void createMapFile() {

        // Save the map to a file
        try {
            File mapFile = File.createTempFile("map", ".map");
            FileWriter writer = new FileWriter(mapFile);
            for (int row = 0; row < MAP_WIDTH; row++) {
                writer.write(map[row]);
                writer.write(System.lineSeparator());
            }
            writer.close();
            this.mapFile = mapFile;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Map-file not successfully created.");
            this.mapFile = null;
        }
        this.mapFileCreated = true; // true even if not successfully created
    }

    /**
     * Get map file.
     * This way only one mapFile per instance.
     */
    public File getMapFile() {
        if (!mapFileCreated){createMapFile();}         // If map isn't saved yet to variable:
        return mapFile;
    }

    // TODO Clean-up here?
    /**
     * Delete map file.
     */
    public void deleteMapFile(){
        boolean successFullyDeleted = mapFile.delete();
        if (!successFullyDeleted){System.out.println("Map file not successfully deleted.");}
        this.mapFileDeleted = true; // true even if not successfully deleted
    }

}
