import java.io.*;

public class Converter {

    public String ReadFile(String file){
        String message = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while(line != null){
                message += line;
                line = br.readLine();
            }
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

        return message;
    }

    public static String StringToBinary(String text){

        byte[] bytes = null;

        try {
            bytes = text.getBytes("ASCII");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }

        String binary = "";
        for(int i = 0; i < bytes.length; i++){
            byte b = bytes[i];
            String bits = "";
            for(int j = 7; j >= 0; j--){
                if(((b >> j) & 1) == 0)
                    bits += "0";
                else
                    bits += "1";
            }
            binary += bits;
        }

        return binary;
    }

    public static String addBitStuffing(String s){

        String resultat = "";
        int count = 0;

        for(int i = 0; i < s.length(); i++){
            if (s.charAt(i) == '1'){
                count++;
            }
            else{
                count = 0;
            }

            resultat = resultat + s.charAt(i);

            if(count == 5){
                resultat = resultat + '0';
                count = 0;
            }
        }
        return resultat;
    }

    public static String removeCharAt(String s, int position){
        return s.substring(0, position) + s.substring(position + 1);
    }

    public  static String unBitStuffing(String s){

        String resultat = "";
        int count = 0;

        for(int i=0;i<s.length();i++){
            if(s.charAt(i) == '1'){
                count++;
                resultat = resultat +s.charAt(i); }
            else{
                resultat = resultat + s.charAt(i);
                count = 0;
            }
            if(count == 5 ){
                s = removeCharAt(s,i+1);
                count = 0;
            }
        }
        return resultat;

    }

    public static boolean In(int value, int[] array){

        for(int i = 0; i < array.length; i++){
            if(array[i] == value){
                return true;
            }
        }

        return false;
    }


}
