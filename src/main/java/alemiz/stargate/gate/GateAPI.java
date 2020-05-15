package alemiz.stargate.gate;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.packets.ServerManagePacket;
import alemiz.stargate.gate.packets.StarGatePacket;
import alemiz.stargate.gate.tasks.PingCheckTask;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Timer;

public class GateAPI {

    private static Server gateServer;

    public GateAPI(Server server){
        gateServer = server;
    }

    public static Server getGateServer(){
        return gateServer;
    }

    public static void RegisterPacket(StarGatePacket packet){
        gateServer.packets.put(packet.getID(), packet);
    }

    public static String putPacket(String client, StarGatePacket packet){
        return gateServer.gatePacket(client, packet);
    }

    public static void ping(String client){
        if (!gateServer.clients.containsKey(client) || gateServer.clients.get(client) == null) return;
        Handler clientHandler = gateServer.clients.get(client);
        if (clientHandler == null) return;

        try{
            long now = System.currentTimeMillis();
            clientHandler.getOut().println("GATE_PING");
            gateServer.pingHistory.put(client, now);

            new Timer().schedule(new PingCheckTask(client), Server.PING_DELAY * 1000);
        }catch (Exception e){
            StarGate.getInstance().getLogger().info("§cWARNING: Error while pinging "+client+" => "+e.getMessage());
        }
    }

    /*This function is for checking ping for every client
    * It may be rewrite soon, so we can call it "deprecated" */
    public static void pingAll(){
        gateServer.clients.forEach((client, handler)->{
            ping(client);
        });
    }

    /* We use this function to set response of packet based on UUID
    * Response is then send to client that sent our packet*/
    public static void setResponse(String client, String uuid, String response){
        if (!gateServer.clients.containsKey(client) || gateServer.clients.get(client) == null) return;
        Handler clientHandler = gateServer.clients.get(client);

        try {
            clientHandler.getOut().println("GATE_RESPONSE:"+uuid+":"+response);
        }catch (Exception e){
            StarGate.getInstance().getLogger().info("§cWARNING: Error while sending response to  "+client+" => "+e.getMessage());
        }
    }

    /* Simple way to add or remove server into server pool and allow players join*/
    public static ServerInfo addServer(String name, SocketAddress address, String motd){
        ProxyServer proxy = ProxyServer.getInstance();
        ServerInfo server = proxy.constructServerInfo(name, address, motd, false, true, "default");
        proxy.getServers().putIfAbsent(name, server);
        return proxy.getServers().get(name);
    }

    public static boolean removeServer(String server){
        return ProxyServer.getInstance().getServers().remove(server) == null;
    }

    /* Implemented method of static addServer() to add server and send response back to client*/
    protected void addServer(ServerManagePacket packet, String client){
        InetSocketAddress address;

        try {
            address = new InetSocketAddress(packet.getServerAddress(), Integer.parseInt(packet.getServerPort()));
        }catch (Exception e){
            StarGate.getInstance().getLogger().warning("ERROR: Unable to create InetAddress!");
            packet.setResponse(client, "STATUS_FAILED");
            return;
        }

        ServerInfo server = addServer(packet.getServerName(), address, packet.getServerAddress());
        packet.setResponse(client, "STATUS_SUCCESS,"+server.getName());
    }

    /* Implemented method of static removeServer() to remove server from pool*/
    protected void removeServer(ServerManagePacket packet, String client){
        boolean success = removeServer(packet.getServerName());
        packet.setResponse(client, (success? "STATUS_SUCCESS" : "STATUS_NOT_FOUND"));
    }

    public static boolean isConnected(String client){
        return gateServer.isConnected(client);
    }
}
