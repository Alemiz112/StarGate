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

package alemiz.stargate.session;

import alemiz.stargate.handler.StarGatePacketHandler;
import alemiz.stargate.protocol.DisconnectPacket;
import alemiz.stargate.protocol.PongPacket;
import alemiz.stargate.protocol.StarGatePacket;
import alemiz.stargate.utils.StarGateLogger;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class StarGateSession {

    public static final long PING_INTERVAL = 30;
    public static final long PING_TIMEOUT = 5;

    protected final InetSocketAddress address;
    protected final Channel channel;
    protected final EventLoop eventLoop;

    protected StarGatePacketHandler packetHandler;
    protected final AtomicBoolean closed = new AtomicBoolean(false);

    public StarGateSession(InetSocketAddress address, Channel channel){
        this.address = address;
        this.channel = channel;
        this.eventLoop = channel.eventLoop();
        eventLoop.scheduleAtFixedRate(this::onTick, 50, 50, TimeUnit.MILLISECONDS);
    }

    public abstract void onPacket(StarGatePacket packet);
    protected abstract void onTick();

    public void onConnect(){
    }
    public abstract void onDisconnect(String reason);

    protected abstract void onPongReceive(PongPacket packet);

    public void sendPacket(StarGatePacket packet){
        this.forcePacket(packet);
    }

    public void forcePacket(StarGatePacket packet){
        if (!this.isClosed()){
            this.channel.writeAndFlush(packet);
        }
    }

    public void disconnect(DisconnectPacket.REASON reason){
        this.disconnect(reason.getMessage());
    }

    public void disconnect(@NonNull String reason){
        this.getLogger().info("Closing StarGate connection! Reason: "+reason);
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
}
