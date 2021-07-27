/*
 * Copyright 2021 Alemiz
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package alemiz.stargate;

import alemiz.stargate.handler.SessionHandler;
import alemiz.stargate.handler.StarGatePacketHandler;
import alemiz.stargate.protocol.DisconnectPacket;
import alemiz.stargate.protocol.PongPacket;
import alemiz.stargate.protocol.StarGatePacket;
import alemiz.stargate.utils.StarGateLogger;
import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class StarGateSession {

    public static final long PING_INTERVAL = 30;
    public static final long PING_TIMEOUT = 5;

    protected final InetSocketAddress address;
    protected final Channel channel;
    protected final EventLoop eventLoop;

    protected AtomicInteger responseCounter = new AtomicInteger(0);
    protected Int2ObjectMap<CompletableFuture<StarGatePacket>> pendingResponses = new Int2ObjectOpenHashMap<>();

    protected StarGatePacketHandler packetHandler;
    protected final AtomicBoolean closed = new AtomicBoolean(false);

    protected int logInputLevel;
    protected int logOutputLevel;

    public StarGateSession(InetSocketAddress address, Channel channel){
        this.address = address;
        this.channel = channel;
        this.eventLoop = channel.eventLoop();
        eventLoop.scheduleAtFixedRate(this::onTick, 50, 50, TimeUnit.MILLISECONDS);
    }

    public boolean onPacket(StarGatePacket packet) {
        if (packet == null) {
            throw new NullPointerException("Tried to handle NULL");
        }
        boolean handled = this.packetHandler != null && packet.handle(this.packetHandler);

        if (packet.isResponse()) {
            CompletableFuture<StarGatePacket> future = this.pendingResponses.remove(packet.getResponseId());
            if (future != null) {
                future.complete(packet);
                handled = true;
            }
        }

        if (this.logInputLevel >= packet.getLogLevel()) {
            this.getLogger().debug("Received " + packet);
        }
        return handled;
    }

    protected abstract void onTick();

    public void onConnect(){
    }
    public abstract void onDisconnect(String reason);

    protected abstract void onPongReceive(PongPacket packet);

    public CompletableFuture<StarGatePacket> responsePacket(StarGatePacket packet){
        if (!packet.sendsResponse()){
            return null;
        }
        int responseId = this.responseCounter.getAndIncrement();
        packet.setResponseId(responseId);

        this.sendPacket(packet);
        return this.pendingResponses.computeIfAbsent(responseId, i -> new CompletableFuture<>());
    }

    public void sendPacket(StarGatePacket packet){
        this.forcePacket(packet);
    }

    public void forcePacket(StarGatePacket packet){
        if (!this.isClosed()){
            this.channel.writeAndFlush(packet);
        }
        if (this.logOutputLevel >= packet.getLogLevel()){
            this.getLogger().debug("Sent "+packet);
        }
    }

    public void disconnect(DisconnectPacket.REASON reason){
        this.disconnect(reason.getMessage());
    }

    public void disconnect(@NonNull String reason){
        this.getLogger().info("Closing StarGate connection! Reason: " + reason);
        DisconnectPacket packet = new DisconnectPacket();
        packet.setReason(reason);
        this.forcePacket(packet);
        this.close();
    }

    public boolean close(){
        if (!this.closed.compareAndSet(false, true)){
            return false;
        }
        this.channel.close();
        this.getLogger().debug("Closed StarGate session "+this.address);
        return true;
    }

    public abstract StarGateLogger getLogger();

    public boolean isClosed() {
        return this.closed.get();
    }

    public AtomicBoolean getClosed() {
        return this.closed;
    }

    public InetSocketAddress getAddress() {
        return this.address;
    }

    public void setPacketHandler(StarGatePacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public StarGatePacketHandler getPacketHandler() {
        return this.packetHandler;
    }

    public abstract void addCustomHandler(SessionHandler<?> customHandler);

    public abstract boolean removeCustomHandler(SessionHandler<?> customHandler);

    public void setLogInputLevel(int logInputLevel) {
        this.logInputLevel = logInputLevel;
    }

    public int getLogInputLevel() {
        return this.logInputLevel;
    }

    public void setLogOutputLevel(int logOutputLevel) {
        this.logOutputLevel = logOutputLevel;
    }

    public int getLogOutputLevel() {
        return this.logOutputLevel;
    }

    public abstract String getSessionName();

    public Channel getChannel() {
        return this.channel;
    }
}
