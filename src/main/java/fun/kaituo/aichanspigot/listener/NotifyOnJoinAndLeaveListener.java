package fun.kaituo.aichanspigot.listener;

import fun.kaituo.aichanspigot.AiChanSpigot;
import fun.kaituo.aichanspigot.client.SocketPacket;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NotifyOnJoinAndLeaveListener implements Listener {
    private final AiChanSpigot plugin;

    public NotifyOnJoinAndLeaveListener(AiChanSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.SERVER_INFORMATION_TO_BOT);
        String welcomeMessage = String.format(
                "%s%s[+]",
                plugin.getConfig().getString("server-prefix"),
                e.getPlayer().getName()
        );
        if (!e.getPlayer().hasPlayedBefore()) {
            welcomeMessage += String.format(
                    "\n%s玩家 %s 首次加入了服务器！",
                    plugin.getConfig().getString("server-prefix"),
                    e.getPlayer().getName());
        }
        packet.add(0, welcomeMessage);
        plugin.getClient().sendPacket(packet);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.SERVER_INFORMATION_TO_BOT);
        packet.add(0, String.format(
                "%s%s[-]",
                plugin.getConfig().getString("server-prefix"),
                e.getPlayer().getName()
        ));
        plugin.getClient().sendPacket(packet);
    }
}
