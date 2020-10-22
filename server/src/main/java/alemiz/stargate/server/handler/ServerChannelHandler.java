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

package alemiz.stargate.server.handler;

import alemiz.stargate.protocol.StarGatePacket;
import alemiz.stargate.server.ServerSession;
import alemiz.stargate.server.StarGateServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;

public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private final StarGateServer server;

    public ServerChannelHandler(StarGateServer server){
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        this.server.onSessionCreation(address, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        ServerSession session = this.server.getSessions().get(address);
        if (session != null){
            this.server.onSessionDisconnect(address, session);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof StarGatePacket){
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            ServerSession session = this.server.getSession(address);
            session.onPacket((StarGatePacket) msg);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        this.server.getLogger().logException(e);
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        ServerSession session = this.server.getSessions().get(address);
        if (session != null){
            session.close();
        }
    }
}
