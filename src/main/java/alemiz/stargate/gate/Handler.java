package alemiz.stargate.gate;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.packets.ConnectionInfoPacket;
import alemiz.stargate.gate.packets.StarGatePacket;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class Handler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String name;
    private String closeReason = "unknown";

    private boolean authenticated = false;

    private long nextTick;

    private boolean isRunning = true;
    private boolean stable = true;


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
        this.nextTick = System.currentTimeMillis();

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }catch (Exception e){
            StarGate.getInstance().getLogger().info("§cERROR: Unable to create sockets for §6" + name + "§!");
            System.out.println(e.getMessage());
            return;
        }

        while (this.isRunning) {
            long now = System.currentTimeMillis();
            long time = now - this.nextTick;

            if (time < 0) {
                try {
                    Thread.sleep(Math.max(25, -time));
                } catch (InterruptedException e) {
                    StarGate.getInstance().getLogger().warning("§eError appear while ticking StarGate Client!");
                }
            }

            if (!this.tick()){
                StarGate.getInstance().getLogger().warning("§eSomething bad happened! Closing client §6"+this.name+"§e!");
                this.shutdown();
            }

            this.nextTick += 50;
        }

        try {
            this.in.close();
            this.out.close();
            this.socket.close();
        }catch (Exception e){
            //ignore
        }
    }

    /**
     * Returns if loop should be continued
     * @return boolean
     */
    private boolean tick(){
        if (!this.isAuthenticated()){
            try {
                this.authenticate();
            }catch (IOException e){
                return false;
            }
            return true;
        }

        try {
            String line = in.readLine();
            if (line == null || line.equals("GATE_STATUS")) return true;

            StarGatePacket packet = GateAPI.getGateServer().processPacket(name, line);
            if (packet == null) {
                StarGate.getInstance().getLogger().info("§cWARNING: Unknown packet!");
                return true;
            }

            if (packet instanceof ConnectionInfoPacket && ((ConnectionInfoPacket) packet).packetType == ConnectionInfoPacket.CONNECTION_CLOSED) {
                String reason = ((ConnectionInfoPacket) packet).reason;
                this.closeReason = (reason == null) ? closeReason : reason;
                this.shutdown();
                return true;
            }

        }catch (Exception e){
            if (e.getLocalizedMessage() != null && e.getLocalizedMessage().equals("Connection reset")){
                this.closeReason = "Connection reset";
                this.shutdown();
                return true;
            }

            StringBuilder report = new StringBuilder("§cERROR: Problem appears while processing packet!\n");
            report.append("§c").append(e.getClass().getName()).append(":").append(e.getLocalizedMessage()).append("\n");

            for (StackTraceElement line : e.getStackTrace()){
                report.append("§4").append(line).append("\n");
            }

            StarGate.getInstance().getLogger().info(report.toString());
        }

        return true;
    }

    private void authenticate() throws IOException {
        String handshake = this.in.readLine();
        if (handshake == null || !handshake.startsWith("CHEVRON:") || handshake.length() <= 8) return;

        String[] handshakeData = handshake.substring(8).split(":");
        name = handshakeData[0];

        /* Password not set*/
        if (handshakeData.length < 2 || !handshakeData[1].equals(Server.password)) {
            StarGate.getInstance().getLogger().warning("§aNew client attends to connect: §6" + name);
            StarGate.getInstance().getLogger().warning("§cClient not authenticated! Wrong password!");

            this.gatePacket(new ConnectionInfoPacket() {{
                packetType = CONNECTION_ABORTED;
                reason = WRONG_PASSWORD;
                isEncoded = false;
            }});

            this.shutdown();
            return;
        }

        Handler client;
        if (GateAPI.getGateServer().clients.containsKey(name) && (client = GateAPI.getGateServer().clients.get(name)) != null) {
            ConnectionInfoPacket packet = new ConnectionInfoPacket(){{
                packetType = CONNECTION_CLOSED;
                reason = "Connected from another location";
            }};

            client.gatePacket(packet, true);
            client.closeReason = "Connected from another location";
            client.shutdown();
            return;
        }

        /*Sending message to Client to confirm successful Connection*/
        this.gatePacket(new ConnectionInfoPacket() {{
            packetType = CONNECTION_CONNECTED;
            isEncoded = false;
        }});

        GateAPI.getGateServer().clients.put(name, this);
        GateAPI.ping(name);

        StarGate.getInstance().getLogger().info("§aNew client connected: §6" + name + " §aThread: §6" + Thread.currentThread().getName());
        StarGate.getInstance().getLogger().info("§aADDRESS: §e" + socket.getInetAddress().toString().replace("/", "") + ":" + socket.getPort());

        this.authenticated = true;
    }


    public boolean reconnect() {
        StarGate.getInstance().getLogger().info("§cWARNING: Reconnecting §6" + name + "§c!");
        this.gatePacket(new ConnectionInfoPacket() {{
            packetType = CONNECTION_RECONNECT;
            isEncoded = false;
        }});

        /* We give some small time for client to make him receive message
         * and prepare for reconnecting*/
        try {
            Thread.sleep(450);
        } catch (Exception e) {
            return false;
        }

        this.authenticated = false;
        this.shutdown();
        return true;
    }

    public String gatePacket(StarGatePacket packet){
        return this.gatePacket(packet, false);
    }

    public String gatePacket(StarGatePacket packet, boolean fireException) {
        String packetString;
        if (!packet.isEncoded) {
            packet.encode();
        }

        packetString = packet.encoded;
        String uuid = UUID.randomUUID().toString();

        try {
            this.out.println(packetString + "!" + uuid);
        } catch (Exception e) {
            if (!fireException) System.out.println(e.getMessage());
        }
        return uuid;
    }

    public void shutdown(){
        this.isRunning = false;

        GateAPI.getGateServer().clients.remove(name);
        StarGate.getInstance().getLogger().info("§cWARNING: Connection with §6"+name+"§c has been closed!");
        StarGate.getInstance().getLogger().info("§cReason: §4"+closeReason);
    }

    public boolean isShutdown(){
        return this.isRunning;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }


    /**
     *  Returns if last ping was successful
     */
    public boolean isStable() {
        return stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }
}