package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class WelcomePacket extends StarGatePacket {

    public String server;
    public int tps;
    public int players;
    public int usage;

    public WelcomePacket(){
        super("WELCOME_PACKET", Packets.WELCOME_PACKET);
    }

    @Override
    public void decode() {
        /* This is very important! Server will try to decode packet if it will be not set correctly
        * And that can return unupdated packet*/
        isEncoded = false;

        /* data[0] => ID*/
        String[] data = Convertor.getPacketStringData(encoded);
        this.server = data[1];
        this.tps = Integer.decode(data[2]);
        this.usage = Integer.decode(data[3]);
        this.players = Integer.decode(data[4]);
    }

    /* Using @class Convertor we can create packetString from custom data
    * It supports custom converting methods
    * You must create new Conventor class then using dynamic functions you can putString|putInt|or other data
    * For more docs see GitHub documentation*/

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());
        convertor.putInt(ID);
        convertor.putString(server);

        convertor.putInt(tps);
        convertor.putInt(usage);

        convertor.putInt(players);

        this.encoded = convertor.getPacketString();
        isEncoded = true;
    }

    /* May be useful in feature references
    * It helps to clone packet with all its data 'super.clone()'*/

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        WelcomePacket packet = (WelcomePacket) super.clone();
        packet.players = players;
        packet.server = server;
        packet.tps = tps;
        packet.usage = usage;

        return  packet;
    }
}
