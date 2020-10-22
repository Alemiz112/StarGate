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

package alemiz.stargate.codec;

public interface StarGatePackets {

    byte HANDSHAKE_PACKET = 0x01;
    byte DISCONNECT_PACKET = 0x02;
    byte PING_PACKET = 0x03;
    byte PONG_PACKET = 0x04;
    byte RECONNECT_PACKET = 0x05;
    byte FORWARD_PACKET = 0x06;

    /**
     * This packets are not registered in codec by default.
     * Register this packet manually after client connects or on server startup if you need them
     */

    byte SERVER_INFO_REQUEST_PACKET = 0x07;
    byte SERVER_INFO_RESPONSE_PACKET = 0x08;
    byte SERVER_TRANSFER_PACKET = 0x09;
}
