import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class RandomBinaryMap {

    private static final int MAX_FILE_SIZE = 1000; // Maximum Size of the binary file in bytes

    private File file;

    public RandomBinaryMap(){

        try {
            Random random = new Random();

            int fileSize = random.nextInt(MAX_FILE_SIZE) + 1; // Random file size between 1 and max file size

            // create file with random file size
            File file = File.createTempFile("input", ". bin");
            FileOutputStream fos = new FileOutputStream(file);

            // create random data
            byte[] inputData = new byte[fileSize]; // generate file
            random.nextBytes(inputData); // take random data

            // write the data to the file
            fos.write(inputData);
            fos.close();

            this.file = file;

        } catch (IOException e){ e.printStackTrace(); this.file = null;}

    }

    public File getRandomBinaryFile(){
        return file;
    }

    public void deleteRandomBinaryFile(){
        boolean successFullyDeleted = file.delete();
        if (!successFullyDeleted){System.out.println("Map file not successfully deleted.");}
    }

}
