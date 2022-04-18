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

import alemiz.stargate.codec.ProtocolCodec;
import alemiz.stargate.protocol.StarGatePacket;
import alemiz.stargate.utils.StarGateLogger;
import alemiz.stargate.utils.exception.StarGateException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class PacketDeEncoder extends ByteToMessageCodec<StarGatePacket> {

    public static final String NAME = "stargate-packet-deencoder";

    private final StarGateLogger logger;
    private final ProtocolCodec protocolCodec;

    public PacketDeEncoder(ProtocolCodec protocolCodec, StarGateLogger logger){
        this.protocolCodec = protocolCodec;
        this.logger = logger;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {
        if (!buffer.isReadable(6)) { // MAGIC + LENGTH
            return;
        }

        int index = buffer.readerIndex();
        if (buffer.readShort() != ProtocolCodec.STARGATE_MAGIC) {
            throw new StarGateException("Received wrong magic");
        }

        int length = buffer.readInt();
        if (!buffer.isReadable(length)) {
            buffer.readerIndex(index);
            return;
        }

        ByteBuf encoded = buffer.readSlice(length);
        encoded.markReaderIndex();

        try {
            StarGatePacket packet = this.protocolCodec.tryDecode(encoded);
            if (packet == null) {
                encoded.resetReaderIndex();
                this.logger.warn("Unknown packet payload received! " + ByteBufUtil.hexDump(encoded));
            } else {
                out.add(packet);
            }
        } catch (Exception e) {
            this.logger.error("Can not decode StarGatePacket!", e);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, StarGatePacket packet, ByteBuf buffer) {
        try {
            buffer.writeShort(ProtocolCodec.STARGATE_MAGIC);

            // Reserve 4 bytes for encoded length
            int index = buffer.writerIndex();
            buffer.writeZero(4);

            this.protocolCodec.tryEncode(buffer, packet);

            // Revert back to the beginning
            int finalIndex = buffer.writerIndex();
            buffer.writerIndex(index);

            // Write encoded length and restore index
            buffer.writeInt(finalIndex - index - 4);
            buffer.writerIndex(finalIndex);
        } catch (Exception e) {
            this.logger.error("Can not encode " + packet.getClass().getSimpleName(), e);
            buffer.clear(); // Do not send anything
            throw e;
        }
    }
}
