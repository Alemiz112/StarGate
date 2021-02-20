/*
 * Copyright 2021 Alemiz
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

import alemiz.stargate.handler.StarGatePacketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
@ToString
public class UnknownPacket extends StarGatePacket implements ReferenceCounted {

    private byte packetId;
    private ByteBuf payload;

    @Override
    public void encodePayload(ByteBuf byteBuf) {
        byteBuf.writeBytes(this.payload);
    }

    @Override
    public void decodePayload(ByteBuf byteBuf) {
        this.payload = byteBuf.readRetainedSlice(byteBuf.readableBytes());
    }

    @Override
    public boolean handle(StarGatePacketHandler handler) {
        return handler.handleUnknown(this);
    }

    @Override
    public byte getPacketId() {
        return this.packetId;
    }

    @Override
    public int refCnt() {
        if (this.payload == null) {
            return 0;
        }
        return this.payload.refCnt();
    }

    @Override
    public UnknownPacket retain() {
        if (this.payload != null) {
            this.payload.retain();
        }
        return this;
    }

    @Override
    public UnknownPacket retain(int increment) {
        if (this.payload != null) {
            this.payload.retain(increment);
        }
        return this;
    }

    @Override
    public UnknownPacket touch() {
        if (this.payload != null) {
            this.payload.touch();
        }
        return this;
    }

    @Override
    public UnknownPacket touch(Object hint) {
        if (this.payload != null) {
            this.payload.touch(hint);
        }
        return this;
    }

    @Override
    public boolean release() {
        if (this.payload != null) {
            return this.payload.release();
        }
        return false;
    }

    @Override
    public boolean release(int decrement) {
        if (this.payload != null) {
            this.payload.release(decrement);
        }
        return false;
    }
}
