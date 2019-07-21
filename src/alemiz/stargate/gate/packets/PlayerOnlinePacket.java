package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerOnlinePacket extends StarGatePacket {

    public ProxiedPlayer player;

    public PlayerOnlinePacket(){
        super("PLAYER_ONLINE_PACKET", Packets.PLAYER_ONLINE_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        player = BungeeCord.getInstance().getPlayer(data[1]);
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());
        convertor.putString(player.getName());

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
}
