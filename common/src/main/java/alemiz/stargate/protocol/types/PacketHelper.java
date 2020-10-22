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

import java.nio.charset.StandardCharsets;

public class PacketHelper {

    public static byte[] read(ByteBuf buf, int len) {
        if (len < 0) {
            return new byte[0];
        }

        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return bytes;
    }

    public static void write(ByteBuf buf, byte[] bytes) {
        if (bytes == null) {
            return;
        }
        buf.writeBytes(bytes);
    }

    public static byte[] readByteArray(ByteBuf buf) {
        return read(buf, buf.readInt());
    }

    public static void writeByteArray(ByteBuf buf, byte[] bytes) {
        buf.writeInt(bytes.length);
        write(buf, bytes);
    }

    public static boolean readBoolean(ByteBuf buf) {
        return buf.readByte() == 1;
    }

    public static void writeBoolean(ByteBuf buf, boolean bool) {
        buf.writeByte(bool? 1 : 0);
    }

    public static void writeInt(ByteBuf buf, int i) {
        buf.writeInt(i);
    }

    public static int readInt(ByteBuf byteBuf) {
        return byteBuf.readInt();
    }

    public static void writeLong(ByteBuf buf, long l) {
        buf.writeLong(l);
    }

    public static long readLong(ByteBuf byteBuf) {
        return byteBuf.readLong();
    }

    public static void writeString(ByteBuf buf, String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeByteArray(buf, bytes);
    }

    public static String readString(ByteBuf buf) {
        return new String(readByteArray(buf), StandardCharsets.UTF_8);
    }
}
