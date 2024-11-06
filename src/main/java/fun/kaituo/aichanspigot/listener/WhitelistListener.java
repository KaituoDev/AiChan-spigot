package fun.kaituo.aichanspigot.listener;

import fun.kaituo.aichanspigot.AiChanSpigot;
import fun.kaituo.aichanspigot.client.SocketPacket;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
        Player player = e.getPlayer();
        String name = player.getName();
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.PLAYER_LOOKUP_REQUEST_TO_BOT);
        packet.set(0, name);

        if (!plugin.getClient().sendPacket(packet)) {
            handleWhitelistTimeout(name);
            return;
        }

        plugin.pendingIds.add(name);
        long whitelistTimeout = plugin.getConfig().getLong("whitelist-timeout");
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> handleWhitelistTimeout(name),
                whitelistTimeout
        );
    }

    private void handleWhitelistTimeout(String playerName) {
        FileConfiguration config = plugin.getConfig();
        if (plugin.pendingIds.contains(playerName) && config.getBoolean("enable-whitelist")) {
            plugin.kickPlayerIfOnline(playerName, config.getString("timeout-message"));
        }
    }
}
