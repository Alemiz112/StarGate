package alemiz.stargate.gate.tasks;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.GateAPI;
import alemiz.stargate.gate.Handler;
import alemiz.stargate.gate.Server;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PingCheckTask extends TimerTask {

    private final String client;

    public PingCheckTask(String client){
        this.client = client;
    }

    @Override
    public void run() {
        Handler client = Server.getInstance().getClients().get(this.client);
        Long received = Server.getInstance().shiftPing(this.client);

        if (client == null || received == null) return;
        long now = System.currentTimeMillis();
        long delay = TimeUnit.SECONDS.toMillis(Server.PING_DELAY);
        long ping = (now - received);

        if (ping <= delay) return;

        if (client.isStable()){
            client.setStable(false);
            GateAPI.ping(this.client);
            return;
        }

        StarGate plugin = StarGate.getInstance();
        //plugin.getLogger().info("§bPING: §e"+ ping+"ms");
        plugin.getLogger().info("§bConnection with §e"+this.client+"§b is slow! Pong was not received!");

        Server.getInstance().getClients().remove(this.client);
        try {
            if (!client.reconnect()){
                plugin.getLogger().info("§cERROR: Reconnecting with §6"+this.client+"§cwas interrupted!");
                plugin.getLogger().info("§cTrying to establish new connection with §6"+this.client);
            }
        }catch (NullPointerException e){
            plugin.getLogger().info("§cLooks like client §6"+this.client +"§c keeps already disconnected!");
        }

    }
}
