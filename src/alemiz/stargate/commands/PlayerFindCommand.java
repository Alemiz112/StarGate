package alemiz.stargate.commands;

import alemiz.stargate.StarGate;
import alemiz.stargate.staff.Staff;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PlayerFindCommand extends Command{

    public Staff staff;

    public PlayerFindCommand(Staff staff){
        super("look", "stargate.staff");
        this.staff = staff;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1){
            sender.sendMessage(new TextComponent("§6Find Player:\n"+
                    "§7/look <player>"));
            return;
        }

        String message = "§cFinding Players with name §6'" + args[0] +"'§c ...\n";
        for (ProxiedPlayer player : StarGate.getInstance().getProxy().getPlayers()){
            if (player.getName().toLowerCase().contains(args[0].toLowerCase())){
                String server = player.getServer().getInfo().getName();

                message += "§c" +player.getName() + "§e > §6" +server + "\n";
            }
        }

        sender.sendMessage(new TextComponent(message));
    }
}
