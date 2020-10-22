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

import alemiz.stargate.handler.PacketDecoder;
import alemiz.stargate.handler.PacketEncoder;
import alemiz.stargate.codec.ProtocolCodec;
import alemiz.stargate.protocol.DisconnectPacket;
import alemiz.stargate.protocol.StarGatePacket;
import alemiz.stargate.protocol.types.HandshakeData;
import alemiz.stargate.session.SessionHandler;
import alemiz.stargate.utils.ServerLoader;
import alemiz.stargate.utils.StarGateLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;

public class StarGateClient extends Thread {

    private final ServerLoader loader;
    private final InetSocketAddress address;
    private final HandshakeData handshakeData;

    private final ProtocolCodec protocolCodec;

    private final EventLoopGroup eventLoopGroup;
    private ClientSession session;

    private StarGateClientListener clientListener;
    private SessionHandler<ClientSession> customHandler;

    public StarGateClient(InetSocketAddress address, HandshakeData handshakeData, ServerLoader loader) {
        this.loader = loader;
        this.address = address;
        this.handshakeData = handshakeData;
        this.protocolCodec = new ProtocolCodec();
        this.eventLoopGroup = new NioEventLoopGroup();

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

    public void onConnect(InetSocketAddress address, ChannelHandlerContext ctx){
        this.getLogger().info("Client "+this.getClientName()+" has connected!");
        this.session = new ClientSession(address, ctx.channel(), this);
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

    public void setCustomHandler(SessionHandler<ClientSession> customHandler) {
        this.customHandler = customHandler;
    }

    public SessionHandler<ClientSession> getCustomHandler() {
        return this.customHandler;
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
            pipeline.addLast(new PacketDecoder(this.client.getProtocolCodec()));
            pipeline.addLast(new PacketEncoder(this.client.getProtocolCodec()));
            pipeline.addLast("timeout-handler", new ReadTimeoutHandler(20));
            pipeline.addLast(new ClientChannelHandler(this.client));
        }

    }
}
