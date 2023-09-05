package randomGenerators.map;


/**
 * This abstract class serves as the base class for map generators.
 * It provides common functionality for generating random maps and managing map files.
 */
public abstract class MapGenerator {

    /**
     * Generates a random map and returns the file path of the generated map file.
     *
     * @return The file path of the generated map file.
     */
    public abstract String generateRandomMap();

    /**
     * The number of generated maps. Used for generating unique map file names.
     */
    public static int mapCount = 1 ;

    /**
     * The number of generated copies of the map. Used for generating unique names of copies of one original, unmuted map.
     */
    public static int mapCopyCount = 1;

    /**
     * Generates the name of the map, with the number and the correct extension
     * @param extension String that can either be .txt or .bin
     * @return String Full name of the map file.
     */
    public static String generateRandomMapFileName(String extension){
        String fileName = "map_" + mapCount + extension;
        mapCount = mapCount + 1;
        return fileName;
    }

    /**
     * Generates the name of the map copy, with the number and the correct extension.
     *
     * @param extension
     *         String that can either be .txt or .bin
     *
     * @return String Full name of the map copy file.
     */
    public static String generateRandomMapCopyFileName(String extension) {
        String fileName = "mapcopy_" + mapCopyCount + extension;
        mapCopyCount = mapCopyCount + 1;
        return fileName;
    }

}
