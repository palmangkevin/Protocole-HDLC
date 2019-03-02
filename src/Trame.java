import java.io.UnsupportedEncodingException;

public class Trame {

    private static final byte FLAG = 126; // 01111110 en Int
    private static final int GEN_POLY = 0x1021;

    private String data;
    private byte type;
    private byte num;
    private int total;
    private short crc;

    public Trame(String data, char type, byte num, int total){
        this.data = data;
        this.type = (byte)type;
        this.num = num;
        this.total = total;

        this.crc = (short)0;
        String binaryString = Converter.StringToBinary(this.ConvertToString());
        this.crc = ComputeCRC(binaryString);
    }

    public String ConvertToString(){
        String str = (char)FLAG + (char)type + (char)num + data + (char)crc + (char)FLAG;
        int i = 0;
        byte[] bytes = new byte[str.length()+1];
        bytes[0] = FLAG;
        bytes[1] = type;
        bytes[2] = num;
        for(int b = 3; b < bytes.length-3; b++){
            bytes[b] = (byte)data.charAt(i++);
        }
        bytes[bytes.length-3] = (byte)((crc >> 8) & 255);
        bytes[bytes.length-2] = (byte)(crc & 255);
        bytes[bytes.length-1] = FLAG;

        try {
            return new String(bytes, "ASCII");
        } catch(Exception e){
            return null;
        }
    }

    public short ComputeCRC(String binaryString){
        byte[] bytes = null;
        try {
            bytes = binaryString.substring(8, binaryString.length()-24).getBytes("ASCII");
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        int crc = 0xFFFF;
        for(int i = 0; i < bytes.length; i++){
            byte by = bytes[i];
            for(int j = 0; j < 8; j++){
                int b = 0;
                if((by >> (7-i) & 1) == 1){
                    b = 1;
                }
                int c = 0;
                if((crc >> 15 & 1) == 1){
                    c = 1;
                }

                crc = crc << 1;
                if((c ^ b) != 0){
                    crc = crc ^ GEN_POLY;
                }
            }
        }

        //Si le crc est negative, le rendre positive car les nombre negative vont
        //causer des problèmes lors de la creation des trames
        crc = crc & 0x7F7F;

        return (short)crc;
    }

    public String getData(){
        return data;
    }

    public void setData(String data){
        this.data = data;
    }

    public int getCRC(){
        return crc;
    }

    public void setCRC(short crc){
        this.crc = crc;
    }



}
