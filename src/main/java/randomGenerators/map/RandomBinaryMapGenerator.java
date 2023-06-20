package randomGenerators.map;

import organizers.FileHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * This class generates random binary maps based on the RandomMapGenerator class.
 * It provides functionality to generate binary maps with random content.
 */
public class RandomBinaryMapGenerator extends MapGenerator {

    /**
     * Max number of bytes the binary file can have.
     * Specified in the configuration file.
     */
    private int maxBinarySize;

    /**
     * Constructs a RandomBinaryMapGenerator with the specified maximum binary size.
     *
     * @param maxBinarySize The maximum size of the generated binary maps.
     */
    public RandomBinaryMapGenerator(int maxBinarySize) {
        this.maxBinarySize = maxBinarySize;
    }

    /**
     * Generates a random binary map file.
     *
     * @return The file path of the generated binary map file.
     */
    @Override
    public String generateRandomMap() {
        Random random = new Random();

        int binarySize = random.nextInt(maxBinarySize);
        byte[] bytes = new byte[binarySize];
        random.nextBytes(bytes);

        String fileName = generateRandomMapFileName(".bin");
        String filePath = FileHandler.actualMapsDirectoryPath + "\\" + fileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }

    /**
     * Generates a custom binary map if u have an encoded line
     * coverts each character in the encoded line to its ASCII value, stores those vlaue in a byte array
     * and writes the byte array to a binary file.
     *
     * @param encodedline The Line in normal letters
     * @return String filePath of the encoded binary file.
    */
    public String generateCustomEncodedMap(String encodedline){

        // Convert each character to its ASCII value and store them in a byte array
        byte[] binaryData = new byte[encodedline.length()];
        for (int i = 0; i < encodedline.length(); i++) {
            binaryData[i] = (byte) encodedline.charAt(i);
        }

        String fileName = generateRandomMapFileName(".bin");
        String filePath = FileHandler.actualMapsDirectoryPath + "\\" + fileName;

        // Write the byte array to a binary file
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(binaryData);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

}
