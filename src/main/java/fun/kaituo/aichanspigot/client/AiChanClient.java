package fun.kaituo.aichanspigot.client;


import com.google.gson.Gson;
import fun.kaituo.aichanspigot.AiChanSpigot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class AiChanClient extends WebSocketClient {

    public static final Gson GSON = new Gson();
    private final AiChanSpigot plugin;

    private final String groupMessagePrefix;

    public AiChanClient(AiChanSpigot plugin, URI uri) {
        super(uri);
        this.plugin = plugin;
        int heartbeatInterval = plugin.getConfig().getInt("heart-beat-interval");
        groupMessagePrefix = plugin.getConfig().getString("group-message-prefix");
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

        if (!packet.getChannelId().equals(plugin.getChannelId())) {
            return;
        }

        switch (packet.getPacketType()) {
            case MESSAGE_TO_SERVER -> Bukkit.broadcastMessage(groupMessagePrefix + packet.getContent());
            case COMMAND -> {
                String cmd = packet.getContent();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        Bukkit.dispatchCommand(plugin.getRemoteSender(), cmd);
                    } catch (Exception e) {
                        try {
                            Bukkit.dispatchCommand(plugin.getConsoleSender(), cmd);
                        } catch (Exception e1) {
                            throw new RuntimeException(e1);
                        }
                    }
                });
            }
            case LIST_REQUEST -> {
                SocketPacket listPacket = new SocketPacket(SocketPacket.PacketType.MESSAGE_TO_CHANNEL);
                listPacket.setChannelId(plugin.getChannelId());
                listPacket.setContent(getListMessage());
                plugin.getClient().sendPacket(listPacket);
            }
        }
    }

    private String getListMessage() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            return "当前无人在线";
        } else {
            StringJoiner listMessage = new StringJoiner(", ");
            for (Player player : players) {
                listMessage.add(player.getName());
            }
            return String.format("当前有 %d 人在线: %s", players.size(), listMessage);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        plugin.getLogger().warning("连接断开，状态码为 " + code + "，额外信息为 " + reason);
    }

    @Override
    public void onError(Exception e) {
        plugin.getLogger().warning("发生内部错误！");
        e.printStackTrace();
    }

}
