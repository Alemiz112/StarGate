package alemiz.stargate.docker;

import alemiz.stargate.StarGate;
import alemiz.stargate.gate.GateAPI;
import alemiz.stargate.gate.packets.ServerManagePacket;

import java.net.InetSocketAddress;

public class DockerPacketHandler {

    public static void handle(ServerManagePacket packet, String client){
        DockerConnection conn;

        switch (packet.getPacketType()){
            case ServerManagePacket.DOCKER_ADD:
                dockerAddServer(packet, client);
                break;
            case ServerManagePacket.DOCKER_REMOVE:
                conn = createConnection(packet.getDockerHost());
                conn.removeContainer(packet.getContainerId(), true);
                break;
            case ServerManagePacket.DOCKER_START:
                conn = createConnection(packet.getDockerHost());
                conn.startContainer(packet.getContainerId());
                break;
            case ServerManagePacket.DOCKER_STOP:
                conn = createConnection(packet.getDockerHost());
                conn.stopContainer(packet.getContainerId());
                break;
        }
    }

    public static DockerConnection createConnection(String dockerHost){
        if (dockerHost == null) dockerHost = "default";
        String[] data = StarGate.getInstance().cfg.getString("dockerHosts."+dockerHost).split(":");

        return new DockerConnection(data[0], data[1]).connect();
    }


    private static void dockerAddServer(ServerManagePacket packet, String client){
        DockerConnection conn = createConnection(packet.getDockerHost());

        String containerId = conn.createContainer(packet.getContainerImage(), packet.getExposedPorts(), packet.getEnvVariables());
        if (containerId == null){
            packet.setResponse(client, "STATUS_FAILED");
            return;
        }

        InetSocketAddress address;
        try {
            address = new InetSocketAddress(packet.getServerAddress(), Integer.parseInt(packet.getServerPort()));
        }catch (Exception e){
            StarGate.getInstance().getLogger().warning("ERROR: Unable to create InetAddress!");
            packet.setResponse(client, "STATUS_FAILED_BAD_IP,"+containerId);
            return;
        }

        GateAPI.addServer(packet.getServerName(), address, packet.getServerAddress());
        packet.setResponse(client, "STATUS_SUCCESS,"+containerId);
    }
}
