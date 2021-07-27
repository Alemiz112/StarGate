/*
 * Copyright 2020 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package alemiz.stargate.server;

import alemiz.stargate.protocol.*;
import alemiz.stargate.protocol.types.PingEntry;
import alemiz.stargate.handler.SessionHandler;
import alemiz.stargate.StarGateSession;
import alemiz.stargate.protocol.types.HandshakeData;
import alemiz.stargate.utils.StarGateLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.PlatformDependent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerSession extends StarGateSession {

    private final StarGateServer server;
    private final Queue<StarGatePacket> queuedPackets = PlatformDependent.newMpscQueue();

    private final List<SessionHandler<?>> customHandlers = new ObjectArrayList<>();
    private final AtomicBoolean authenticated = new AtomicBoolean(false);

    private HandshakeData handshakeData;
    private PingEntry pingEntry;

    public ServerSession(InetSocketAddress address, Channel channel, StarGateServer server){
        super(address, channel);
        this.server = server;
        this.eventLoop.scheduleAtFixedRate(this::onPingHook, StarGateSession.PING_INTERVAL, StarGateSession.PING_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public boolean onPacket(StarGatePacket packet) {
        if (super.onPacket(packet)) {
            return true;
        }

        if (!this.customHandlers.isEmpty()) {
            try {
                for (SessionHandler<?> handler : this.customHandlers) {
                    if (packet.handle(handler)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                this.getLogger().error("Error occurred in custom packet handler!", e);
            }
        }
        return false;
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

    public void sendRaw(ByteBuf buffer){
        if (!this.isClosed()){
            this.channel.writeAndFlush(buffer);
        }
    }

    private void onPingHook(){
        if (this.isClosed()){
            return;
        }

        this.pingClient(StarGateSession.PING_TIMEOUT, TimeUnit.SECONDS).whenComplete((PongPacket reply, Throwable error)-> {
            if (error != null){
                this.requestReconnect("Ping Timeout");
            }
            this.getLogger().debug("Client "+this.getSessionName()+" received ping "+(reply.getPongTime() - reply.getPingTime())+"ms!");
        });
    }

    public CompletableFuture<PongPacket> pingClient(long timeout, TimeUnit util){
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
    public void onPongReceive(PongPacket packet) {
        if (this.pingEntry == null){
            return;
        }

        packet.setPongTime(System.currentTimeMillis());
        this.pingEntry.getFuture().complete(packet);
        this.pingEntry = null;
    }

    @Override
    public void onDisconnect(String reason) {
        if (this.isAuthenticated()){
            this.getLogger().info("StarGate client "+this.getSessionName()+" has disconnected! Reason: "+reason);
        }
        this.close();
    }

    @Override
    public void disconnect(@NonNull String reason) {
        if (this.isAuthenticated()){
            this.getLogger().info("Disconnecting client "+this.getSessionName());
        }
        super.disconnect(reason);
    }

    public void requestReconnect(@NonNull String reason){
        this.getLogger().info("Requesting client reconnection from "+this.getSessionName()+"! Reason: "+reason);
        ReconnectPacket packet = new ReconnectPacket();
        packet.setReason(reason);
        this.forcePacket(packet);
        this.close();
    }

    public StarGateServer getServer() {
        return this.server;
    }

    @Override
    public StarGateLogger getLogger() {
        return this.server.getLogger();
    }

    @Deprecated
    public String getClientName() {
        return this.getSessionName();
    }

    @Override
    public String getSessionName() {
        return this.handshakeData.getClientName();
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated.set(authenticated);
    }

    public boolean isAuthenticated() {
        return this.authenticated.get();
    }


    public void setHandshakeData(HandshakeData handshakeData) {
        if (this.handshakeData == null){
            this.handshakeData = handshakeData;
        }
    }

    public HandshakeData getHandshakeData() {
        return this.handshakeData;
    }

    @Deprecated
    public void setCustomHandler(SessionHandler<?> customHandler) {
        this.customHandlers.add(customHandler);
    }

    @Override
    public void addCustomHandler(SessionHandler<?> customHandler) {
        this.customHandlers.add(customHandler);
    }

    @Override
    public boolean removeCustomHandler(SessionHandler<?> customHandler) {
        return this.customHandlers.remove(customHandler);
    }

    public void clearCustomHandlers() {
        this.customHandlers.clear();
    }

    public List<SessionHandler<?>> getCustomHandlers() {
        return this.customHandlers;
    }
}
