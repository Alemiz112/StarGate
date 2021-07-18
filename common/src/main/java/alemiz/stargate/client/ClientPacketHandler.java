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

package alemiz.stargate.client;

import alemiz.stargate.protocol.*;
import alemiz.stargate.handler.CommonSessionHandler;

public class ClientPacketHandler extends CommonSessionHandler<ClientSession> {

    public ClientPacketHandler(ClientSession session){
        super(session);
    }

    @Override
    public boolean handlePing(PingPacket packet) {
        PongPacket pongPacket = new PongPacket();
        pongPacket.setPingTime(packet.getPingTime());
        this.session.forcePacket(pongPacket);
        return true;
    }

    @Override
    public boolean handlePong(PongPacket packet) {
        this.session.onPongReceive(packet);
        return true;
    }

    @Override
    public boolean handleReconnect(ReconnectPacket packet) {
        this.session.reconnect(packet.getReason(), false);
        return true;
    }
}
