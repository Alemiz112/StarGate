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

package alemiz.stargate.handler;

import alemiz.stargate.codec.ProtocolCodec;
import alemiz.stargate.protocol.StarGatePacket;
import alemiz.stargate.utils.StarGateLogger;
import alemiz.stargate.utils.exception.StarGateException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class PacketDeEncoder extends ByteToMessageCodec<StarGatePacket> {

    private final StarGateLogger logger;
    private final ProtocolCodec protocolCodec;

    public PacketDeEncoder(ProtocolCodec protocolCodec, StarGateLogger logger){
        this.protocolCodec = protocolCodec;
        this.logger = logger;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (!buffer.isReadable(2)) {
            return;
        }

        int index = buffer.readerIndex();
        if (buffer.readShort() != ProtocolCodec.STARGATE_MAGIC) {
            throw new StarGateException("Received wrong magic");
        }

        try {
            StarGatePacket packet = this.protocolCodec.tryDecode(buffer);
            if (packet == null) {
                buffer.readerIndex(index);
            } else {
                out.add(packet);
            }
        }catch (Exception e){
            byte packetId = buffer.getByte(index + 2);
            buffer.skipBytes(buffer.readableBytes());
            this.logger.error("Can not decode StarGatePacket packetId="+packetId, e);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, StarGatePacket packet, ByteBuf buffer) throws Exception {
        buffer.writeShort(ProtocolCodec.STARGATE_MAGIC);
        this.protocolCodec.tryEncode(buffer, packet);
    }
}
