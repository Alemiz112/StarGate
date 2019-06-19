package alemiz.stargate.staffchat;

import alemiz.stargate.StarGate;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StaffChat {

    private StarGate plugin;

    private static StaffChat instance;

    public Map<String, ProxiedPlayer> forceStaff = new HashMap<String, ProxiedPlayer>();

    public StaffChat(StarGate plugin){
        this.plugin = plugin;
        instance = this;

        this.plugin.getProxy().getPluginManager().registerListener(this.plugin, new StaffListener(this));
        this.plugin.getProxy().getPluginManager().registerCommand(this.plugin, new StaffChatCommand(this));
    }

    public StaffChat getInstance(){
        return instance;
    }

    public void sendMessage(ProxiedPlayer player, String message){
        Collection<ProxiedPlayer> players = this.plugin.getProxy().getPlayers();

        //'&7[&6Staff&7] &b(%server%) &7%player% : &f%message%'
        String format = this.plugin.cfg.getString("StaffChatFormat");
        format = format.replace("%player%", player.getDisplayName());
        format = format.replace("%message%", message);
        format = format.replace("%server%", player.getServer().getInfo().getName());

        String finalMessage = this.plugin.colorText(format);

        for (ProxiedPlayer member : players){
            if (player.hasPermission("stargate.staffchat")){
                member.sendMessage(new TextComponent(finalMessage));
            }
        }
    }

    public void forceChattAdd(ProxiedPlayer player){
        this.forceStaff.put(player.getName(), player);
        player.sendMessage(new TextComponent(plugin.colorText("§7[§6Staff§7] §fForce staff chat turned ON!")));
    }

    public void forceChattRemove(ProxiedPlayer player){
        this.forceStaff.remove(player.getName());
        player.sendMessage(new TextComponent(plugin.colorText("§7[§6Staff§7] §fForce staff chat turned OFF!")));
    }

    public boolean isForceChatting(ProxiedPlayer player){
        if (forceStaff.containsKey(player.getName()) && forceStaff.get(player.getName()) != null){
            return true;
        }else {
            return false;
        }
    }

}
