package alemiz.stargate.staff;

import alemiz.stargate.StarGate;
import alemiz.stargate.commands.PlayerFindCommand;
import alemiz.stargate.commands.StaffFindCommand;

public class Staff {

    protected StarGate plugin;

    public Staff(StarGate plugin){
        this.plugin = plugin;

        this.registerCommands();
    }

    private void registerCommands(){
        plugin.getProxy().getPluginManager().registerCommand(plugin, new PlayerFindCommand(this));

        plugin.getProxy().getPluginManager().registerCommand(plugin, new StaffFindCommand(this));
    }

    
}
