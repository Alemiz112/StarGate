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

package alemiz.stargate.pipeline;

import alemiz.stargate.StarGateSession;
import alemiz.stargate.protocol.StarGatePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class UnhandledPacketConsumer extends SimpleChannelInboundHandler<StarGatePacket> {

    public static final String NAME = "stargate-packet-consumer";

    private final StarGateSession session;

    public UnhandledPacketConsumer(StarGateSession session) {
        this.session = session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StarGatePacket msg) throws Exception {
        this.session.getLogger().debug("Unhandled packet " + msg);
    }
}
