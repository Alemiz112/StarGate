package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class PingPacket extends StarGatePacket {

    protected String client;

    public PingPacket(){
        super("PING_PACKET", Packets.PING_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        client = data[1];
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());
        convertor.putString(client);

        this.encoded = convertor.getPacketString();
        isEncoded = true;
    }

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        PingPacket packet = (PingPacket) super.clone();
        packet.client = client;

        return packet;
    }

    public String getClient() {
        return client;
    }
}
