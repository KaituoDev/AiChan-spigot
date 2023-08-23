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
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.GROUP_TEXT);
        packet.set(0, plugin.getConfig().getString("server-prefix") + e.getPlayer().getName() + "[+]");
        plugin.getClient().sendPacket(packet);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.GROUP_TEXT);
        packet.set(0, plugin.getConfig().getString("server-prefix") + e.getPlayer().getName() + "[-]");
        plugin.getClient().sendPacket(packet);
    }
}
