package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class PlayerTransferPacket extends StarGatePacket {

    public String player;
    public String destination;

    public PlayerTransferPacket(){
        super("PLAYER_TRANSFER_PACKET", Packets.PLAYER_TRANSFER_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        player = data[1];
        destination = data[2];
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());

        convertor.putString(player);
        convertor.putString(destination);

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

    public String getDestination() {
        return destination;
    }
}
