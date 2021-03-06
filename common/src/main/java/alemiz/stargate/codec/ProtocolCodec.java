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

import java.lang.reflect.InvocationTargetException;

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
            return clazz.getDeclaredConstructor().newInstance();
        }catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e){
            return null;
        }
    }

    public void tryEncode(ByteBuf encoded, StarGatePacket packet) throws Exception {
        ByteBuf buffer = encoded.alloc().ioBuffer();
        try {
            PacketHeader header = packet.createHeader();
            header.encode(buffer);
            packet.encodePayload(buffer);

            encoded.writeInt(buffer.readableBytes());
            encoded.writeBytes(buffer);
        } finally {
            buffer.release();
        }
    }

    public StarGatePacket tryDecode(ByteBuf encoded) throws Exception {
        if (!encoded.isReadable(4)) {
            // Tried to decode invalid buffer
            return null;
        }

        int length = encoded.readInt();
        if (!encoded.isReadable(length)) {
            // Received incomplete packet
            return null;
        }

        ByteBuf buffer = encoded.readSlice(length);
        PacketHeader header = new PacketHeader();
        header.decode(buffer);

        StarGatePacket packet = this.constructPacket(header.getPacketId());
        if (packet == null) {
            packet = new UnknownPacket();
            ((UnknownPacket) packet).setPacketId(header.getPacketId());
        }

        if (header.isSupportsResponse()) {
            packet.setResponseId(header.getResponseId());
        }

        packet.decodePayload(buffer);
        return packet;
    }
}
