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

import alemiz.stargate.protocol.DisconnectPacket;
import alemiz.stargate.protocol.StarGatePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

public class ClientChannelHandler extends SimpleChannelInboundHandler<StarGatePacket> {

    private final StarGateClient client;

    public ClientChannelHandler(StarGateClient client){
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        this.client.onConnect(address, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.client.onDisconnect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StarGatePacket packet) throws Exception {
        ClientSession session = this.client.getSession();
        if (session == null) {
            return;
        }

        if (!session.onPacket(packet)) {
            ctx.fireChannelRead(ReferenceCountUtil.retain(packet));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        this.client.getLogger().error("[StarGateClient] An exception was caught!", e);
        if (this.client.isConnected()) {
            this.client.getSession().disconnect(DisconnectPacket.REASON.INTERNAL_ERROR);
        } else {
            this.client.onDisconnect();
        }
    }
}
