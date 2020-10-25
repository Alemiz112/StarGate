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

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
@ToString
public class ServerInfoResponsePacket extends StarGatePacket {

    private String serverName;
    private boolean selfInfo;
    private int onlinePlayers;
    private int maxPlayers;
    private String[] playerList;
    private String[] serverList;

    @Override
    public void encodePayload(ByteBuf byteBuf) {
        PacketHelper.writeString(byteBuf, this.serverName);
        PacketHelper.writeBoolean(byteBuf, this.selfInfo);

        PacketHelper.writeInt(byteBuf, this.onlinePlayers);
        PacketHelper.writeInt(byteBuf, this.maxPlayers);

        String players = String.join(",", this.playerList);
        String servers = String.join(",", this.serverList);
        PacketHelper.writeString(byteBuf, players);
        PacketHelper.writeString(byteBuf, servers);
    }

    @Override
    public void decodePayload(ByteBuf byteBuf) {
        this.serverName = PacketHelper.readString(byteBuf);
        this.selfInfo = PacketHelper.readBoolean(byteBuf);

        this.onlinePlayers = PacketHelper.readInt(byteBuf);
        this.maxPlayers = PacketHelper.readInt(byteBuf);

        String players = PacketHelper.readString(byteBuf);
        String servers = PacketHelper.readString(byteBuf);
        this.playerList = players.split(",");
        this.serverList = servers.split(",");
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
