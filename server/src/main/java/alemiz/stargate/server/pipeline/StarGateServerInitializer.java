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

package alemiz.stargate.server.pipeline;

import alemiz.stargate.pipeline.PacketDeEncoder;
import alemiz.stargate.pipeline.UnhandledPacketConsumer;
import alemiz.stargate.server.ServerSession;
import alemiz.stargate.server.StarGateServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;

public class StarGateServerInitializer extends ChannelInitializer<SocketChannel> {

    private final StarGateServer server;

    public StarGateServerInitializer(StarGateServer server) {
        this.server = server;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        InetSocketAddress address = channel.remoteAddress();

        ServerSession session = this.server.onSessionCreation(address, channel);
        if (session == null) {
            channel.close();
            return;
        }

        pipeline.addLast(PacketDeEncoder.NAME, new PacketDeEncoder(this.server.getProtocolCodec(), this.server.getLogger()));
        pipeline.addLast(SessionHandshakeHandler.NAME, new SessionHandshakeHandler(session));
        pipeline.addLast(SessionChannelHandler.NAME, new SessionChannelHandler(session));
        pipeline.addLast(UnhandledPacketConsumer.NAME, new UnhandledPacketConsumer(session));
    }
}
