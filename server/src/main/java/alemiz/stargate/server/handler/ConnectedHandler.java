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

package alemiz.stargate.server.handler;

import alemiz.stargate.protocol.ForwardPacket;
import alemiz.stargate.protocol.PingPacket;
import alemiz.stargate.protocol.PongPacket;
import alemiz.stargate.server.ServerSession;
import alemiz.stargate.server.StarGateServer;
import alemiz.stargate.handler.CommonSessionHandler;

public class ConnectedHandler extends CommonSessionHandler<ServerSession> {

    public ConnectedHandler(ServerSession session) {
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
    public boolean handleForwardPacket(ForwardPacket packet) {
        StarGateServer server = this.session.getServer();
        ServerSession clientSession = server.getSession(packet.getClientName());
        if (clientSession == null){
            this.session.getLogger().debug("Tried to forward packet to unknown client "+packet.getClientName()+"!");
            return true;
        }
        clientSession.sendPacket(packet.getPacket().retain());
        return true;
    }
}
