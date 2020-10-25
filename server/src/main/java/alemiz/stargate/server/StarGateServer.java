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


import alemiz.stargate.codec.ProtocolCodec;
import alemiz.stargate.handler.PacketDecoder;
import alemiz.stargate.handler.PacketEncoder;
import alemiz.stargate.protocol.DisconnectPacket;
import alemiz.stargate.server.handler.ServerChannelHandler;
import alemiz.stargate.utils.ServerLoader;
import alemiz.stargate.utils.StarGateLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class StarGateServer extends Thread {

    private final ServerLoader loader;
    private final InetSocketAddress bindAddress;

    private final ProtocolCodec protocolCodec;
    private final EventLoopGroup bossLoopGroup;
    private final EventLoopGroup eventLoopGroup;

    private final Map<InetSocketAddress, ServerSession> starGateSessionMap = new ConcurrentHashMap<>();
    private StarGateServerListener serverListener;

    private final String password;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public StarGateServer(InetSocketAddress bindAddress, String password, ServerLoader loader){
        this.loader = loader;
        this.bindAddress = bindAddress;
        this.bossLoopGroup = new NioEventLoopGroup();
        this.eventLoopGroup = new NioEventLoopGroup();
        this.protocolCodec = new ProtocolCodec();
        this.password = password;
    }

    @Override
    public void run() {
        this.boot();
    }

    private void boot(){
        this.getLogger().info("Binding StarGate server to "+this.bindAddress);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(this.bossLoopGroup, this.eventLoopGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(new StarGateServerInitializer(this));

            bootstrap.bind(this.bindAddress);
        }catch (Exception e){
            this.getLogger().error("StarGate can't be bind to "+this.bindAddress, e);
        }
    }

    @SneakyThrows
    public void shutdown(){
        for (ServerSession session : this.starGateSessionMap.values()){
            session.disconnect(DisconnectPacket.REASON.SERVER_SHUTDOWN);
        }
        Thread.sleep(500); // Give some time
        this.interrupt();
    }


    //TODO: check for opened sessions

    public void onSessionCreation(InetSocketAddress address, ChannelHandlerContext ctx){
        ServerSession session = new ServerSession(address, ctx.channel(), this);
        boolean success = this.serverListener == null || this.serverListener.onSessionCreated(address, session);

        if (success){
            this.getLogger().debug("New StarGate session was created "+address);
            this.starGateSessionMap.put(address, session);
            session.onConnect();
        }else {
            session.close();
        }
    }

    public void onSessionAuthenticated(ServerSession session){
        if (this.serverListener != null){
            this.serverListener.onSessionAuthenticated(session);
        }
    }

    public void onSessionDisconnect(InetSocketAddress address, ServerSession session){
        if (this.serverListener != null){
            this.serverListener.onSessionDisconnected(session);
        }
        this.starGateSessionMap.remove(address);
    }

    public ServerLoader getLoader() {
        return this.loader;
    }

    public StarGateLogger getLogger(){
        return this.loader.getStarGateLogger();
    }

    public InetSocketAddress getBindAddress() {
        return this.bindAddress;
    }

    public EventLoopGroup getEventLoopGroup() {
        return this.eventLoopGroup;
    }

    public ProtocolCodec getProtocolCodec() {
        return this.protocolCodec;
    }

    public ServerSession getSession(InetSocketAddress address){
        return this.starGateSessionMap.get(address);
    }

    public ServerSession getSession(String clientName){
        for (ServerSession session : this.starGateSessionMap.values()){
            if (session.getHandshakeData() != null && session.getClientName().equals(clientName)){
                return session;
            }
        }
        return null;
    }

    public Map<InetSocketAddress, ServerSession> getSessions() {
        return this.starGateSessionMap;
    }

    public boolean isShutdown(){
        return this.shutdown.get();
    }

    public String getPassword() {
        return this.password;
    }

    public void setServerListener(StarGateServerListener serverListener) {
        this.serverListener = serverListener;
    }

    public StarGateServerListener getServerListener() {
        return this.serverListener;
    }

    private static class StarGateServerInitializer extends ChannelInitializer<SocketChannel> {

        private final StarGateServer server;

        public StarGateServerInitializer(StarGateServer server){
            this.server = server;
        }

        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast(new PacketDecoder(this.server.getProtocolCodec()));
            pipeline.addLast(new PacketEncoder(this.server.getProtocolCodec()));
            pipeline.addLast(new ServerChannelHandler(this.server));
        }
    }
}
