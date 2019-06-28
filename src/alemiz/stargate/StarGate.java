package alemiz.stargate;

import alemiz.stargate.gate.Server;
import alemiz.stargate.staff.Staff;
import alemiz.stargate.staffchat.StaffChat;
import alemiz.stargate.staffchat.StaffChatCommand;
import com.google.common.collect.Lists;
import io.github.waterfallmc.waterfall.QueryResult;
import io.github.waterfallmc.waterfall.event.ProxyQueryEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.event.EventHandler;


public class StarGate extends Plugin implements Listener{

    private static StarGate instance;

    public Configuration cfg;

    public StaffChat StaffChat;

    public Staff Staff;

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("§cRegistring Config");
        this.registerConfig();

        this.getLogger().info("§cRegistring StarGate Listener");
        getProxy().getPluginManager().registerListener(this, this);

        this.StaffChat = new StaffChat(this);
        this.Staff = new Staff(this);

        new Server(this);
    }

    public static StarGate getInstance() {
        return instance;
    }

    //CONFIG
    private void registerConfig(){
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }

        try {
            cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Color Translator
    public String colorText(String message){
        return ChatColor.translateAlternateColorCodes('§', message);
    }

    //LISTENERS
    @EventHandler
    public void onProxyPing(ProxyPingEvent event){
        ServerPing ping = event.getResponse();
        ServerPing.Players p = ping.getPlayers();

        ping.setPlayers(new ServerPing.Players(p.getOnline() + 1, p.getOnline(), ping.getPlayers().getSample()));

        ServerPing.Protocol prot = new ServerPing.Protocol("", ping.getVersion().getProtocol()); //NAME => MCPE Version
        ping.setVersion(prot);

        event.setResponse(ping);
    }

    @EventHandler
    public void SetQuery(ProxyQueryEvent event){
        QueryResult result = event.getResult();
        result.setVersion("1.11.0");
        //result.setWorldName("Test");
        event.setResult(result);
    }

    @EventHandler
    public void onJoin(PostLoginEvent event){
        ProxiedPlayer player = event.getPlayer();

        List<String> allPerms = new ArrayList<>();
        allPerms.add("stargate.staffchat");
        allPerms.add("stargate.staff");

        if (cfg.contains("Perms."+player.getName())){
            List<String> data = cfg.getStringList("Perms."+player.getName());
            for (String perm : data){
                player.setPermission(perm, true);

                allPerms.remove(allPerms.indexOf(perm));
            }
        }

        for (String perm : allPerms){
            player.setPermission(perm, false);
            getLogger().info("§a" +perm);
        }
    }

}
