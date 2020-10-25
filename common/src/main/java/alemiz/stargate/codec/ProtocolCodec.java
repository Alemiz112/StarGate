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

package alemiz.stargate.codec;

import alemiz.stargate.protocol.*;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ProtocolCodec {

    public static final short STARGATE_MAGIC = 0xa20;

    private final Object2ObjectMap<Byte, Class<? extends StarGatePacket>> packetPool = new Object2ObjectOpenHashMap<>();

    public ProtocolCodec(){
        this.registerPacket(StarGatePackets.HANDSHAKE_PACKET, HandshakePacket.class);
        this.registerPacket(StarGatePackets.SERVER_HANDSHAKE_PACKET, ServerHandshakePacket.class);
        this.registerPacket(StarGatePackets.DISCONNECT_PACKET, DisconnectPacket.class);
        this.registerPacket(StarGatePackets.PING_PACKET, PingPacket.class);
        this.registerPacket(StarGatePackets.PONG_PACKET, PongPacket.class);
        this.registerPacket(StarGatePackets.RECONNECT_PACKET, ReconnectPacket.class);
        this.registerPacket(StarGatePackets.FORWARD_PACKET, ForwardPacket.class);
    }

    public boolean registerPacket(byte packetId, Class<? extends StarGatePacket> clazz){
        return this.packetPool.putIfAbsent(packetId, clazz) == null;
    }

    public Class<? extends StarGatePacket> getPacketClass(byte packetId){
        return this.packetPool.get(packetId);
    }

    public Class<? extends StarGatePacket> unregisterPacket(byte packetId){
        return this.packetPool.remove(packetId);
    }

    public StarGatePacket constructPacket(byte packetId) {
        Class<? extends StarGatePacket> clazz = this.packetPool.get(packetId);
        if (clazz == null) return null;

        try {
            return clazz.newInstance();
        }catch (IllegalAccessException | InstantiationException e){
            return null;
        }
    }

    public void tryEncode(ByteBuf decoded, StarGatePacket packet) throws Exception {
        decoded.writeByte(packet.getPacketId());

        ByteBuf payload = decoded.alloc().buffer();
        packet.encodePayload(payload);

        int bodyLength = payload.readableBytes();
        if (packet.isResponse() || packet.sendsResponse()){
            bodyLength += 4;
        }

        decoded.writeInt(bodyLength);
        decoded.writeBytes(payload);
        payload.release();

        if (packet.isResponse() || packet.sendsResponse()){
            decoded.writeInt(packet.getResponseId());
        }
    }

    public StarGatePacket tryDecode(ByteBuf encoded) throws Exception {
        byte packetId = encoded.readByte(); //Packet od
        StarGatePacket packet = this.constructPacket(packetId);
        if (packet == null){
            return null;
        }
        int bodyLength = encoded.readInt();
        if (packet.isResponse() || packet.sendsResponse()){
            bodyLength -= 4;
        }

        ByteBuf payload = encoded.alloc().buffer(bodyLength);
        payload.writeBytes(encoded, bodyLength);
        packet.decodePayload(payload);
        payload.release();

        if (packet.isResponse() || packet.sendsResponse()){
            packet.setResponseId(encoded.readInt());
        }
        return packet;
    }
}
