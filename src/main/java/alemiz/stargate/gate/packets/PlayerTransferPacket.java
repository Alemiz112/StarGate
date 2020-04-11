package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerTransferPacket extends StarGatePacket {

    public ProxiedPlayer player;
    public String destination;

    public PlayerTransferPacket(){
        super("PLAYER_TRANSFORM_PACKET", Packets.PLAYER_TRANSFORM_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        player = ProxyServer.getInstance().getPlayer(data[1]);
        destination = data[2];
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());

        convertor.putString(player.getName());
        convertor.putString(destination);

        this.encoded = convertor.getPacketString();
        isEncoded = true;
    }

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        return null;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public String getDestination() {
        return destination;
    }
}
