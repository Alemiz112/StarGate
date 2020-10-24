/**
 * Copyright 2020 WaterdogTEAM
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alemiz.stargate.client;

import alemiz.stargate.protocol.*;
import alemiz.stargate.protocol.types.PingEntry;
import alemiz.stargate.session.StarGateSession;
import alemiz.stargate.utils.StarGateLogger;
import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.util.internal.PlatformDependent;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientSession extends StarGateSession {

    private final StarGateClient client;
    private final Queue<StarGatePacket> queuedPackets = PlatformDependent.newMpscQueue();

    private PingEntry pingEntry;

    public ClientSession(InetSocketAddress address, Channel channel, StarGateClient client) {
        super(address, channel);
        this.client = client;
        this.packetHandler = new HandshakePacketHandler(this);
    }

    @Override
    public void onConnect() {
        HandshakePacket packet = new HandshakePacket();
        packet.setHandshakeData(this.client.getHandshakeData());
        this.forcePacket(packet);
        this.channel.pipeline().remove("timeout-handler");
    }

    @Override
    public void onPacket(StarGatePacket packet) {
        Preconditions.checkNotNull(packet);
        boolean handled = this.packetHandler != null && packet.handle(this.packetHandler);

        if (this.client.getCustomHandler() != null){
            try {
                if (packet.handle(this.client.getCustomHandler())){
                    handled = true;
                }
            }catch (Exception e){
                this.getLogger().error("Error occurred in custom packet handler!", e);
            }
        }

        if (!handled){
            this.getLogger().debug("Unhandled packet "+packet);
        }
    }

    @Override
    protected void onTick() {
        if (this.isClosed()){
            return;
        }

        StarGatePacket packet;
        while ((packet = this.queuedPackets.poll()) != null){
            this.forcePacket(packet);
        }

        long currentTime = System.currentTimeMillis();
        PingEntry pingEntry = this.pingEntry;
        if (pingEntry != null && currentTime >= pingEntry.getTimeout()){
            pingEntry.getFuture().completeExceptionally(new TimeoutException());
            this.pingEntry = null;
        }
    }

    @Override
    public void sendPacket(StarGatePacket packet) {
        this.queuedPackets.add(packet);
    }

    public CompletableFuture<PongPacket> pingServer(long timeout, TimeUnit util){
        if (this.pingEntry != null){
            return this.pingEntry.getFuture();
        }
        long now = System.currentTimeMillis();
        this.pingEntry = new PingEntry(new CompletableFuture<>(), now + util.toMillis(timeout));


        PingPacket packet = new PingPacket();
        packet.setPingTime(now);
        this.forcePacket(packet);
        return pingEntry.getFuture();
    }

    @Override
    protected void onPongReceive(PongPacket packet) {
        if (this.pingEntry == null){
            return;
        }

        packet.setPongTime(System.currentTimeMillis());
        this.pingEntry.getFuture().complete(packet);
    }

    @Override
    public void onDisconnect(String reason) {
        this.getLogger().info("StarGate server has been disconnected! Reason: "+reason);
        this.close();
    }

    public void reconnect(String reason, boolean send){
        if (send){
            DisconnectPacket packet = new DisconnectPacket();
            packet.setReason(reason);
            this.forcePacket(packet);
        }
        this.getLogger().info("Reconnecting to server! Reason: "+reason);
        this.close();
        this.client.connect();
    }

    @Override
    public StarGateLogger getLogger() {
        return this.client.getLogger();
    }

    public void setClosed(boolean closed){
        this.closed.set(closed);
    }

    public StarGateClient getClient() {
        return this.client;
    }
}
