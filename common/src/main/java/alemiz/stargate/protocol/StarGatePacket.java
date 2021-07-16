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

import alemiz.stargate.handler.StarGatePacketHandler;
import alemiz.stargate.utils.StarGateLogger;
import io.netty.buffer.ByteBuf;
import lombok.ToString;

import java.util.concurrent.ThreadLocalRandom;

@ToString
public abstract class StarGatePacket {

    private int responseId;

    public abstract void encodePayload(ByteBuf byteBuf);
    public abstract void decodePayload(ByteBuf byteBuf);

    public boolean handle(StarGatePacketHandler handler){
        return false;
    }

    public abstract byte getPacketId();

    public void generateResponseId(){
        this.responseId = ThreadLocalRandom.current().nextInt();
    }

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public int getResponseId() {
        return this.responseId;
    }

    public boolean sendsResponse(){
        return false;
    }

    public boolean isResponse(){
        return false;
    }

    public int getLogLevel() {
        return StarGateLogger.LEVEL_FILTERED;
    }
}
