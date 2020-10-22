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

package alemiz.stargate.protocol;

import alemiz.stargate.codec.StarGatePackets;
import alemiz.stargate.handler.StarGatePacketHandler;
import alemiz.stargate.protocol.types.HandshakeData;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
@ToString
public class HandshakePacket extends StarGatePacket {

    private HandshakeData handshakeData;

    @Override
    public void encodePayload(ByteBuf byteBuf) {
        HandshakeData.encodeData(byteBuf, this.handshakeData);
    }

    @Override
    public void decodePayload(ByteBuf byteBuf) {
        this.handshakeData = HandshakeData.decodeData(byteBuf);
    }

    @Override
    public boolean handle(StarGatePacketHandler handler) {
        return handler.handleHandshake(this);
    }

    @Override
    public byte getPacketId() {
        return StarGatePackets.HANDSHAKE_PACKET;
    }
}
