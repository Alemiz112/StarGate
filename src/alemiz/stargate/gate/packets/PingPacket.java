package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class PingPacket extends StarGatePacket {

    private int ping;
    private String client;

    public PingPacket(){
        super("PING_PACKET", Packets.PING_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);

        ping = Convertor.getInt(data[1]);
        client = data[2];
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());
        convertor.putInt(ping);
        convertor.putString(client);

        this.encoded = convertor.getPacketString();
        isEncoded = true;
    }

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        PingPacket packet = (PingPacket) super.clone();
        packet.ping = ping;
        packet.client = client;

        return packet;
    }

    public int getPing() {
        return ping;
    }

    public String getClient() {
        return client;
    }
}
