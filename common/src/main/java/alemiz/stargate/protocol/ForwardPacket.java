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

import alemiz.stargate.codec.ProtocolCodec;
import alemiz.stargate.codec.StarGatePackets;
import alemiz.stargate.handler.StarGatePacketHandler;
import alemiz.stargate.protocol.types.PacketHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class ForwardPacket extends StarGatePacket {

    public static ForwardPacket from(String clientName, StarGatePacket packet){
        ForwardPacket forwardPacket = new ForwardPacket();
        forwardPacket.setClientName(clientName);
        forwardPacket.setForwardPacketId(packet.getPacketId());

        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer();
        packet.encodePayload(buf);

        byte[] payload = new byte[buf.readableBytes()];
        buf.readBytes(payload);
        buf.release();

        forwardPacket.setPayload(payload);
        return forwardPacket;
    }

    private String clientName;
    private byte forwardPacketId;
    private byte[] payload;

    @Override
    public void encodePayload(ByteBuf byteBuf) {
        PacketHelper.writeString(byteBuf, this.clientName);
        byteBuf.writeByte(this.forwardPacketId);
        PacketHelper.writeByteArray(byteBuf, this.payload);
    }

    @Override
    public void decodePayload(ByteBuf byteBuf) {
        this.clientName = PacketHelper.readString(byteBuf);
        this.forwardPacketId = byteBuf.readByte();
        this.payload = PacketHelper.readByteArray(byteBuf);
    }

    public ByteBuf createPacket(){
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer();
        buf.writeShort(ProtocolCodec.STARGATE_MAGIC);
        buf.writeByte(this.forwardPacketId);
        PacketHelper.writeByteArray(buf, this.payload);
        return buf;
    }

    @Override
    public boolean handle(StarGatePacketHandler handler) {
        return handler.handleForwardPacket(this);
    }

    @Override
    public byte getPacketId() {
        return StarGatePackets.FORWARD_PACKET;
    }
}
