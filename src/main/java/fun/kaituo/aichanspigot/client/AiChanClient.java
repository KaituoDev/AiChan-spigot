package fun.kaituo.aichanspigot.client;


import com.google.gson.Gson;
import fun.kaituo.aichanspigot.AiChanSpigot;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static fun.kaituo.aichanspigot.Utils.fixMinecraftColor;

public class AiChanClient extends WebSocketClient {

    public static final Gson GSON = new Gson();
    private final AiChanSpigot plugin;


    private final String serverName;

    private final String trigger;
    private final String broadcastTrigger;

    public AiChanClient(AiChanSpigot plugin, URI uri) {
        super(uri);
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();
        this.serverName = config.getString("server-name");
        this.trigger = config.getString("trigger");
        this.broadcastTrigger = config.getString("broadcast-trigger");
        int heartbeatInterval = config.getInt("heart-beat-interval");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::connect);
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, this::keepAlive, heartbeatInterval, heartbeatInterval);
    }

    public void sendPacket(SocketPacket packet) {
        if (!isOpen()) {
            plugin.getLogger().warning("连接处于关闭状态，发送包失败");
            keepAlive();
            return;
        }
        String data = GSON.toJson(packet);
        String encryptedData = plugin.getFernetManager().encrypt(data);
        send(encryptedData);
    }

    private void keepAlive() {
        if (!isOpen()) {
            reconnect();
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        plugin.getLogger().info("已连接到WebSocket服务器: " + getRemoteSocketAddress());
    }

    @Override
    public void onMessage(String s) {
        String data = plugin.getFernetManager().decrypt(s);
        SocketPacket packet = SocketPacket.fromJsonString(data);

        switch (packet.getPacketType()) {
            case GROUP_CHAT_TO_SERVER -> {
                if (packet.get(0).equals(this.trigger)) {
                    break;
                }
                String message = fixMinecraftColor(packet.get(1));
                Bukkit.broadcastMessage(message);
            }
            case LIST_REQUEST_TO_SERVER -> {
                SocketPacket listPacket = new SocketPacket(SocketPacket.PacketType.SERVER_INFORMATION_TO_BOT);
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                if (players.isEmpty()) {
                    listPacket.add(0, String.format("%s无人在线", this.serverName));
                } else {
                    StringJoiner listMessage = new StringJoiner(", ");
                    for (Player player : players) {
                        listMessage.add(player.getName());
                    }

                    listPacket.add(0, String.format("%s有 %d 人在线: %s", this.serverName, players.size(), listMessage));
                }
                plugin.getClient().sendPacket(listPacket);
            }
            case COMMAND_TO_SERVER -> {
                if (packet.get(0).equals(this.trigger) || packet.get(0).equals(this.broadcastTrigger)) {
                    Bukkit.getScheduler().runTask(
                            plugin,
                            () -> Bukkit.dispatchCommand(plugin.getCommandSender(), packet.get(1))
                    );
                }
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        plugin.getLogger().warning("连接断开，状态码为 " + code + "，额外信息为 " + reason);
    }

    @Override
    public void onError(Exception e) {
        plugin.getLogger().warning("发生内部错误: " + e.getMessage());
    }

}
