package alemiz.stargate.staffchat;
import alemiz.stargate.StarGate;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class StaffListener implements Listener {

    protected StaffChat staffChat;

    public StaffListener(StaffChat staffChat){
        this.staffChat = staffChat;
    }

    @EventHandler
    public void onChat(ChatEvent event){
        if (!(event.getSender() instanceof ProxiedPlayer)) return;
        String message = event.getMessage();
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("stargate.staffchat")) return;
        if (event.getMessage().startsWith("/")) return;

        if (this.staffChat.isForceChatting(player)){
            event.setCancelled(true);
            this.staffChat.sendMessage(player, message);
            return;
        }

        String caller = StarGate.getInstance().cfg.getString("StaffChatCaller");
        if (!message.startsWith(caller)) return;
        event.setCancelled(true);

        String finalMessage = message.replace(caller, "");
        this.staffChat.sendMessage(player, finalMessage);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event){
        ProxiedPlayer player = event.getPlayer();

        if (player.hasPermission("stargate.staffchat")){
            if (staffChat.isForceChatting(player)){
                staffChat.forceStaff.remove(player.getDisplayName());
            }
        }
    }
}
