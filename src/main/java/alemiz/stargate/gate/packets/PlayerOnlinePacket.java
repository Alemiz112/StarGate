package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerOnlinePacket extends StarGatePacket {

    public ProxiedPlayer player = null;
    public String customPlayer = null;

    public PlayerOnlinePacket(){
        super("PLAYER_ONLINE_PACKET", Packets.PLAYER_ONLINE_PACKET);
    }

    @Override
    public void decode() {
        isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(data[1]);

        if (player == null){
            customPlayer = data[1];
        }else this.player = player;
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());

        if (player != null){
            convertor.putString(player.getName());
        }else convertor.putString(customPlayer);

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

    public String getCustomPlayer() {
        return customPlayer;
    }
}
