package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class ForwardPacket extends StarGatePacket {

    public String client;
    public String encodedPacket = "";

    public ForwardPacket(){
        super("FORWARD_PACKET", Packets.FORWARD_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        client = data[1];

        for (int i = 2; i < data.length; i++){
            if (i == data.length - 1){
                encodedPacket+= data[i];
                continue;
            }
            encodedPacket+= data[i] + "!";
        }
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());
        convertor.putString(client);

        String[] forwardPacketData = Convertor.getPacketStringData(encodedPacket);
        for (String data : forwardPacketData){
            convertor.putString(data);
        }

        this.encoded = convertor.getPacketString();
        isEncoded = true;
    }

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        return null;
    }

    public String getClient() {
        return client;
    }

    public String getEncodedPacket() {
        return encodedPacket;
    }
}
