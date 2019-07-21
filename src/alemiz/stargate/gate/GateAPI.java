package alemiz.stargate.gate;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.packets.StarGatePacket;

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

}
