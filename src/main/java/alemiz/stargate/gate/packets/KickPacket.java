package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class KickPacket extends StarGatePacket {

    public String reason;
    public String player;

    public KickPacket(){
        super("KICK_PACKET", Packets.KICK_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        player = data[1];
        reason = data[2];
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());

        convertor.putString(player);
        convertor.putString(reason);

        this.encoded = convertor.getPacketString();
        isEncoded = true;
    }

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        return null;
    }

    public String getPlayer() {
        return player;
    }

    public String getReason() {
        return reason;
    }
}
