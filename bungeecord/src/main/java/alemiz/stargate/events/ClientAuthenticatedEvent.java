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
import net.md_5.bungee.api.plugin.Cancellable;

public class ClientAuthenticatedEvent extends ServerEvent implements Cancellable {

    private boolean canceled;
    private String cancelMessage = "Authentication was canceled!";

    public ClientAuthenticatedEvent(ServerSession session, StarGate plugin) {
        super(session, plugin);
    }

    public void setCancelMessage(String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }

    public String getCancelMessage() {
        return this.cancelMessage;
    }

    @Override
    public boolean isCancelled() {
        return this.canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }
}
