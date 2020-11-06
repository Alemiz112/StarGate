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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
@ToString
public class ServerInfoResponsePacket extends StarGatePacket {

    private String serverName;
    private boolean selfInfo;
    private int onlinePlayers;
    private int maxPlayers;
    private List<String> playerList = new ArrayList<>();
    private List<String> serverList = new ArrayList<>();

    @Override
    public void encodePayload(ByteBuf byteBuf) {
        PacketHelper.writeString(byteBuf, this.serverName);
        PacketHelper.writeBoolean(byteBuf, this.selfInfo);

        PacketHelper.writeInt(byteBuf, this.onlinePlayers);
        PacketHelper.writeInt(byteBuf, this.maxPlayers);

        PacketHelper.writeArray(byteBuf, this.playerList, PacketHelper::writeString);
        PacketHelper.writeArray(byteBuf, this.serverList, PacketHelper::writeString);
    }

    @Override
    public void decodePayload(ByteBuf byteBuf) {
        this.serverName = PacketHelper.readString(byteBuf);
        this.selfInfo = PacketHelper.readBoolean(byteBuf);

        this.onlinePlayers = PacketHelper.readInt(byteBuf);
        this.maxPlayers = PacketHelper.readInt(byteBuf);

        PacketHelper.readArray(byteBuf, this.playerList, PacketHelper::readString);
        PacketHelper.readArray(byteBuf, this.serverList, PacketHelper::readString);
    }

    @Override
    public boolean handle(StarGatePacketHandler handler) {
        return handler.handleServerInfoResponse(this);
    }

    @Override
    public byte getPacketId() {
        return StarGatePackets.SERVER_INFO_RESPONSE_PACKET;
    }

    @Override
    public boolean isResponse() {
        return true;
    }
}
