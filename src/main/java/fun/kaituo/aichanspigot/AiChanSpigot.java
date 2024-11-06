package fun.kaituo.aichanspigot;


import com.macasaet.fernet.StringValidator;
import com.macasaet.fernet.Validator;
import fun.kaituo.aichanspigot.client.ClientHandler;
import fun.kaituo.aichanspigot.client.SocketClient;
import fun.kaituo.aichanspigot.client.SocketPacket;
import fun.kaituo.aichanspigot.listener.NotifyOnJoinAndLeaveListener;
import fun.kaituo.aichanspigot.listener.WhitelistListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

import static fun.kaituo.aichanspigot.Utils.fixMinecraftColor;

public class AiChanSpigot extends JavaPlugin implements Listener {

    public final Set<String> pendingIds = new HashSet<>();

    public ClientHandler getHandler() {
        return handler;
    }

    private ClientHandler handler;

    public SocketClient getClient() {
        return client;
    }

    private SocketClient client;

    public AiChanSpigotRemoteConsoleCommandSender getCommandSender() {
        return commandSender;
    }

    private AiChanSpigotRemoteConsoleCommandSender commandSender;

    public final Validator<String> validator = new StringValidator() {
    };


    public void kickPlayerIfOnline(String name, String message) {
        Bukkit.getScheduler().runTask(this, () -> {
            Player p = Bukkit.getPlayer(name);
            if (p == null || !p.isOnline()) {
                return;
            }
            p.kickPlayer(message);
        });
    }

    public void onEnable() {
        saveDefaultConfig();
        registerEventIfEnabled("notify-on-join-and-quit", new NotifyOnJoinAndLeaveListener(this));
        registerEventIfEnabled("enable-whitelist", new WhitelistListener(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("AiChanSpigot 已加载");

        initializeComponents();
    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll((Plugin) this);
        this.client.close();
        this.client = null;
        getLogger().info("AiChanSpigot 已卸载");
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.SERVER_CHAT_TO_BOT);
        String msg = String.format("%s: %s", e.getPlayer().getName(), e.getMessage());
        msg = fixMinecraftColor(msg);
        packet.set(0, getConfig().getString("trigger"));
        packet.set(1, getConfig().getString("server-prefix") + " " + msg);
        this.client.sendPacket(packet);
    }

    private void registerEventIfEnabled(String configKey, Listener listener) {
        if (getConfig().getBoolean(configKey)) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    private void initializeComponents() {
        this.client = new SocketClient(this);
        this.handler = new ClientHandler(this);
        this.commandSender = new AiChanSpigotRemoteConsoleCommandSender(this);
    }
}
