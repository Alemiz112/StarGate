package alemiz.stargate.gate.events;

import alemiz.stargate.gate.packets.StarGatePacket;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class PacketPreHandleEvent extends Event implements Cancellable {

    private StarGatePacket packet;
    private String client;

    private boolean cancelled;

    public PacketPreHandleEvent(String client, StarGatePacket packet){
        this.client = client;
        this.packet = packet;
    }

    /* Returns packet to be handled*/
    public StarGatePacket getPacket() {
        return packet;
    }

    /* Returns client, who sent the packet*/
    public String getClient() {
        return client;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(){
        this.cancelled = true;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
