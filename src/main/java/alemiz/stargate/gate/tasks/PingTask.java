package alemiz.stargate.gate.tasks;

import alemiz.stargate.gate.GateAPI;

public class PingTask implements Runnable{

    @Override
    public void run() {
        GateAPI.pingAll();
    }
}
