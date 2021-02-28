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

package alemiz.stargate.codec;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PacketHeader {

    private byte packetId;
    private boolean supportsResponse;
    private int responseId;

    public PacketHeader() {
    }

    public static boolean isReadable(ByteBuf buffer) {
        if (!buffer.isReadable(2)) { // Has PacketID, SupportsResponse
            return false;
        }

        int index = buffer.readerIndex();
        try {
            buffer.readByte(); // PacketID
            if (buffer.readBoolean()) {
                return buffer.isReadable(4); // ResponseID
            }
            return true;
        } finally {
            buffer.readerIndex(index);
        }
    }

    public void encode(ByteBuf encoded) {
        encoded.writeByte(this.packetId);
        encoded.writeBoolean(this.supportsResponse);
        if (this.supportsResponse) {
            encoded.writeInt(this.responseId);
        }
    }

    public void decode(ByteBuf encoded) {
        this.packetId = encoded.readByte();
        this.supportsResponse = encoded.readBoolean();
        if (this.supportsResponse) {
            this.responseId = encoded.readInt();
        }
    }
}
