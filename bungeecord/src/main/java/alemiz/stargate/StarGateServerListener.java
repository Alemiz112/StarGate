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

import alemiz.stargate.handler.PacketHandler;
import alemiz.stargate.server.ServerSession;

import java.net.InetSocketAddress;

public class StarGateServerListener extends alemiz.stargate.server.StarGateServerListener {

    private final StarGate loader;

    public StarGateServerListener(StarGate loader){
        this.loader = loader;
    }

    @Override
    public boolean onSessionCreated(InetSocketAddress address, ServerSession session) {
        //TODO: event
        return true;
    }

    /**
     * Here we change default session handler to extended and modified for proxy.
     * @param session authenticated session instance
     */
    @Override
    public void onSessionAuthenticated(ServerSession session) {
        session.setPacketHandler(new PacketHandler(session, this.loader));
        //TODO: event
    }

    @Override
    public void onSessionDisconnected(ServerSession session) {
        //TODO: event
    }
}
