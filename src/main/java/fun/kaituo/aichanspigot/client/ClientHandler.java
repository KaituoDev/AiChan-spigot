package fun.kaituo.aichanspigot.client;


import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;
import fun.kaituo.aichanspigot.AiChanSpigot;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.IDisconnectHandler;
import org.xsocket.connection.INonBlockingConnection;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ClientHandler implements IDataHandler, IConnectHandler, IDisconnectHandler {
    private final AiChanSpigot plugin;

    private final String serverName;

    private final String groupMessagePrefix;

    private final String trigger;
    private final String broadcastTrigger;

    public ClientHandler(AiChanSpigot plugin) {
        FileConfiguration config = plugin.getConfig();

        this.plugin = plugin;
        this.serverName = config.getString("server-name");
        this.trigger = config.getString("trigger");
        this.broadcastTrigger = config.getString("broadcast-trigger");
        this.groupMessagePrefix = config.getString("group-message-prefix");
    }

    public boolean onConnect(INonBlockingConnection nbc) throws BufferUnderflowException {
        String remoteName = nbc.getRemoteAddress().getHostName();
        this.plugin.getLogger().info(String.format("已连接到 %s", remoteName));
        return true;
    }

    public boolean onDisconnect(INonBlockingConnection nbc) {
        String remoteName = nbc.getRemoteAddress().getHostName();
        this.plugin.getLogger().warning(String.format("连接从 %s 断开", remoteName));
        return true;
    }

    public boolean onData(INonBlockingConnection nbc) throws IOException, BufferUnderflowException {
        String encryptedData = nbc.readStringByDelimiter(SocketPacket.DELIMITER);
        Token token = Token.fromString(encryptedData);
        Key key = new Key(plugin.getConfig().getString("token"));

        String data;
        try {
            data = token.validateAndDecrypt(key, plugin.validator);
        } catch (Exception e) {
            nbc.close();
            plugin.getLogger().warning("解密失败，断开连接！");
            return true;
        }

        SocketPacket packet = SocketPacket.parsePacket(data);

        FileConfiguration config = plugin.getConfig();

        switch (packet.getPacketType()) {
            case SERVER_TEXT -> {
                if (packet.get(0).equals(this.trigger) || packet.get(0).equals(this.broadcastTrigger)) {
                    Bukkit.broadcastMessage(groupMessagePrefix + packet.get(1));
                }
            }
            case PLAYER_NOT_FOUND -> {
                String name = packet.get(0);
                plugin.pendingIds.remove(name);
                if (config.getBoolean("enable-whitelist")) {
                    plugin.kickPlayerIfOnline(name, config.getString("not-whitelisted-message"));
                }
            }
            case PLAYER_STATUS -> {
                String name = packet.get(2);
                plugin.pendingIds.remove(name);

                boolean isLinked = Boolean.parseBoolean(packet.get(1));
                boolean isBanned = Boolean.parseBoolean(packet.get(3));

                if (isBanned && config.getBoolean("enable-whitelist")) {
                    plugin.kickPlayerIfOnline(name, config.getString("banned-message"));
                } else if (!isLinked && config.getBoolean("enable-whitelist")) {
                    plugin.kickPlayerIfOnline(name, config.getString("timeout-message"));
                }
            }
            case LIST_REQUEST -> {
                SocketPacket listPacket = new SocketPacket(SocketPacket.PacketType.GROUP_TEXT);
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                if (players.isEmpty()) {
                    listPacket.set(0, String.format("%s无人在线", this.serverName));
                } else {
                    StringJoiner listMessage = new StringJoiner(", ");
                    for (Player player : players) {
                        listMessage.add(player.getName());
                    }

                    listPacket.set(0, String.format("%s有 %d 人在线: %s", this.serverName, players.size(), listMessage));
                }
                plugin.getClient().sendPacket(listPacket);
            }
            case SERVER_COMMAND -> {
                if (packet.get(0).equals(this.trigger) || packet.get(0).equals(this.broadcastTrigger)) {
                    Bukkit.getScheduler().runTask(
                            plugin,
                            () -> Bukkit.dispatchCommand(plugin.getCommandSender(), packet.get(1))
                    );
                }
            }
        }
        return true;
    }
}
