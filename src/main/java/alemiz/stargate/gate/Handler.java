package alemiz.stargate.gate;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.packets.ConnectionInfoPacket;
import alemiz.stargate.gate.packets.StarGatePacket;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

class Handler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String name;
    private String closeReason = "unknown";

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public Socket getSocket() {
        return socket;
    }

    public void run() {
        try {
            //in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //out = new DataOutputStream(socket.getOutputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {

               String handshake = in.readLine();
               if (handshake.startsWith("CHEVRON:") && handshake.length() > 8) {
                   String[] handshakeData = handshake.substring(8).split(":");
                   name = handshakeData[0];

                   /* Password not set*/
                   if (handshakeData.length < 2 || !handshakeData[1].equals(Server.password)){
                       StarGate.getInstance().getLogger().warning("§aNew client attends to connect: §6"+name);
                       StarGate.getInstance().getLogger().warning("§cClient not authenticated! Wrong password!");

                      gatePacket(new ConnectionInfoPacket(){{
                           packetType = CONNECTION_ABORTED;
                           reason = WRONG_PASSWORD;
                           isEncoded = false;
                      }});
                      return;
                   }

                   if (GateAPI.getGateServer().clients.containsKey(name)){
                       gatePacket(new ConnectionInfoPacket(){{
                           packetType = CONNECTION_ABORTED;
                           reason = "Server with this name already is connected";
                           isEncoded = false;
                       }});
                       return;
                   }

                   /*Sending message to Client to confirm successful Connection*/
                   gatePacket(new ConnectionInfoPacket(){{
                       packetType = CONNECTION_CONNECTED;
                       isEncoded = false;
                   }});
                   GateAPI.getGateServer().clients.put(name, this);

                   StarGate.getInstance().getLogger().info("§aNew client connected: §6"+name);
                   StarGate.getInstance().getLogger().info("§aADDRESS: §e"+socket.getInetAddress().toString().replace("/", "")+":"+socket.getPort());
                   break;
               }
            }

            GateAPI.ping(name);

            while (true) {
                /* This should prevent higher CPU usage*/
                if (socket.getInputStream().available() < 0){
                    continue;
                }
                String line = in.readLine();
                if (line == null) continue;

                try {
                    StarGatePacket packet;

                    if ((packet = GateAPI.getGateServer().processPacket(name, line)) == null){
                        StarGate.getInstance().getLogger().info("§cWARNING: Unknown packet !");
                    }else {
                        if (packet instanceof ConnectionInfoPacket){
                            if (((ConnectionInfoPacket) packet).packetType == ConnectionInfoPacket.CONNECTION_CLOSED){
                                String reason = ((ConnectionInfoPacket) packet).reason;
                                closeReason = (reason == null) ? closeReason : reason;
                                break;
                            }
                        }
                    }
                }catch (Exception e){
                    StarGate.getInstance().getLogger().info("§cERROR: Problem appears while processing packet!");
                    StarGate.getInstance().getLogger().info("§c"+e.getStackTrace()[0].getLineNumber());
                }
            }

        } catch(IOException i) {
            if (i.getMessage() == "Connection reset" || i.getMessage() == "Stream closed") return;

            StarGate.getInstance().getLogger().info("§cERROR: Connection with §6"+name+"§c has been aborted!");
            System.out.println(i.getMessage());
        } finally {
            GateAPI.getGateServer().clients.remove(name);
            StarGate.getInstance().getLogger().info("§cWARNING: Connection with §6"+name+"§c has been closed!");
            StarGate.getInstance().getLogger().info("§cReason: §4"+closeReason);

            try {
                in.close();
                out.close();
                socket.close();
            } catch(IOException e) {}
        }
    }

    public boolean reconnect(){
        StarGate.getInstance().getLogger().info("§cWARNING: Reconnecting §6"+name+"§c!");
        try {
            gatePacket(new ConnectionInfoPacket(){{
                packetType = CONNECTION_RECONNECT;
                isEncoded = false;
            }});

            /* We give some small time for client to make him receive message
            * and prepare for reconnecting*/
            Thread.sleep(450);

            in.close();
            out.close();
            socket.close();
        } catch(Exception e) {
            return  false;
        }
        return true;
    }

    public String gatePacket(StarGatePacket packet){
        String packetString;
        if (!packet.isEncoded) {
            packet.encode();
        }

        packetString = packet.encoded;
        String uuid = UUID.randomUUID().toString();

        try {
            out.println(packetString +"!"+ uuid);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return  uuid;
    }
}