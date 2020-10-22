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

package alemiz.stargate.protocol.types;

import io.netty.buffer.ByteBuf;
import lombok.ToString;

@ToString
public class HandshakeData {

    public static void encodeData(ByteBuf byteBuf, HandshakeData handshakeData){
        PacketHelper.writeInt(byteBuf, handshakeData.software.ordinal());
        PacketHelper.writeString(byteBuf, handshakeData.clientName);
        PacketHelper.writeString(byteBuf, handshakeData.password);
    }

    public static HandshakeData decodeData(ByteBuf byteBuf){
        SOFTWARE software = SOFTWARE.values()[PacketHelper.readInt(byteBuf)];
        String clientName = PacketHelper.readString(byteBuf);
        String password = PacketHelper.readString(byteBuf);
        return new HandshakeData(clientName, password, software);
    }

    private final String clientName;
    private final String password;
    private final SOFTWARE software;

    public HandshakeData(String clientName, String password, SOFTWARE software){
        this.clientName = clientName;
        this.password = password;
        this.software = software;
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

    public enum SOFTWARE {
        POCKETMINE,
        PMMP4,
        NUKKIT,
        CLOUDBURST,
        WATERDOG,
        CUSTOM
    }
}
