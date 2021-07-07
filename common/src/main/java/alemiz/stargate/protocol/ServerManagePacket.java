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

package alemiz.stargate.protocol;

import alemiz.stargate.codec.StarGatePackets;
import alemiz.stargate.handler.StarGatePacketHandler;
import alemiz.stargate.protocol.types.PacketHelper;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.InetSocketAddress;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
@ToString
public class ServerManagePacket extends StarGatePacket {

    private Action action;
    private String serverName;
    private InetSocketAddress address;
    private InetSocketAddress publicAddress;
    private String serverType;

    @Override
    public void encodePayload(ByteBuf byteBuf) {
        PacketHelper.writeInt(byteBuf, this.action.ordinal());
        PacketHelper.writeString(byteBuf, this.serverName);
        if (this.action == Action.ADD) {
            PacketHelper.writeAddress(byteBuf, this.address);
            PacketHelper.writeAddress(byteBuf, this.publicAddress == null ? this.address : this.publicAddress);
            PacketHelper.writeString(byteBuf, this.serverType);
        }
    }

    @Override
    public void decodePayload(ByteBuf byteBuf) {
        this.action = Action.values()[PacketHelper.readInt(byteBuf)];
        this.serverName = PacketHelper.readString(byteBuf);
        if (this.action == Action.ADD) {
            this.address = PacketHelper.readAddress(byteBuf);
            this.publicAddress = PacketHelper.readAddress(byteBuf);
            this.serverType = PacketHelper.readString(byteBuf);
        }
    }

    @Override
    public boolean handle(StarGatePacketHandler handler) {
        return handler.handleServerManage(this);
    }

    @Override
    public byte getPacketId() {
        return StarGatePackets.SERVER_MANAGE_PACKET;
    }

    public enum Action {
        ADD,
        REMOVE
    }
}
