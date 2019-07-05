package alemiz.stargate.gate;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.events.CustomPacketEvent;
import alemiz.stargate.gate.packets.*;
import alemiz.stargate.gate.tasks.PingTask;
import alemiz.stargate.untils.gateprotocol.Convertor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    protected StarGate plugin;

    private static Server instance;
    private static GateAPI gateAPI;

    /**
     * Settings of StarGate protocol, communication services and more
     * These services are running on separated threads, so performance will not get DOWN
     */
    protected static int port = 47007;
    protected static int maxConn = 50;


    protected Map<String, Handler> clients = new HashMap<>();
    protected Map<Integer, StarGatePacket> packets = new HashMap<>();

    protected ExecutorService clientPool;
    protected Thread serverThread;

    public Server(StarGate plugin){
        this.plugin = plugin;

        gateAPI = new GateAPI(this);

        initConfig();
        initPackets();
        start();
    }

    public static Server getInstance(){
        return instance;
    }

    public void start(){
        plugin.getLogger().info("§aStarting StarGate Protocol on Port: §2"+port);
        clientPool = Executors.newFixedThreadPool(50);

        Runnable serverTask = new Runnable(){
            @Override
            public void run() {
                try (ServerSocket listener = new ServerSocket(47007)) {
                    plugin.getLogger().info("§cDone! §aStarGate Protocol is successfully running. Waiting for clients...");

                    while (true) {
                        Handler client = new Handler(listener.accept());
                        clientPool.execute(client);
                    }
                }catch (IOException e) {
                    plugin.getLogger().info("§cERROR: Connection refused!\n§r" +e.getMessage());
                }
            }
        };

        /* Here we are creating new Thread for Server only
        * Every client has its own Thread*/

        serverThread = new Thread(serverTask);
        serverThread.start();

        /* Launching PingTask is very easy
        * Just set delay (60seconds) and launch task*/
        long interval = 60 * 1000;
        Timer timer = new Timer();
        timer.schedule(new PingTask(), 0, interval);
    }

    /**
     * Here we are registring new Packets, may be useful for DEV
     * Every packet Extends @class StarGatePacket*/

    private void initPackets(){
        GateAPI.RegisterPacket(new WelcomePacket());
        GateAPI.RegisterPacket(new PingPacket());
        GateAPI.RegisterPacket(new PlayerTransferPacket());
    }

    private void initConfig(){
        port = plugin.cfg.getInt("StarGate.port");
        maxConn = plugin.cfg.getInt("StarGate.maxConnections");
    }

    /* This function we use to send packet to Clients
     * You must specify Client name or Chevron and packet that will be sent*/

    protected void gatePacket(String client, StarGatePacket packet){
        if (!clients.containsKey(client) || clients.get(client) == null) return;

        Handler clientHandler = clients.get(client);
        clientHandler.gatePacket(packet);
    }

    /* Using these function we can process packet from string to data
    *  After packet is successfully created we can handle that Packet*/

    protected boolean processPacket(String client, String packetString){
        String[] data = Convertor.getPacketStringData(packetString);
        int PacketId = Integer.decode(data[0]);

        if (!packets.containsKey(PacketId) || packets.get(PacketId) == null) return false;

        /* Here we decode Packet. Create from String Data*/
        StarGatePacket packet = packets.get(PacketId);


        /* Preprocessing official packets if its needed.
        *  Great example is PingPacket - we receive NanoTime and converts it to MilliSeconds */
        switch (packet.getID()){
            case Packets.PING_PACKET:
                long actualTime = System.nanoTime();
                long startTime = Long.decode(data[1]);

                //long ping = (actualTime-startTime) / 1_000_000_000; => Old Format
                long ping = TimeUnit.NANOSECONDS.toMillis((actualTime-startTime));

                data[1] = Long.toString(ping);

                packet.encoded = Convertor.getPacketString(data);
                break;
            default:
                packet.encoded = packetString;
                break;
        }

        packet.decode();

        handlePacket(client, packet);
        return true;
    }

    private void handlePacket(String client, StarGatePacket packet){
        int type = packet.getID();

        switch (type){
            case Packets.WELCOME_PACKET:
                WelcomePacket welcomePacket = (WelcomePacket) packet;
                plugin.getLogger().info("§bReceiving first data from §e"+welcomePacket.server);
                plugin.getLogger().info("§bUSAGE: §e"+welcomePacket.usage+"%§b TPS: §e"+welcomePacket.tps+" §bPLAYERS: §e"+welcomePacket.players);
                break;
            case Packets.PING_PACKET:
                PingPacket pingPacket = (PingPacket) packet;
                long delay = TimeUnit.SECONDS.toMillis(30);

                if (pingPacket.getPing() > delay){
                    Handler handler = clients.get(client);

                    int ping = pingPacket.getPing();
                    plugin.getLogger().info("§bConnection with §e"+client+" §b is slow! Ping: §e"+ping+"ms");

                    if (!handler.reconnect()){
                        plugin.getLogger().info("§cERROR: Reconnecting with §6"+client+"§cwas interrupted!");
                        plugin.getLogger().info("§cTrying to establish new connection with §6"+client);
                    }

                    clients.remove(client);
                }else{
                    int ping = pingPacket.getPing();
                    plugin.getLogger().info("§bPING: §e"+ ping);
                }
                break;
            case Packets.PLAYER_TRANSFORM_PACKET:
                PlayerTransferPacket transferPacket = (PlayerTransferPacket) packet;
                ProxiedPlayer player = transferPacket.getPlayer();

                if (player == null){
                    plugin.getLogger().info("§cWARNING: §bTransfer Packet => Player not found!");
                }else {
                    ServerInfo server = plugin.getProxy().getServerInfo(transferPacket.getDestination());
                    player.connect(server);
                }

                break;
            default:
                /** Here we call Event that will send packet to DEVs plugin*/
                plugin.getProxy().getPluginManager().callEvent(new CustomPacketEvent(client, packet));
                break;
        }
    }

    /* Server Data*/

    public Map<String, Handler> getClients() {
        return clients;
    }
}
