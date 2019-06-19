package alemiz.stargate.staffchat;

import alemiz.stargate.StarGate;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class StaffChatCommand extends Command{

    protected StaffChat staffChat;

    public StaffChatCommand(StaffChat staffChat){
        super("sc", "stargate.staffchat");
        this.staffChat = staffChat;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0){
            if (staffChat.isForceChatting(player)) {
                staffChat.forceChattRemove(player);
            }else {
                staffChat.forceChattAdd(player);
            }
            return;
        }


        switch (args[0]){
            case "help":
                player.sendMessage(new TextComponent("§6Staff Chat:\n"+
                        "§7/sc <message> : Sends message to StaffChat\n"+
                        "§7/sc : Turns force StaffChat <on|off>\n"+
                        "§3You can also use " + StarGate.getInstance().cfg.getString("StaffChatCaller")+ " before message to send message to StaffChat"));
                return;
            default:
                String message = "";
                for (int i = 0; i < args.length; i++){
                    message += args[i] + " ";
                }
                staffChat.sendMessage(player, message);
        }
    }
}
