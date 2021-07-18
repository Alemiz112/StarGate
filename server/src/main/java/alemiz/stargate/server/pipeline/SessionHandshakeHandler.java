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
import alemiz.stargate.protocol.HandshakePacket;
import alemiz.stargate.protocol.ServerHandshakePacket;
import alemiz.stargate.protocol.StarGatePacket;
import alemiz.stargate.protocol.types.HandshakeData;
import alemiz.stargate.server.ServerSession;
import alemiz.stargate.server.handler.ConnectedHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SessionHandshakeHandler extends SimpleChannelInboundHandler<StarGatePacket> {

    public static final String NAME = "server-handshake-handler";
    private final ServerSession session;

    public SessionHandshakeHandler(ServerSession session) {
        this.session = session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StarGatePacket msg) throws Exception {
        if (!(msg instanceof HandshakePacket)) {
            // Drop all except HandshakePacket to make sure client is authenticated
            return;
        }

        HandshakePacket packet = (HandshakePacket) msg;
        HandshakeData handshakeData = packet.getHandshakeData();
        this.session.setHandshakeData(handshakeData);
        if (!this.session.getServer().getPassword().equals(handshakeData.getPassword())){
            this.session.getLogger().warn("Client "+handshakeData.getClientName()+" connected with wrong password!");
            this.session.disconnect(DisconnectPacket.REASON.WRONG_PASSWORD);
            return;
        }

        if (this.session.getServer().getProtocolVersion() != handshakeData.getProtocolVersion()) {
            this.session.getLogger().warn("Client "+handshakeData.getClientName()+" connected with incompatible protocol version ("+handshakeData.getProtocolVersion()+")!");
            this.session.disconnect(DisconnectPacket.REASON.INCORRECT_VERSION);
            return;
        }

        // Remove handshake handler
        ctx.pipeline().remove(NAME);

        this.session.setAuthenticated(true);
        this.session.setPacketHandler(new ConnectedHandler(this.session));
        this.session.getLogger().info("New client connected! Name: "+handshakeData.getClientName()+" Software: "+handshakeData.getSoftware().name());
        this.session.sendPacket(new ServerHandshakePacket());
        this.session.getServer().onSessionAuthenticated(this.session);
    }
}
