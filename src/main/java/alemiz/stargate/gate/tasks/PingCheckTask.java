package alemiz.stargate.gate.tasks;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.Handler;
import alemiz.stargate.gate.Server;

import java.util.TimerTask;

public class PingCheckTask extends TimerTask {

    private final String client;

    public PingCheckTask(String client){
        this.client = client;
    }

    @Override
    public void run() {
        Handler client = Server.getInstance().getClients().get(this.client);
        if (client == null || Server.getInstance().getPingHistory().remove(this.client) == null) return;

        StarGate plugin = StarGate.getInstance();
        plugin.getLogger().info("§bConnection with §e"+client+" §b is slow! Pong was not received!");

        try {
            if (!client.reconnect()){
                plugin.getLogger().info("§cERROR: Reconnecting with §6"+client+"§cwas interrupted!");
                plugin.getLogger().info("§cTrying to establish new connection with §6"+client);
            }
        }catch (NullPointerException e){
            plugin.getLogger().info("§cLooks like client §6"+client +"§c keeps already disconnected!");
        }

        Server.getInstance().getClients().remove(this.client);
    }
}
