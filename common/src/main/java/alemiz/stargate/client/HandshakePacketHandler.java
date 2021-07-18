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

import alemiz.stargate.protocol.ServerHandshakePacket;
import alemiz.stargate.handler.CommonSessionHandler;

public class HandshakePacketHandler extends CommonSessionHandler<ClientSession> {

    public HandshakePacketHandler(ClientSession session) {
        super(session);
    }

    @Override
    public boolean handleServerHandshake(ServerHandshakePacket packet) {
        if (this.session.getClient().getClientListener() != null){
            this.session.getClient().getClientListener().onSessionAuthenticated(this.session);
        }
        this.session.setPacketHandler(new ClientPacketHandler(this.session));
        return true;
    }
}
