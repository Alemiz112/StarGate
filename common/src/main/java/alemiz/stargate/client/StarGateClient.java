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

package alemiz.stargate.client;

import alemiz.stargate.pipeline.PacketDeEncoder;
import alemiz.stargate.codec.ProtocolCodec;
import alemiz.stargate.pipeline.UnhandledPacketConsumer;
import alemiz.stargate.protocol.DisconnectPacket;
import alemiz.stargate.protocol.StarGatePacket;
import alemiz.stargate.protocol.types.HandshakeData;
import alemiz.stargate.handler.SessionHandler;
import alemiz.stargate.utils.ServerLoader;
import alemiz.stargate.utils.StarGateLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StarGateClient extends Thread {

    private final ServerLoader loader;
    private final InetSocketAddress address;
    private final HandshakeData handshakeData;

    private final ProtocolCodec protocolCodec;

    private final EventLoopGroup eventLoopGroup;
    private ClientSession session;

    private StarGateClientListener clientListener;
    private final List<SessionHandler<ClientSession>> customHandlers = new ObjectArrayList<>();

    public StarGateClient(InetSocketAddress address, HandshakeData handshakeData, ServerLoader loader) {
        this(address, handshakeData, loader, new NioEventLoopGroup(0, new DefaultThreadFactory("stargate")));
    }

    public StarGateClient(InetSocketAddress address, HandshakeData handshakeData, ServerLoader loader, EventLoopGroup eventLoopGroup) {
        this.loader = loader;
        this.address = address;
        this.handshakeData = handshakeData;
        this.protocolCodec = new ProtocolCodec();
        this.eventLoopGroup = eventLoopGroup;
        this.setName("StarGate Client #"+handshakeData.getClientName());
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void run() {
        this.connect();
    }

    public void connect() {
        if (this.isConnected()){
            return;
        }

        this.getLogger().debug("Connecting to StarGate server "+this.address);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(this.eventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ClientSessionInitializer(this));
            bootstrap.connect(this.address).get();
        } catch (Exception e) {
            this.getLogger().error("Can not connect to StarGate server!", e);
        }
    }

    public void onConnect(InetSocketAddress address, ChannelHandlerContext ctx) {
        this.getLogger().info("Client " + this.getClientName() + " has connected!");

        this.session = new ClientSession(address, ctx.channel(), this);
        ctx.pipeline().addLast(UnhandledPacketConsumer.NAME, new UnhandledPacketConsumer(session));

        this.session.onConnect();
        if (this.clientListener != null){
            this.clientListener.onSessionCreated(address, session);
        }
    }

    public void onDisconnect(){
        if (this.session == null || !this.session.close()){
            return;
        }

        this.getLogger().warn("StarGate client "+this.getClientName()+" has been disconnected!");
        if (this.clientListener != null){
            this.clientListener.onSessionDisconnected(this.session);
        }
    }

    public void sendPacket(StarGatePacket packet){
        if (this.session != null){
            this.session.sendPacket(packet);
        }
    }

    public CompletableFuture<StarGatePacket> responsePacket(StarGatePacket packet){
        if (this.session != null){
            return this.session.responsePacket(packet);
        }
        return null;
    }

    public void shutdown() {
        if (!this.isConnected()){
            return;
        }

        if (this.session != null){
            this.session.disconnect(DisconnectPacket.REASON.CLIENT_SHUTDOWN);
        }
    }

    public ServerLoader getLoader() {
        return this.loader;
    }

    public StarGateLogger getLogger() {
        return this.loader.getStarGateLogger();
    }

    public HandshakeData getHandshakeData() {
        return this.handshakeData;
    }

    public boolean isConnected() {
        return this.session != null && !this.session.isClosed();
    }

    public ProtocolCodec getProtocolCodec() {
        return this.protocolCodec;
    }

    protected void setSession(ClientSession session) {
        this.session = session;
    }

    public ClientSession getSession() {
        return this.session;
    }

    public String getClientName(){
        return this.handshakeData.getClientName();
    }

    @Deprecated
    public void setCustomHandler(SessionHandler<ClientSession> customHandler) {
        this.customHandlers.add(customHandler);
    }

    public void addCustomHandler(SessionHandler<ClientSession> customHandler) {
        this.customHandlers.add(customHandler);
    }

    public boolean removeCustomHandler(SessionHandler<ClientSession> customHandler) {
        return this.customHandlers.remove(customHandler);
    }

    public void clearCustomHandlers() {
        this.customHandlers.clear();
    }

    public List<SessionHandler<ClientSession>> getCustomHandlers() {
        return this.customHandlers;
    }

    public void setClientListener(StarGateClientListener clientListener) {
        this.clientListener = clientListener;
    }

    public StarGateClientListener getClientListener() {
        return this.clientListener;
    }

    private static class ClientSessionInitializer extends ChannelInitializer<SocketChannel> {

        private final StarGateClient client;

        public ClientSessionInitializer(StarGateClient client) {
            this.client = client;
        }

        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast(PacketDeEncoder.NAME, new PacketDeEncoder(this.client.getProtocolCodec(), this.client.getLogger()));
            pipeline.addLast("timeout-handler", new ReadTimeoutHandler(20));
            pipeline.addLast(new ClientChannelHandler(this.client));
        }

    }
}
