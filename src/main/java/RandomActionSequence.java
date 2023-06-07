import java.util.Random;

public class RandomActionSequence {

    String actionSequence;

    public RandomActionSequence(){
        StringBuilder actionSequence = new StringBuilder();
        Random random = new Random();

        // Generate a random action sequence
        for (int i = 0; i < 5; i++) {
            int rand = random.nextInt(8);
            switch (rand) {
                case 0:
                    actionSequence.append('E'); // Exit
                    break;
                case 1:
                    actionSequence.append('Q'); // Quit
                    break;
                case 2:
                    actionSequence.append('S'); // Start
                    break;
                case 3:
                    actionSequence.append('W'); // Sleep
                    break;
                case 4:
                    actionSequence.append('U'); // Up
                    break;
                case 5:
                    actionSequence.append('L'); // Left
                    break;
                case 6:
                    actionSequence.append('D'); // Down
                    break;
                case 7:
                    actionSequence.append('R'); // Right
                    break;
            }
        }
        this.actionSequence = actionSequence.toString();
    }

    public String getActionSequence(){
        return this.actionSequence;
    }

    public void deleteActionSequence(){
        this.actionSequence = null ;
    }

}
