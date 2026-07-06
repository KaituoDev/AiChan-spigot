package fun.kaituo.aichanspigot;


import fun.kaituo.aichanspigot.client.AiChanClient;
import fun.kaituo.aichanspigot.client.SocketPacket;
import fun.kaituo.aichanspigot.listener.NotifyOnJoinAndLeaveListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static fun.kaituo.aichanspigot.Utils.fixMinecraftColor;

public class AiChanSpigot extends JavaPlugin implements Listener {

    private FernetManager fernetManager;
    private AiChanClient client;
    private final ConcurrentHashMap<String, AuthResult> pendingAuths = new ConcurrentHashMap<>();
    private final AtomicInteger sessionCounter = new AtomicInteger(0);


    public FernetManager getFernetManager() {
        return fernetManager;
    }

    public AiChanClient getClient() {
        return client;
    }
    private String serverPrefix;

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
        String uriString = "ws://" + getConfig().getString("ip") + ":" + getConfig().getInt("port");
        this.client = new AiChanClient(this, new URI(uriString));
    }

    public void executeBotCommand(String cmd, String contextJson) {
        Bukkit.getScheduler().runTask(this, () -> {
            CommandSender contextualSender = Bukkit.createCommandSender(component -> {
                SocketPacket packet = new SocketPacket(SocketPacket.PacketType.SERVER_COMMAND_FEEDBACK_TO_BOT);
                String message = PlainTextComponentSerializer.plainText().serialize(component);

                packet.add(0, contextJson);
                packet.add(1, fixMinecraftColor(serverPrefix + " " + message));

                client.sendPacket(packet);
            });
            Bukkit.dispatchCommand(contextualSender, cmd);
        });
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!getConfig().getBoolean("enable-whitelist")) {
            return;
        }

        String name = event.getName().toLowerCase();
        String sessionId = String.valueOf(sessionCounter.incrementAndGet());

        AuthResult result = new AuthResult();
        pendingAuths.put(sessionId, result);

        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.SERVER_PLAYER_LOOKUP_REQUEST_TO_BOT);
        packet.add(0, name);
        packet.add(1, sessionId);
        client.sendPacket(packet);

        try {
            result.future.get(getConfig().getInt("whitelist-timeout"), TimeUnit.SECONDS);
            if (!result.isAuthorized) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(result.reason));
            }
        } catch (Exception e) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(getConfig().getString("timeout-message")));
        } finally {
            pendingAuths.remove(sessionId);
        }
    }

    public void kickPlayerIfOnline(String name, String message) {
        Bukkit.getScheduler().runTask(this, () -> {
            Player player = Bukkit.getPlayerExact(name);
            if (player != null) {
                player.kick(Component.text(message));
            }
        });
    }

    public ConcurrentHashMap<String, AuthResult> getPendingAuths() {
        return pendingAuths;
    }

    public static class AuthResult {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        volatile boolean isAuthorized = false;
        volatile String reason;

        public void complete(boolean authorized, String kickReason) {
            this.isAuthorized = authorized;
            this.reason = kickReason;
            future.complete(null);
        }
    }
}
