package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class PlayerTransferPacket extends StarGatePacket {

    private ProxiedPlayer player;
    private String destination;

    public PlayerTransferPacket(){
        super("PLAYER_TRANSFORM_PACKET", Packets.PLAYER_TRANSFORM_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        player = BungeeCord.getInstance().getPlayer(data[1]);
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
