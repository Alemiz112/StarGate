package alemiz.stargate;

import alemiz.stargate.gate.Server;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;

import com.google.common.io.ByteStreams;

public class StarGate extends Plugin implements Listener{

    private static StarGate instance;
    public Configuration cfg;

    protected Server server;

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("§cRegistring Config");
        this.registerConfig();

        this.getLogger().info("§cRegistring StarGate Listener");
        getProxy().getPluginManager().registerListener(this, this);

        server = new Server(this);
    }

    public static StarGate getInstance() {
        return instance;
    }

    public Server getServer() {
        return server;
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
}
