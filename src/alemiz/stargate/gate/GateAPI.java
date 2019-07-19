package alemiz.stargate.gate;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.packets.Packets;
import alemiz.stargate.gate.packets.StarGatePacket;
import alemiz.stargate.gate.packets.WelcomePacket;

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

    public static void putPacket(String client, StarGatePacket packet){
        gateServer.gatePacket(client, packet);
    }

    public static void ping(String client){
        if (!gateServer.clients.containsKey(client) || gateServer.clients.get(client) == null) return;
        Handler clientHandler = gateServer.clients.get(client);

        try{
            clientHandler.getOut().println("GATE_PING:" +System.nanoTime());
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

}
