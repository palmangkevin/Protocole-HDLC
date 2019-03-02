import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Sender {

    //private Test test;

    private int WINDOW = 8;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<Trame> trames = new ArrayList<Trame>();
    private int globalLoopCount = 0;


    public void CreateTrames(String file) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line = br.readLine();
        int num = 0;
        int total = 0;
        while(line != null){
            trames.add(new Trame(line, 'I', (byte)num, total));
            num = (num+1) % 8;
            if(num == 0)
                this.globalLoopCount++;
            total++;

            line = br.readLine();
        }
        //this.test = new Test(trames);
    }

    public void connect(String host, int numPort) throws IOException {
        this.socket = new Socket(host, numPort);
        this.socket.setSoTimeout(3000);

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream());
    }

    public void SendRequestGoBackN() throws IOException {

        System.out.println("ENVOI DE LA TRAME DE CONNEXION...\n");

        Trame cframe = new Trame("", 'C', (byte)0, 0);
        out.println(Converter.addBitStuffing(Converter.StringToBinary(cframe.ConvertToString())));
        out.flush();

        String rr = Converter.unBitStuffing(in.readLine());
        System.out.println("**CONNEXION ACCEPTÉE PAR RÉCEPTEUR**");
        System.out.println();

    }

    public void sendTrames() throws IOException, SocketTimeoutException{

        boolean end = false;
        int num = 0;
        int total = 0;
        int loopCount = 0;
        boolean send = true;
        boolean ok = false;
        boolean c = false;

        while(!end){

            //Gérer les trames perdues
            /*
            int[] tramesNotSentNumbers = test.getTramesNotSentNumbers();
            boolean[] notSentOks = test.getNotSentOks();
            for(int i = 0; i < tramesNotSentNumbers.length; i++){
                if(total == tramesNotSentNumbers[i]){
                    if(!notSentOks[i]){
                        notSentOks[i] = true;
                        num = (num+1) % 8;
                        if(num == 0){
                            send = false;
                            loopCount++;
                        }
                        total++;
                        c = true;
                    }
                    break;
                }
            }
            if(c){
                c = false;
                continue;
            }*/

            //Gérer les trames corrompues
            /*
            int[] tramesErrorsNumbers = test.getTramesErrorsNumbers();
            boolean[] errorsOks = test.getErrorsOks();
            for(int i = 0; i < tramesErrorsNumbers.length; i++){
                if(total == tramesErrorsNumbers[i]){
                    if(errorsOks[i]){
                        errorsOks[i] = false;
                        Trame trame = trames.get(tramesErrorsNumbers[i]);
                        trame.setData(test.getTramesErrorsCache()[i]);//.substring(0, frame.getData().length()-1));
                        trame.setCRC(trame.ComputeCRC(Converter.StringToBinary(trame.ConvertToString())));
                    }
                    errorsOks[i] = true;
                    break;
                }
            }*/

            if(send && (loopCount*8+num) < trames.size()){

                System.out.println("******************** Envoi de la Trame " + total + " (" +"Numéro Trame: "+ num + ") ********************");
                System.out.println("Data de la Trame " + num + ": " + trames.get(total).getData());
                System.out.println("CRC de la Trame: " + trames.get(total).getCRC());

                String str = Converter.StringToBinary(trames.get(total).ConvertToString());
                String frameBinaryString = Converter.addBitStuffing(Converter.StringToBinary(trames.get(total).ConvertToString()));

                out.println(frameBinaryString);
                out.flush();
                System.out.println("********************************************************************************\n\n");

                num = (num+1) % 8;
                total++;

                if(num == 0){
                    send = false;
                    loopCount++;
                }
            }

            String receivedFrameBinary = null;

            try {
                receivedFrameBinary = in.readLine();
            } catch(SocketTimeoutException e){}

            if(receivedFrameBinary != null){
                char type = (char)Integer.parseInt(receivedFrameBinary.substring(8, 16), 2);
                byte receivedNum = (byte)Integer.parseInt(receivedFrameBinary.substring(16, 24), 2);
                if(type == 'A'){
                    System.out.println("ACK REÇU DE LA TRAME: " + receivedNum + "\n\n");

                    if(receivedNum == 7){
                        send = true;
                    }
                    if(loopCount*8 + receivedNum == trames.size()-1){
                        end = true;
                    }
                }
                else if(type == 'R'){
                    System.out.println("REJET REÇU DE LA TRAME: " + receivedNum + "\n\n");
                    if(num != 0){
                        total -= (num - receivedNum);
                    } else {
                        total -= (7 - receivedNum)+1;
                        loopCount--;
                    }
                    num = receivedNum;
                    send = true;
                }
            }
        }

        Trame endFrame = new Trame("", 'F', (byte)0, 0);
        String frameBinaryString = Converter.StringToBinary(endFrame.ConvertToString());
        out.println(frameBinaryString);
        out.flush();

    }

    public void communicationEnd() throws IOException {
        socket.close();
    }




    public static void main(String[] args) {

        System.out.flush();
        System.out.println("###############################################");
        System.out.println("#                                             #");
        System.out.println("#           COMMUNICATION PAR TRAME           #");
        System.out.println("#                   EMETTEUR                  #");
        System.out.println("#                                             #");
        System.out.println("###############################################");
        System.out.println();
        System.out.println();

        if(args.length != 4){
            System.out.println("Syntax Error : <Nom_machine> <Port> <Nom_fichier> <Go_Back_N>");
        }

        else{

            /*Nom de la machine*/
            String host = args[0];

            /*Numero de port*/
            int numPort = Integer.parseInt(args[1]);

            /*Fichier à lire*/
            String file = args[2];

            /*Go-Back-N*/
            int goBackN = Integer.parseInt(args[3]);

            try{
                Sender sender = new Sender();
                sender.connect(host,numPort);

                if(goBackN == 0){
                    sender.SendRequestGoBackN();
                    sender.CreateTrames(file);
                    sender.sendTrames();
                    sender.communicationEnd();
                }
                else{
                    System.out.println("Syntax Error : <Nom_machine> <Port> <Nom_fichier> <0>");
                }

            }catch(IOException e){
                e.printStackTrace();
            }
        }

    }



}
