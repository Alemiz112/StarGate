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

package alemiz.stargate.utils;

import alemiz.stargate.StarGate;
import alemiz.stargate.utils.StarGateLogger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class BungeeLogger implements StarGateLogger {

    private final StarGate loader;
    private boolean debug = false;

    public BungeeLogger(StarGate loader){
        this.loader = loader;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return this.debug;
    }

    @Override
    public void debug(String message) {
        if (this.debug){
            this.loader.getLogger().info("[DEBUG] "+message);
        }
    }

    @Override
    public void info(String message) {
        this.loader.getLogger().info("§b"+message);
    }

    @Override
    public void warn(String message) {
        this.loader.getLogger().warning("§e"+message);
    }

    @Override
    public void error(String message) {
        this.loader.getLogger().warning("§c"+message);
    }

    @Override
    public void error(String message, Throwable e) {
        this.error(message);
        this.logException(e);
    }

    @Override
    public void logException(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        this.loader.getLogger().warning(stringWriter.toString());
    }
}
