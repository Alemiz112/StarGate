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

package alemiz.stargate.events;

import alemiz.stargate.StarGate;
import alemiz.stargate.server.ServerSession;
import alemiz.stargate.server.StarGateServer;
import net.md_5.bungee.api.plugin.Event;

public class ServerEvent extends Event {

    private final StarGate plugin;
    private final ServerSession session;

    public ServerEvent(ServerSession session, StarGate plugin){
        this.session = session;
        this.plugin = plugin;
    }

    public StarGateServer getServer(){
        return this.session.getServer();
    }

    public ServerSession getSession() {
        return this.session;
    }

    public StarGate getPlugin() {
        return this.plugin;
    }
}
