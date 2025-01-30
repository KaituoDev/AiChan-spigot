package fun.kaituo.aichanspigot;


import fun.kaituo.aichanspigot.client.AiChanClient;
import fun.kaituo.aichanspigot.client.SocketPacket;
import fun.kaituo.aichanspigot.listener.NotifyOnJoinAndLeaveListener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.URISyntaxException;

import static fun.kaituo.aichanspigot.Utils.fixMinecraftColor;

public class AiChanSpigot extends JavaPlugin implements Listener {

    private FernetManager fernetManager;
    private AiChanClient client;


    public FernetManager getFernetManager() {
        return fernetManager;
    }

    public AiChanClient getClient() {
        return client;
    }
    private String serverPrefix;

    public CommandSender getCommandSender() {
        return commandSender;
    }

    private CommandSender commandSender;

    public void onEnable() {
        saveDefaultConfig();

        try {
            initializeComponents();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        registerEventIfEnabled("notify-on-join-and-quit", new NotifyOnJoinAndLeaveListener(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("AiChanSpigot 已加载");
    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll((Plugin) this);
        this.client.close();
        getLogger().info("AiChanSpigot 已卸载");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.SERVER_CHAT_TO_BOT);
        String msg = String.format("%s: %s", e.getPlayer().getName(), e.getMessage());
        msg = fixMinecraftColor(msg);
        packet.add(0, getConfig().getString("trigger"));
        packet.add(1, getConfig().getString("server-prefix") + " " + msg);
        this.client.sendPacket(packet);
    }

    private void registerEventIfEnabled(String configKey, Listener listener) {
        if (getConfig().getBoolean(configKey)) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    private void initializeComponents() throws URISyntaxException, IllegalArgumentException {
        try {
            this.fernetManager = new FernetManager(getConfig().getString("fernet_key"));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize fernet manager!");
        }
        this.serverPrefix = getConfig().getString("server-prefix");
        this.commandSender = Bukkit.createCommandSender( component -> {
            SocketPacket packet = new SocketPacket(SocketPacket.PacketType.SERVER_INFORMATION_TO_BOT);
            String message = PlainTextComponentSerializer.plainText().serialize(component);
            packet.add(0, fixMinecraftColor(serverPrefix + " " + message));
            client.sendPacket(packet);
        });
        String uriString = "ws://" + getConfig().getString("ip") + ":" + getConfig().getInt("port");
        this.client = new AiChanClient(this, new URI(uriString));
    }
}
