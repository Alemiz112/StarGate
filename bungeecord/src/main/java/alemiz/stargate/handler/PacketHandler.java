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

package alemiz.stargate.handler;

import alemiz.stargate.StarGate;
import alemiz.stargate.protocol.*;
import alemiz.stargate.server.ServerSession;
import alemiz.stargate.server.handler.ConnectedHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.ArrayList;
import java.util.List;

public class PacketHandler extends ConnectedHandler {

    private final StarGate loader;

    public PacketHandler(ServerSession session, StarGate loader) {
        super(session);
        this.loader = loader;
    }

    @Override
    public boolean handleServerInfoRequest(ServerInfoRequestPacket packet) {
        ServerInfoResponsePacket response = new ServerInfoResponsePacket();
        response.setServerName(packet.getServerName());
        response.setSelfInfo(packet.isSelfInfo());
        response.setResponseId(packet.getResponseId());

        if (packet.isSelfInfo()){
            response.setOnlinePlayers(this.loader.getProxy().getOnlineCount());
            response.setMaxPlayers(this.loader.getProxy().getConfig().getPlayerLimit());

            for (ProxiedPlayer player : this.loader.getProxy().getPlayers()){
                response.getPlayerList().add(player.getName());
            }

            for (ServerInfo serverInfo : this.loader.getProxy().getServers().values()){
                response.getServerList().add(serverInfo.getName());
            }
            this.session.sendPacket(response);
            return true;
        }

        ServerInfo serverInfo = this.loader.getProxy().getServerInfo(packet.getServerName());
        if (serverInfo == null){
            return false;
        }
        response.setOnlinePlayers(serverInfo.getPlayers().size());
        response.setMaxPlayers(0);

        for (ProxiedPlayer player : serverInfo.getPlayers()){
            response.getPlayerList().add(player.getName());
        }
        this.session.sendPacket(response);
        return true;
    }

    @Override
    public boolean handleServerTransfer(ServerTransferPacket packet) {
        ProxiedPlayer player = this.loader.getProxy().getPlayer(packet.getPlayerName());
        ServerInfo serverInfo = this.loader.getProxy().getServerInfo(packet.getTargetServer());
        if (player == null || serverInfo == null){
            return false;
        }
        player.connect(serverInfo);
        return true;
    }

    @Override
    public boolean handlePlayerPingRequest(PlayerPingRequestPacket packet) {
        ProxiedPlayer player = this.loader.getProxy().getPlayer(packet.getPlayerName());
        if (player == null) {
            return false;
        }

        PlayerPingResponsePacket response = new PlayerPingResponsePacket();
        response.setPlayerName(player.getName());
        response.setResponseId(packet.getResponseId());
        response.setUpstreamPing(player.getPing());
        // Bungee doesnt have implemented downstream ping :/
        response.setDownstreamPing(0);
        this.session.sendPacket(response);
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean handleServerManage(ServerManagePacket packet) {
        if (packet.getAction() == ServerManagePacket.Action.REMOVE) {
            return this.loader.getProxy().getServers().remove(packet.getServerName()) != null;
        }

        if (this.loader.getProxy().getServers().containsKey(packet.getServerName())) {
            // Server with same name was already registered
            return false;
        }

        ServerInfo serverInfo = this.loader.getProxy().constructServerInfo(
                packet.getServerName(),
                packet.getAddress(),
                "",
                false
        );
        this.loader.getProxy().getServers().put(packet.getServerName(), serverInfo);
        return true;
    }
}
