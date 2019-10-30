package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class PingPacket extends StarGatePacket {

    protected String pingData;
    protected String client;

    public PingPacket(){
        super("PING_PACKET", Packets.PING_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);

        pingData = data[1];
        client = data[2];
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());
        convertor.putString(pingData);
        convertor.putString(client);

        this.encoded = convertor.getPacketString();
        isEncoded = true;
    }

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        PingPacket packet = (PingPacket) super.clone();
        packet.pingData = pingData;
        packet.client = client;

        return packet;
    }

    public String getPingData() {
        return pingData;
    }

    public String getClient() {
        return client;
    }
}
