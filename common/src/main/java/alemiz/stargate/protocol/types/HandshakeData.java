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

package alemiz.stargate.protocol.types;

import io.netty.buffer.ByteBuf;
import lombok.ToString;

@ToString
public class HandshakeData {

    public static void encodeData(ByteBuf byteBuf, HandshakeData handshakeData){
        PacketHelper.writeInt(byteBuf, handshakeData.software.ordinal());
        PacketHelper.writeString(byteBuf, handshakeData.clientName);
        PacketHelper.writeString(byteBuf, handshakeData.password);
        PacketHelper.writeInt(byteBuf, handshakeData.protocolVersion);
    }

    public static HandshakeData decodeData(ByteBuf byteBuf){
        SOFTWARE software = SOFTWARE.values()[PacketHelper.readInt(byteBuf)];
        String clientName = PacketHelper.readString(byteBuf);
        String password = PacketHelper.readString(byteBuf);
        int protocolVersion = PacketHelper.readInt(byteBuf);
        return new HandshakeData(clientName, password, software, protocolVersion);
    }

    private final String clientName;
    private final String password;
    private final SOFTWARE software;
    private final int protocolVersion;

    public HandshakeData(String clientName, String password, SOFTWARE software, int protocolVersion){
        this.clientName = clientName;
        this.password = password;
        this.software = software;
        this.protocolVersion = protocolVersion;
    }

    public String getClientName() {
        return this.clientName;
    }

    public String getPassword() {
        return this.password;
    }

    public SOFTWARE getSoftware() {
        return this.software;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    public enum SOFTWARE {
        POCKETMINE_LEGACY,
        PMMP,
        NUKKIT,
        CLOUDBURST,
        WATERDOG,
        CUSTOM
    }
}
