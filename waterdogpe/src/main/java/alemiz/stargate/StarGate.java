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

import alemiz.stargate.codec.StarGatePackets;
import alemiz.stargate.protocol.*;
import alemiz.stargate.server.ServerSession;
import alemiz.stargate.server.StarGateServer;
import alemiz.stargate.utils.WaterdogLogger;
import alemiz.stargate.utils.ServerLoader;
import alemiz.stargate.utils.StarGateLogger;
import dev.waterdog.waterdogpe.plugin.Plugin;

import java.net.InetSocketAddress;

public class StarGate extends Plugin implements ServerLoader {

    private static StarGate instance;

    private WaterdogLogger logger;
    private StarGateServer server;

    private boolean checkClientNames;

    @Override
    public void onEnable() {
        instance = this;

        this.logger = new WaterdogLogger(this);
        this.logger.setDebug(this.getConfig().getBoolean("debug", false));
        this.checkClientNames = this.getConfig().getBoolean("blockSameNames", true);

        InetSocketAddress address = new InetSocketAddress("0.0.0.0", this.getConfig().getInt("serverPort"));
        this.server = new StarGateServer(address, this.getConfig().getString("password"), this);
        this.server.setServerListener(new StarGateServerListener(this));
        this.server.getProtocolCodec().registerPacket(StarGatePackets.SERVER_INFO_REQUEST_PACKET, ServerInfoRequestPacket.class);
        this.server.getProtocolCodec().registerPacket(StarGatePackets.SERVER_INFO_RESPONSE_PACKET, ServerInfoResponsePacket.class);
        this.server.getProtocolCodec().registerPacket(StarGatePackets.SERVER_TRANSFER_PACKET, ServerTransferPacket.class);
        this.server.getProtocolCodec().registerPacket(StarGatePackets.PLAYER_PING_REQUEST_PACKET, PlayerPingRequestPacket.class);
        this.server.getProtocolCodec().registerPacket(StarGatePackets.PLAYER_PING_RESPONSE_PACKET, PlayerPingResponsePacket.class);
        this.server.getProtocolCodec().registerPacket(StarGatePackets.SERVER_MANAGE_PACKET, ServerManagePacket.class);
        this.server.start();
    }

    @Override
    public void onDisable() {
        this.server.shutdown();
    }

    public ServerSession getSession(String sessionName) {
        return this.server.getSession(sessionName);
    }

    public static StarGate getInstance() {
        return instance;
    }

    @Override
    public StarGateLogger getStarGateLogger() {
        return this.logger;
    }

    public StarGateServer getServer() {
        return this.server;
    }

    public boolean isCheckClientNames() {
        return this.checkClientNames;
    }
}
