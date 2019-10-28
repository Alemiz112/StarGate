package alemiz.stargate.gate;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.events.CustomPacketEvent;
import alemiz.stargate.gate.packets.*;
import alemiz.stargate.gate.tasks.PingTask;
import alemiz.stargate.untils.gateprotocol.Convertor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.util.*;
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
    protected static String password = "123456789";


    protected Map<String, Handler> clients = new HashMap<>();
    protected Map<Integer, StarGatePacket> packets = new HashMap<>();

    protected ExecutorService clientPool;
    protected Thread serverThread;

    public Server(StarGate plugin){
        instance = this;
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
     * Here we are registering new Packets, may be useful for DEV
     * Every packet Extends @class StarGatePacket*/

    private void initPackets(){
        GateAPI.RegisterPacket(new WelcomePacket());
        GateAPI.RegisterPacket(new PingPacket());
        GateAPI.RegisterPacket(new PlayerTransferPacket());
        GateAPI.RegisterPacket(new KickPacket());
        GateAPI.RegisterPacket(new PlayerOnlinePacket());
        GateAPI.RegisterPacket(new ForwardPacket());
        GateAPI.RegisterPacket(new ConnectionInfoPacket());
    }

    private void initConfig(){
        port = plugin.cfg.getInt("port");
        maxConn = plugin.cfg.getInt("maxConnections");
        password = plugin.cfg.getString("password");
    }

    /* This function we use to send packet to Clients
     * You must specify Client name or Chevron and packet that will be sent*/

    protected String gatePacket(String client, StarGatePacket packet){
        if (!clients.containsKey(client) || clients.get(client) == null) return null;

        Handler clientHandler = clients.get(client);
        return clientHandler.gatePacket(packet);
    }

    /* Using these function we can process packet from string to data
    *  After packet is successfully created we can handle that Packet*/

    protected boolean processPacket(String client, String packetString) throws InstantiationException, IllegalAccessException{
        String[] data = Convertor.getPacketStringData(packetString);
        int PacketId = Integer.decode(data[0]);


        if (!packets.containsKey(PacketId) || packets.get(PacketId) == null) return false;

        /* Here we decode Packet. Create from String Data*/
        StarGatePacket packet = packets.get(PacketId).getClass().newInstance();
        String uuid = data[data.length - 1];


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
                packet.uuid = uuid;
                packet.encoded = packetString;
                break;
        }

        packet.decode();

        handlePacket(client, packet);
        //plugin.getLogger().info("§6"+packetString);
        return true;
    }

    private void handlePacket(String client, StarGatePacket packet){
        int type = packet.getID();

        ProxiedPlayer player;

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
                }/*else{ DEBUG STUFF
                    int ping = pingPacket.getPing();

                    plugin.getLogger().info("§bPING: §e"+ ping);
                }*/
                break;
            case Packets.PLAYER_TRANSFORM_PACKET:
                PlayerTransferPacket transferPacket = (PlayerTransferPacket) packet;
                player = transferPacket.getPlayer();

                if (player == null){
                    plugin.getLogger().info("§cWARNING: §bTransfer Packet => Player not found!");
                }else {
                    ServerInfo server = plugin.getProxy().getServerInfo(transferPacket.getDestination());

                    /*Prevent disconnecting client if server doesnt exist*/
                    if (server == null){
                        player.sendMessage(new TextComponent("§cCant connect to server §6"+transferPacket.getDestination()+"§c!"));
                        plugin.getLogger().info("§cWARNING: Player "+player.getName()+" was supposed to connect server that is unreachable!");
                        return;
                    }
                    player.connect(server);
                }

                break;
            case Packets.KICK_PACKET:
                KickPacket kickPacket = (KickPacket) packet;
                player = kickPacket.getPlayer();

                if (player == null){
                    plugin.getLogger().info("§cWARNING: §bKick Packet => Player not found!");
                }else {
                    String reason = StarGate.getInstance().colorText(kickPacket.getReason());
                    player.disconnect(new TextComponent(reason));
                }
                break;
            case Packets.PLAYER_ONLINE_PACKET:
                PlayerOnlinePacket onlinePacket = (PlayerOnlinePacket) packet;

                ProxiedPlayer guest = null;
                if (onlinePacket.getCustomPlayer() != null){
                    guest = ProxyServer.getInstance().getPlayer(onlinePacket.getCustomPlayer());
                }

                if (onlinePacket.getPlayer() != null && onlinePacket.getPlayer().isConnected()){
                    guest = onlinePacket.getPlayer();
                }

                if (guest == null){
                    GateAPI.setResponse(client, onlinePacket.getUuid(), "false");
                }else {
                    GateAPI.setResponse(client, onlinePacket.getUuid(), "true!"+guest.getServer().getInfo().getName());
                }

                break;
            case Packets.FORWARD_PACKET:
                ForwardPacket forwardPacket = (ForwardPacket) packet;
                String sendto = forwardPacket.getClient();

                if (!clients.containsKey(sendto) || (clients.get(sendto) == null)){
                    plugin.getLogger().info("§cWARNING: ForwardPacket => Client §6"+sendto+"§c isnt connected!");
                    return;
                }

                Handler handler = clients.get(sendto);
                String data = forwardPacket.getEncodedPacket();

                handler.getOut().println(data);
                break;

            default:
                /** Here we call Event that will send packet to DEVs plugin*/
                plugin.getProxy().getPluginManager().callEvent(new CustomPacketEvent(client, packet));
                break;
        }
    }

    /*public boolean isConnected(String client){
        if (!clients.containsKey(client)) return false;
        if (clients.get(client) == null || clients.get(client).getSocket().is) return false;
    }*/

    /* Server Data*/
    public Map<String, Handler> getClients() {
        return clients;
    }
}
