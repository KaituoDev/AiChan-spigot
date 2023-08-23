package fun.kaituo.aichanspigot.listener;

import fun.kaituo.aichanspigot.AiChanSpigot;
import fun.kaituo.aichanspigot.client.SocketPacket;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class WhitelistListener implements Listener {

    private final AiChanSpigot plugin;

    public WhitelistListener(AiChanSpigot plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.PLAYER_LOOKUP);
        String name = e.getPlayer().getName();
        packet.set(0, name);
        if (!plugin.getClient().sendPacket(packet)) {
            if (plugin.getConfig().getBoolean("enable-whitelist")) {
                plugin.kickPlayerIfOnline(name, plugin.getConfig().getString("timeout-message"));

            }
            return;
        }

        plugin.pendingIds.add(name);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (plugin.pendingIds.contains(name)) {
                if (plugin.getConfig().getBoolean("enable-whitelist")) {
                    plugin.kickPlayerIfOnline(name, plugin.getConfig().getString("timeout-message"));
                }
            }
        }, plugin.getConfig().getLong("whitelist-timeout"));
    }
}
