package alemiz.stargate.gate.packets;

public interface Packets {

    int WELCOME_PACKET = 0x01;
    int PING_PACKET = 0x02;
    int PLAYER_TRANSFORM_PACKET = 0x03;
    int KICK_PACKET = 0x04;
    int PLAYER_ONLINE_PACKET = 0x05;
    int FORWARD_PACKET = 0x06;
    int CONNECTION_INFO_PACKET = 0x07;

    int SERVER_MANAGE_PACKET = 0x10;
}
