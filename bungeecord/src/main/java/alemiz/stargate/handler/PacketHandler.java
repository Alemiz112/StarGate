/**
 * Copyright 2020 WaterdogTEAM
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alemiz.stargate.handler;

import alemiz.stargate.StarGate;
import alemiz.stargate.protocol.ServerInfoRequestPacket;
import alemiz.stargate.protocol.ServerInfoResponsePacket;
import alemiz.stargate.protocol.ServerTransferPacket;
import alemiz.stargate.server.ServerSession;
import alemiz.stargate.server.handler.ConnectedHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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

        if (packet.isSelfInfo()){
            response.setOnlinePlayers(this.loader.getProxy().getOnlineCount());
            response.setMaxPlayers(this.loader.getProxy().getConfig().getPlayerLimit());

            List<String> playerList = new ArrayList<>();
            for (ProxiedPlayer player : this.loader.getProxy().getPlayers()){
                playerList.add(player.getName());
            }
            response.setPlayerList(playerList.toArray(new String[0]));

            List<String> serverList = new ArrayList<>();
            for (ServerInfo serverInfo : this.loader.getProxy().getServersCopy().values()){
                serverList.add(serverInfo.getName());
            }
            response.setServerList(serverList.toArray(new String[0]));
            this.session.sendPacket(response);
            return true;
        }

        ServerInfo serverInfo = this.loader.getProxy().getServerInfo(packet.getServerName());
        if (serverInfo == null){
            return false;
        }
        response.setOnlinePlayers(serverInfo.getPlayers().size());
        response.setMaxPlayers(0);

        List<String> playerList = new ArrayList<>();
        for (ProxiedPlayer player : serverInfo.getPlayers()){
            playerList.add(player.getName());
        }
        response.setPlayerList(playerList.toArray(new String[0]));
        response.setServerList(new String[0]);
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
        return false;
    }
}
