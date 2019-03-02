import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {

    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public void connect(int numPort) throws IOException {
        this.serverSocket = new ServerSocket(numPort);
        this.socket = this.serverSocket.accept();

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream());
    }

    public void ReceiveRequestGoBackN() throws IOException {
        in.readLine();

        Trame atrame = new Trame("", 'A', (byte)0, 0);
        out.println(Converter.addBitStuffing(Converter.StringToBinary(atrame.ConvertToString())));
        out.flush();
        System.out.println("ENVOI DE LA TRAME QUI AUTORISE LA CONNEXION...");
    }


    private boolean CheckCRCErrors(String s){

        String receivedCRCString = s.substring(s.length()-24-1, s.length()-8);
        byte b1 = (byte)Integer.parseInt(s.substring(s.length()-24-1, s.length()-16), 2);
        byte b2 = (byte)Integer.parseInt(s.substring(s.length()-16-1, s.length()-8), 2);
        byte[] bytes = {b1,b2};
        String str = null;
        try {
            str = new String(bytes, "ASCII");
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        byte c1 = (byte)str.charAt(0);
        byte c2 = (byte)str.charAt(1);
        short receivedCRC = (short)(((c1 << 8) & 0xFF00) | (c2 & 0x00FF));

        //On crée une trame vide juste pour utiliser la fonction ComputeCRC
        Trame frame = new Trame("", '-', (byte)0, 0);
        short computedCRC = frame.ComputeCRC(s);

        System.out.println("CRC (de la trame): " + receivedCRC);
        System.out.println("CRC (calculé): " + computedCRC);

        return receivedCRC != computedCRC;
    }

    private void tramePrinter(String s){
        byte[] bytes = new byte[s.length()/8];
        int i = 0;
        for(int b = 0; b < s.length(); b+=8){
            bytes[i++] = (byte)Integer.parseInt(s.substring(b, b+8), 2);
        }
        try {
            String frameText = new String(bytes, "ASCII");
            System.out.println("Data: " + frameText.substring(3, frameText.length()-3));
        } catch(Exception e){
            e.printStackTrace();
        }
    }



    public void ReceiveTrames() throws IOException {

        int num = 0;
        char type = 'H'; // On initialise Type
        double timer = 0.0;
        double lastTime = System.nanoTime();

        while(type != 'F'){

            String receivedTrameBinary = null;

            if(in.ready()){
                receivedTrameBinary = in.readLine();
            }
            if(receivedTrameBinary != null){

                //unBitStuffing
                receivedTrameBinary = Converter.unBitStuffing(receivedTrameBinary);

                type = (char)Integer.parseInt(receivedTrameBinary.substring(8, 16), 2);
                byte receivedNum = (byte)Integer.parseInt(receivedTrameBinary.substring(16, 24), 2);

                System.out.println("\n\nRéception de la Trame de Type " + type);

                if(type == 'I'){

                    //Si on ne recoit pas la trame attendue
                    if(receivedNum != num){
                        System.out.println("Trame non attendue reçu");
                    }

                    if(receivedNum == num){
                        timer = 0.0;
                        lastTime = System.nanoTime();

                        System.out.println("******************** Réception de la Trame " + num + " ********************");

                        //S'il n'y a pas d'erreurs, envoyer une confirmation et printer la frame
                        if(!CheckCRCErrors(receivedTrameBinary)){
                            Trame frameToSend = new Trame("", 'A', (byte)num, 0);
                            out.println(Converter.StringToBinary(frameToSend.ConvertToString()));
                            out.flush();
                            num = (num+1) % 8;
                            tramePrinter(receivedTrameBinary);
                        }
                        //S'il y a des erreurs, demander un retransmission
                        else {
                            System.out.println("Trame contient des erreurs. Demande de retransmission....");
                            Trame trameToSend = new Trame("", 'R', (byte)num, 0);
                            out.println(Converter.StringToBinary(trameToSend.ConvertToString()));
                            out.flush();
                        }
                        System.out.println("*****************************************************************");
                    }
                }
            }

            timer += (System.nanoTime()-lastTime);
            lastTime = System.nanoTime();

            //Timer de 3 seconds
            if(timer >= 3000000000.0){
                Trame trameToSend = new Trame("", 'R', (byte)num, 0);
                out.println(Converter.StringToBinary(trameToSend.ConvertToString()));
                out.flush();
                timer = 0.0;
            }
        }
    }

    public void endOfCommunication() throws IOException {
        serverSocket.close();
        socket.close();
    }


    public static void main(String[] args) throws IOException {

        System.out.flush();
        System.out.println("###############################################");
        System.out.println("#                                             #");
        System.out.println("#           COMMUNICATION PAR TRAME           #");
        System.out.println("#                  RECEPTEUR                  #");
        System.out.println("#                                             #");
        System.out.println("###############################################");
        System.out.println();
        System.out.println();

        if(args.length != 1){
            System.out.println("Syntax Error: <Port>");
        }
        else{

            System.out.println("EN ATTENTE...");

            /*Numéro de Port*/
            int numPort = Integer.parseInt(args[0]);

            try{

                Receiver receiver = new Receiver();
                receiver.connect(numPort);
                receiver.ReceiveRequestGoBackN();
                receiver.ReceiveTrames();
                receiver.endOfCommunication();

            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }

}
