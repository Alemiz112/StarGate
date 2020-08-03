package alemiz.stargate.gate.client;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashMap;
import java.util.Map;

public class ClientData {

    public final static String MAX_PLAYERS = "max-players";

    private final String clientName;
    private final ServerInfo serverInfo;

    private final Map<String, Object> attributesMap = new HashMap<>();

    public ClientData(String clientName, int maxPlayers){
        this.clientName = clientName;
        this.setMaxPlayers(maxPlayers);
        this.serverInfo = ProxyServer.getInstance().getServerInfo(clientName);
    }

    public String getClientName() {
        return this.clientName;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public Object getAttribute(String index){
        return this.attributesMap.get(index);
    }

    public void setAttribute(String index, Object data){
        this.attributesMap.put(index, data);
    }

    public void setMaxPlayers(int maxPlayers) {
        this.setAttribute(MAX_PLAYERS, maxPlayers);
    }

    public int getMaxPlayers() {
        return (Integer) this.getAttribute(MAX_PLAYERS);
    }
}
