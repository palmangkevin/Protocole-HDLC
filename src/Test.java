import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

public class Test {

    private static Random random = new Random();

    private ArrayList<Trame> trames;
    private int numTramesNotSent;
    private int numTramesErrors;
    private int[] tramesNotSentNumbers;
    private boolean[] notSentOks;
    private int[] tramesErrorsNumbers;
    private boolean[] errorsOks;
    private String[] tramesErrorsCache;

    public Test(ArrayList<Trame> trames){

        this.trames = trames;

        if(trames.size() > 0){
            this.numTramesNotSent = random.nextInt(3) + 1;		//Entre 1 et 3
            this.numTramesErrors = random.nextInt(3) + 1;		//Entre 1 et 3
        } else {
            this.numTramesNotSent = 0;
            this.numTramesErrors = 0;
        }
        this.tramesNotSentNumbers = new int[this.numTramesNotSent];
        this.notSentOks = new boolean[this.numTramesNotSent];
        this.tramesErrorsNumbers = new int[this.numTramesErrors];
        this.errorsOks = new boolean[this.numTramesErrors];
        this.tramesErrorsCache = new String[this.numTramesErrors];
        Arrays.fill(tramesNotSentNumbers, -1);
        Arrays.fill(tramesErrorsNumbers, -1);

        for(int i = 0; i < tramesNotSentNumbers.length; i++){
            if(trames.size() > 1){
                do {
                    tramesNotSentNumbers[i] = random.nextInt(trames.size()-1) + 1;		//Entre 1 et frames.size()-1
                } while(!Converter.In(tramesNotSentNumbers[i], tramesNotSentNumbers));
            } else {
                tramesNotSentNumbers[i] = 0;
            }
            System.out.println("Trame " + tramesNotSentNumbers[i] + " n'a pas été envoyé !");
            notSentOks[i] = false;
        }
        for(int j = 0; j < tramesErrorsNumbers.length; j++){
            if(trames.size() > 1){
                do {
                    tramesErrorsNumbers[j] = random.nextInt(trames.size()-1) + 1;		//Entre 1 et frames.size()-1
                } while(!Converter.In(tramesErrorsNumbers[j], tramesErrorsNumbers));
            } else {
                tramesErrorsNumbers[j] = 0;
            }
            errorsOks[j] = false;
            System.out.println("Erreurs dans la Trame " + tramesErrorsNumbers[j]);
        }

        System.out.println("\n\n");

        //Mettre les erreurs
        for(int k = 0; k < tramesErrorsNumbers.length; k++){
            Trame trame = trames.get(tramesErrorsNumbers[k]);
            if(trame.getData().length() > 0){
                tramesErrorsCache[k] = trame.getData();
                trame.setData("0" + trame.getData().substring(0, trame.getData().length()));
                //frame.setCRC(frame.ComputeCRC(Util.StringToBinaryString(frame.ConvertToString())));
            }
        }
    }

    public int[] getTramesNotSentNumbers(){
        return tramesNotSentNumbers;
    }

    public boolean[] getNotSentOks(){
        return notSentOks;
    }

    public int[] getTramesErrorsNumbers(){
        return tramesErrorsNumbers;
    }

    public boolean[] getErrorsOks(){
        return errorsOks;
    }

    public String[] getTramesErrorsCache(){
        return tramesErrorsCache;
    }

}
