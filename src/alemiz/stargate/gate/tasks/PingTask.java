package alemiz.stargate.gate.tasks;

import alemiz.stargate.gate.GateAPI;
import alemiz.stargate.gate.Server;

import java.util.TimerTask;

public class PingTask extends TimerTask {
    @Override
    public void run() {
        GateAPI.pingAll();
    }
}
