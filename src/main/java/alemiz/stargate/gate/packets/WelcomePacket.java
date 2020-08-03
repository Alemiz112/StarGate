package alemiz.stargate.gate.packets;

import alemiz.stargate.untils.gateprotocol.Convertor;

public class WelcomePacket extends StarGatePacket {

    public String server;
    public int tps;
    public int players;
    public int maxPlayers;
    public int usage;

    public WelcomePacket(){
        super("WELCOME_PACKET", Packets.WELCOME_PACKET);
    }

    @Override
    public void decode() {
        /* This is very important! Server will try to decode packet if it will be not set correctly
        * And that can return un-updated packet*/
        this.isEncoded = false;

        /* data[0] => ID */
        String[] data = Convertor.getPacketStringData(encoded);
        this.server = data[1];
        this.tps = Integer.parseInt(data[2]);
        this.usage = Integer.parseInt(data[3]);
        this.players = Integer.parseInt(data[4]);
        this.maxPlayers = Integer.parseInt(data[5]);
    }

    /* Using @class Convertor we can create packetString from custom data
    * It supports custom converting methods
    * You must create new Conventor class then using dynamic functions you can putString|putInt|or other data
    * For more docs see GitHub documentation*/

    @Override
    public void encode() {
        Convertor convertor = new Convertor(getID());
        convertor.putString(this.server);

        convertor.putInt(this.tps);
        convertor.putInt(this.usage);
        convertor.putInt(this.players);
        convertor.putInt(this.maxPlayers);

        this.encoded = convertor.getPacketString();
        this.isEncoded = true;
    }

    /* May be useful in feature references
    * It helps to clone packet with all its data 'super.clone()'*/

    @Override
    public StarGatePacket copy() throws CloneNotSupportedException {
        WelcomePacket packet = (WelcomePacket) super.clone();
        packet.players = this.players;
        packet.server = this.server;
        packet.tps = this.tps;
        packet.usage = this.usage;
        return packet;
    }
}
