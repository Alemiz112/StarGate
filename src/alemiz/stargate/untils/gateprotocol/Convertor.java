package alemiz.stargate.untils.gateprotocol;

public class Convertor {

    private String[] data = new String[1];

    public Convertor(int id){
        data[0] = Integer.toString(id);
    }


    public void packetStringData(String packetString){
        data = packetString.split("!");
    }


    public void putInt(int integer){
        push(Integer.toString(integer));
    }


    public void putString(String string){
        push(string);
    }


    public String getString(int key){
        return data[key];
    }


    public static int getInt(String string){
        return Integer.decode(string);
    }


    public static String getPacketString(String[] strings){
        String string = "";
        for (int i = 0; i < strings.length; i++){
            if (strings.length - i <= 1){
                string += (strings[i]);
                continue;
            }
            string += (strings[i] + "!");
        }
        return string;
    }

    public static String[] getPacketStringData(String packetString){
        return packetString.split("!");
    }

    public String getPacketString(){
        String packetString = "";
        for (int i = 0; i < data.length; i++){
            if (data.length - i <= 1){
                packetString += (data[i]);
                continue;
            }
            packetString += (data[i] + "!");
        }
        return packetString;
    }

    public void push(String string){
        int lenght = data.length;
        String[] newData = new String[lenght + 1];

        int i = 0;
        for (String value : data){
            newData[i] = value;
            i++;
        }
        newData[i] = string;
        data = newData;
    }
}
