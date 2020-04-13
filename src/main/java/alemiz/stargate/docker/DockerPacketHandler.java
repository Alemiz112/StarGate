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
                if ((conn = createConnection(packet.getDockerHost())) == null) return;
                conn.removeContainer(packet.getContainerId(), true);
                break;
            case ServerManagePacket.DOCKER_START:
                if ((conn = createConnection(packet.getDockerHost())) == null) return;
                conn.startContainer(packet.getContainerId());
                break;
            case ServerManagePacket.DOCKER_STOP:
                if ((conn = createConnection(packet.getDockerHost())) == null) return;
                conn.stopContainer(packet.getContainerId());
                break;
        }
    }

    public static DockerConnection createConnection(String dockerHost){
        if (dockerHost == null) dockerHost = "default";
        String[] data = StarGate.getInstance().cfg.getString("dockerHosts."+dockerHost).split(":");

        if (data.length == 0) return null;
        return new DockerConnection(data[0], data[1]).connect();
    }


    private static void dockerAddServer(ServerManagePacket packet, String client){
        DockerConnection conn = createConnection(packet.getDockerHost());
        if (conn == null) {
            packet.setResponse(client, "STATUS_FAILED,HOST_NOT_FOUND");
            return;
        }

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
