package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class ConnectionInfoPacket extends StarGatePacket {

    public static final int CONNECTION_CONNECTED = 0;
    public static final int CONNECTION_CLOSED = 1;
    public static final int CONNECTION_RECONNECT = 2;

    public static final int CONNECTION_ABORTED = 5;

    public static final String ABORTED = "Connection unacceptable closed";
    public static final String WRONG_PASSWORD = "Wrong password";

    public int packetType;
    public String reason = null;

    public ConnectionInfoPacket(){
        super("CONNECTION_INFO_PACKET", Packets.CONNECTION_INFO_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        packetType = Convertor.getInt(data[1]);

        if (data.length > 2){
            reason = data[2];
        }
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());

        convertor.putInt(packetType);
        if (reason != null){
            convertor.putString(reason);
        }

        this.encoded = convertor.getPacketString();
        isEncoded = true;
    }

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        return null;
    }

    public int getPacketType() {
        return packetType;
    }

    public String getReason() {
        return reason;
    }
}
