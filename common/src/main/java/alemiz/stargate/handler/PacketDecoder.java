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

package alemiz.stargate.handler;

import alemiz.stargate.codec.ProtocolCodec;
import alemiz.stargate.utils.exception.StarGateException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

    private final ProtocolCodec protocolCodec;

    public PacketDecoder(ProtocolCodec protocolCodec){
        this.protocolCodec = protocolCodec;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (buffer.readShort() != ProtocolCodec.STARGATE_MAGIC){
            throw new StarGateException("Received wrong magic");
        }

        out.add(this.protocolCodec.tryDecode(buffer));
    }
}
