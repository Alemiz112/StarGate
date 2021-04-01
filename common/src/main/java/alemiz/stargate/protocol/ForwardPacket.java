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

package alemiz.stargate.protocol;

import alemiz.stargate.codec.StarGatePackets;
import alemiz.stargate.handler.StarGatePacketHandler;
import alemiz.stargate.protocol.types.PacketHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCounted;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class ForwardPacket extends StarGatePacket implements ReferenceCounted {

    public static ForwardPacket from(String clientName, StarGatePacket packet){
        ForwardPacket forwardPacket = new ForwardPacket();
        forwardPacket.setClientName(clientName);

        UnknownPacket unknownPacket = new UnknownPacket();
        unknownPacket.setPacketId(packet.getPacketId());
        ByteBuf buffer = ByteBufAllocator.DEFAULT.ioBuffer();
        packet.encodePayload(buffer);
        unknownPacket.setPayload(buffer);

        forwardPacket.setPacket(unknownPacket);
        return forwardPacket;
    }

    private String clientName;
    private UnknownPacket packet;

    @Override
    public void encodePayload(ByteBuf byteBuf) {
        PacketHelper.writeString(byteBuf, this.clientName);
        byteBuf.writeByte(this.packet.getPacketId());

        ByteBuf payload = this.packet.getPayload();
        PacketHelper.writeInt(byteBuf, payload.readableBytes());
        byteBuf.writeBytes(payload);
    }

    @Override
    public void decodePayload(ByteBuf byteBuf) {
        this.clientName = PacketHelper.readString(byteBuf);

        this.packet = new UnknownPacket();
        this.packet.setPacketId(byteBuf.readByte());

        int length = PacketHelper.readInt(byteBuf);
        this.packet.decodePayload(byteBuf.slice(byteBuf.readerIndex(), length));
    }

    @Override
    public boolean handle(StarGatePacketHandler handler) {
        return handler.handleForwardPacket(this);
    }

    @Override
    public byte getPacketId() {
        return StarGatePackets.FORWARD_PACKET;
    }

    @Override
    public int refCnt() {
        if (this.packet == null) {
            return 0;
        }
        return this.packet.refCnt();
    }

    @Override
    public ForwardPacket retain() {
        if (this.packet != null) {
            this.packet.retain();
        }
        return this;
    }

    @Override
    public ForwardPacket retain(int increment) {
        if (this.packet != null) {
            this.packet.retain(increment);
        }
        return this;
    }

    @Override
    public ForwardPacket touch() {
        if (this.packet != null) {
            this.packet.touch();
        }
        return this;
    }

    @Override
    public ForwardPacket touch(Object hint) {
        if (this.packet != null) {
            this.packet.touch(hint);
        }
        return this;
    }

    @Override
    public boolean release() {
        if (this.packet != null) {
            return this.packet.release();
        }
        return false;
    }

    @Override
    public boolean release(int decrement) {
        if (this.packet != null) {
            this.packet.release(decrement);
        }
        return false;
    }
}
