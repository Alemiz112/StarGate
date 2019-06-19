package alemiz.stargate.untils;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStorage {

    private ProxiedPlayer player;
    private String name;
    private UUID uuid;

    private Map<String, Object> data = new HashMap<String, Object>();

    public PlayerStorage(ProxiedPlayer player, Map<String, Object> data) {
        this.player = player;
        this.name = player.getName();
        this.uuid = player.getUniqueId();

        this.data = data;
    }

    public String getName() {
        return this.name;
    }

    public UUID getID(){
        return this.uuid;
    }

    public ProxiedPlayer getPlayer(){
        return this.player;
    }

    /** Returns Synapse Client| May be useful*/
    public Server getSynapse(){
        return this.player.getServer();
    }

    public Map getData(){
        return this.data;
    }

    public void saveToData(String key, Object data){
        this.data.put(key, data);
    }

    public void removeByKey(String key){
        this.data.remove(key);
    }

    public void unset(String key){
        this.removeByKey(key);
    }

    public boolean isset(String key){
        return this.data.containsKey(key) && this.data.get(key) != null;
    }

    public void newData(Map<String, Object> data){
        this.data = data;
    }
}
