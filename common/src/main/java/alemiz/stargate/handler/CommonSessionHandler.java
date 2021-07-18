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

package alemiz.stargate.handler;

import alemiz.stargate.protocol.DisconnectPacket;
import alemiz.stargate.protocol.UnknownPacket;
import alemiz.stargate.StarGateSession;
import io.netty.buffer.ByteBufUtil;

public abstract class CommonSessionHandler<T extends StarGateSession> extends SessionHandler<T> {

    public CommonSessionHandler(T session) {
        super(session);
    }

    @Override
    public boolean handleDisconnect(DisconnectPacket packet) {
        this.session.onDisconnect(packet.getReason());
        return true;
    }

    @Override
    public boolean handleUnknown(UnknownPacket packet) {
        this.session.getLogger().info("Received UnknownPacket packetId="+packet.getPacketId()+" payload="+(packet.getPayload() == null ?
                "null" :
                ByteBufUtil.prettyHexDump(packet.getPayload())));
        return true;
    }
}
