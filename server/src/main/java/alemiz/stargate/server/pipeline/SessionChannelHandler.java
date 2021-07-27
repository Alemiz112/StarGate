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

import alemiz.stargate.protocol.DisconnectPacket;
import alemiz.stargate.protocol.StarGatePacket;
import alemiz.stargate.server.ServerSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

public class SessionChannelHandler extends SimpleChannelInboundHandler<StarGatePacket> {

    public static final String NAME = "stargate-server-session-handler";
    private final ServerSession session;

    public SessionChannelHandler(ServerSession session) {
        this.session = session;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.session.getServer().onSessionDisconnect(this.session);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StarGatePacket packet) throws Exception {
        if (this.session == null) {
            return;
        }

        if (!this.session.onPacket(packet)) {
            ctx.fireChannelRead(ReferenceCountUtil.retain(packet));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        this.session.getLogger().error("[" + this.session.getSessionName() + "] An exception was caught!", e);
        this.session.disconnect(DisconnectPacket.REASON.INTERNAL_ERROR);
    }
}
