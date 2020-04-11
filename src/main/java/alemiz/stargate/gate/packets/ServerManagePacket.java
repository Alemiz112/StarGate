package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class ServerManagePacket extends StarGatePacket {

    public static final int SERVER_ADD = 0;
    public static final int SERVER_REMOVE = 1;

    public static final int DOCKER_ADD = 2;
    public static final int DOCKER_REMOVE = 3;
    public static final int DOCKER_START = 4;
    public static final int DOCKER_STOP = 5;


    public int packetType;
    public String serverAddress;
    public String serverPort;
    public String serverName;

    public String dockerHost; //Name of host defined in proxy config.yml
    public String containerImage;
    public String containerId;
    public String[] exposedPorts; //19135/19132,25566/25565...
    public String[] envVariables; //VARIABLE=VALUE,VARIABLE2...

    public ServerManagePacket(){
        super("SERVER_MANAGE_PACKET", Packets.SERVER_MANAGE_PACKET);
    }

    @Override
    public void decode() throws Exception {
        this.isEncoded = false;

        String[] data = Convertor.getPacketStringData(encoded);
        try {
            this.packetType = Integer.parseInt(data[1]);
        } catch (Exception e){
            throw new Exception("Variable PacketType must be numerical value!");
        }

        switch (this.packetType){
            case SERVER_ADD:
                this.serverAddress = data[2];
                this.serverPort = data[3];
                this.serverName = data[4];
                break;
            case SERVER_REMOVE:
                this.serverName = data[2];
                break;
            case DOCKER_ADD:
                this.serverAddress = data[2];
                this.serverPort = data[3];
                this.serverName = data[4];
                this.containerImage = data[5];
                this.exposedPorts = data[6].split(",");
                this.exposedPorts = data[7].split(",");
                if (data.length > 9){
                    this.dockerHost = data[8];
                }
                break;
            case DOCKER_REMOVE:
            case DOCKER_START:
            case DOCKER_STOP:
                this.containerId = data[2];
                if (data.length > 4){
                    this.dockerHost = data[3];
                }
                break;
        }
    }

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());
        convertor.putInt(this.packetType);

        switch (this.packetType){
            case SERVER_ADD:
                convertor.putString(this.serverAddress);
                convertor.putString(this.serverPort);
                convertor.putString(this.serverName);
                break;
            case SERVER_REMOVE:
                convertor.putString(this.serverName);
                break;
            case DOCKER_ADD:
                convertor.putString(this.serverAddress);
                convertor.putString(this.serverPort);
                convertor.putString(this.serverName);
                convertor.putString(this.containerImage);
                convertor.putString(String.join(",", this.exposedPorts));
                convertor.putString(String.join(",", this.envVariables));
                if (this.dockerHost != null) convertor.putString(dockerHost);
                break;
            case DOCKER_REMOVE:
            case DOCKER_START:
            case DOCKER_STOP:
                convertor.putString(this.containerId);
                if (this.dockerHost != null) convertor.putString(dockerHost);
                break;
        }

        this.encoded = convertor.getPacketString();
        this.isEncoded = true;
    }

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        return null;
    }

    public int getPacketType() {
        return packetType;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getServerPort() {
        return serverPort;
    }

    public String getServerName() {
        return serverName;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public String getContainerId() {
        return containerId;
    }

    public String[] getExposedPorts() {
        return exposedPorts;
    }

    public String[] getEnvVariables() {
        return envVariables;
    }

    public String getDockerHost() {
        return dockerHost;
    }
}
