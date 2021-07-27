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

package alemiz.stargate;

import alemiz.stargate.events.ClientAuthenticatedEvent;
import alemiz.stargate.events.ClientConnectedEvent;
import alemiz.stargate.events.ClientDisconnectedEvent;
import alemiz.stargate.handler.PacketHandler;
import alemiz.stargate.protocol.DisconnectPacket;
import alemiz.stargate.server.ServerSession;

import java.net.InetSocketAddress;

public class StarGateServerListener extends alemiz.stargate.server.StarGateServerListener {

    private final StarGate loader;

    public StarGateServerListener(StarGate loader){
        this.loader = loader;
    }

    @Override
    public boolean onSessionCreated(InetSocketAddress address, ServerSession session) {
        ClientConnectedEvent event = new ClientConnectedEvent(session, this.loader);
        this.loader.getProxy().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * Here we change default session handler to extended and modified for proxy.
     * @param session authenticated session instance
     */
    @Override
    public void onSessionAuthenticated(ServerSession session) {
        session.setPacketHandler(new PacketHandler(session, this.loader));

        ClientAuthenticatedEvent event = new ClientAuthenticatedEvent(session, this.loader);
        this.loader.getProxy().getPluginManager().callEvent(event);
        if (event.isCancelled()){
            session.disconnect(event.getCancelMessage());
        }

        if (this.loader.isCheckClientNames()){
            ServerSession oldSession = this.loader.getSession(session.getSessionName());
            if (oldSession != null && session != oldSession){
                oldSession.disconnect(DisconnectPacket.REASON.ANOTHER_LOCATION_LOGIN);
            }
        }
    }

    @Override
    public void onSessionDisconnected(ServerSession session) {
        ClientDisconnectedEvent event = new ClientDisconnectedEvent(session, this.loader);
        this.loader.getProxy().getPluginManager().callEvent(event);
    }
}
