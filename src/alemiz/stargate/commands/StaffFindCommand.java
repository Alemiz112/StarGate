package alemiz.stargate.commands;

import alemiz.stargate.StarGate;
import alemiz.stargate.staff.Staff;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class StaffFindCommand extends Command{

    public Staff staff;

    public StaffFindCommand(Staff staff){
        super("staff", "stargate.staff");
        this.staff = staff;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String message = "§cShowing list of Online Staff§c ...\n";
        for (ProxiedPlayer player : StarGate.getInstance().getProxy().getPlayers()){
            if (player.hasPermission("stargate.staff")){
                String server = player.getServer().getInfo().getName();

                if ((sender instanceof ProxiedPlayer) && sender.getName() == player.getName()){
                    message += "§6" +player.getName() + "§e > " +server + "\n";
                }else {
                    message += "§4" +player.getName() + "§e > " +server+ "\n";
                }
            }
        }

        sender.sendMessage(new TextComponent(message));
    }
}
