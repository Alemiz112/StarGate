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
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfoType;
import dev.waterdog.waterdogpe.network.session.DownstreamClient;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;

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
            response.setOnlinePlayers(this.loader.getProxy().getPlayers().size());
            response.setMaxPlayers(this.loader.getProxy().getConfiguration().getMaxPlayerCount());

            for (ProxiedPlayer player : this.loader.getProxy().getPlayers().values()){
                response.getPlayerList().add(player.getName());
            }

            for (ServerInfo serverInfo : this.loader.getProxy().getServers()){
                response.getServerList().add(serverInfo.getServerName());
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
        response.setResponseId(packet.getResponseId());
        response.setUpstreamPing(player.getPing());

        DownstreamClient downstream = player.getDownstream();
        if (downstream != null && downstream.isConnected()) {
            response.setDownstreamPing(downstream.getSession().getLatency());
        }
        this.session.sendPacket(response);
        return true;
    }

    @Override
    public boolean handleServerManage(ServerManagePacket packet) {
        if (packet.getAction() == ServerManagePacket.Action.REMOVE) {
            return this.loader.getProxy().removeServerInfo(packet.getServerName()) != null;
        }

        ServerInfoType serverType = ServerInfoType.getOrBedrock(packet.getServerType());
        ServerInfo serverInfo = this.loader.getProxy().getServerInfoMap().createServerInfo(
                packet.getServerName(), packet.getAddress(), packet.getPublicAddress(), serverType);
        return this.loader.getProxy().registerServerInfo(serverInfo);
    }
}
