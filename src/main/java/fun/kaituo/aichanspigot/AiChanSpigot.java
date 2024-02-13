package fun.kaituo.aichanspigot;


import fun.kaituo.aichanspigot.client.AiChanClient;
import fun.kaituo.aichanspigot.client.SocketPacket;
import fun.kaituo.aichanspigot.listener.NotifyOnJoinAndLeaveListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.URISyntaxException;

public class AiChanSpigot extends JavaPlugin implements Listener {

    private String channelId;

    public String getChannelId() {
        return channelId;
    }

    private FernetManager fernetManager;

    public FernetManager getFernetManager() {
        return fernetManager;
    }

    private AiChanClient client;

    public AiChanClient getClient() {
        return client;
    }


    // Remote sender only works for non-native commands
    private RemoteSender remoteSender;

    public RemoteSender getRemoteSender() {
        return remoteSender;
    }

    // Console sender only works for native commands
    private ConsoleSender consoleSender;

    public ConsoleSender getConsoleSender() {
        return consoleSender;
    }

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

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.MESSAGE_TO_CHANNEL);
        packet.setChannelId(channelId);
        String msg = String.format("%s: %s", e.getPlayer().getName(), e.getMessage());
        msg = msg.replaceAll("&.|§.", "");
        packet.setContent(msg);
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
        this.remoteSender = new RemoteSender(this);
        this.consoleSender = new ConsoleSender(this, Bukkit.getConsoleSender());
        String uriString = "ws://" + getConfig().getString("ip") + ":" + getConfig().getInt("port");
        channelId = getConfig().getString("channel_id");
        this.client = new AiChanClient(this, new URI(uriString));
    }
}
