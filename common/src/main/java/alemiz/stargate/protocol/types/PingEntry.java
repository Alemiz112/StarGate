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

package alemiz.stargate.protocol.types;

import alemiz.stargate.protocol.PongPacket;

import java.util.concurrent.CompletableFuture;

public class PingEntry {

    private final CompletableFuture<PongPacket> future;
    private final long timeout;

    public PingEntry(CompletableFuture<PongPacket> future, long timeout){
        this.future = future;
        this.timeout = timeout;
    }

    public CompletableFuture<PongPacket> getFuture() {
        return this.future;
    }

    public long getTimeout() {
        return this.timeout;
    }
}
