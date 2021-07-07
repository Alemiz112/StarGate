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
import alemiz.stargate.utils.BungeeLogger;
import alemiz.stargate.utils.ServerLoader;
import alemiz.stargate.utils.StarGateLogger;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.InetSocketAddress;

public class StarGate extends Plugin implements ServerLoader {

    private static StarGate instance;
    public Configuration config;

    private BungeeLogger logger;
    private StarGateServer server;

    private boolean checkClientNames;

    @Override
    public void onEnable() {
        instance = this;
        this.loadConfig();

        this.logger = new BungeeLogger(this);
        this.logger.setDebug(this.config.getBoolean("debug"));
        this.checkClientNames = this.config.getBoolean("blockSameNames");

        InetSocketAddress address = new InetSocketAddress("0.0.0.0", this.config.getInt("serverPort"));
        this.server = new StarGateServer(address, this.config.getString("password"), this);
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

    private void loadConfig(){
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }

        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(this.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static StarGate getInstance() {
        return instance;
    }

    public Configuration getConfig() {
        return this.config;
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
