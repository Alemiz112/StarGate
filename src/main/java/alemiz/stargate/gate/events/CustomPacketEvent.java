package alemiz.stargate.gate.events;

import alemiz.stargate.gate.packets.StarGatePacket;
import net.md_5.bungee.api.plugin.Event;

public class CustomPacketEvent extends Event{

    private StarGatePacket packet;
    private String client;

    public CustomPacketEvent(String client, StarGatePacket packet){
        this.client = client;
        this.packet = packet;
    }

    /* Returns unofficial packet handled by server*/
    public StarGatePacket getPacket() {
        return packet;
    }

    /* Returns client, who sent that unofficial packet*/
    public String getClient() {
        return client;
    }
}
