package fun.kaituo.aichanspigot.client;


import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;
import fun.kaituo.aichanspigot.AiChanSpigot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.IDisconnectHandler;
import org.xsocket.connection.INonBlockingConnection;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements IDataHandler, IConnectHandler, IDisconnectHandler {
    private final AiChanSpigot plugin;

    private final String serverName;

    private final String groupMessagePrefix;

    private final String trigger;
    private final String broadcastTrigger;

    public ClientHandler(AiChanSpigot plugin) {
        this.plugin = plugin;
        this.serverName = plugin.getConfig().getString("server-name");
        this.trigger = plugin.getConfig().getString("trigger");
        this.broadcastTrigger = plugin.getConfig().getString("broadcast-trigger");
        this.groupMessagePrefix = plugin.getConfig().getString("group-message-prefix");
    }

    public boolean onConnect(INonBlockingConnection nbc) throws BufferUnderflowException {
        String remoteName = nbc.getRemoteAddress().getHostName();
        this.plugin.getLogger().info("已连接到 " + remoteName);
        return true;
    }

    public boolean onDisconnect(INonBlockingConnection nbc) {
        String remoteName = nbc.getRemoteAddress().getHostName();
        this.plugin.getLogger().warning("连接从 " + remoteName + "断开");
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


        switch (packet.getPacketType()) {
            case SERVER_TEXT -> {
                if (packet.get(0).equals(this.trigger) || packet.get(0).equals(this.broadcastTrigger)) {
                    Bukkit.broadcastMessage(groupMessagePrefix + packet.get(1));
                }
            }
            case PLAYER_NOT_FOUND -> {
                String name = packet.get(0);
                plugin.pendingIds.remove(name);
                if (plugin.getConfig().getBoolean("enable-whitelist")) {
                    plugin.kickPlayerIfOnline(name, plugin.getConfig().getString("not-whitelisted-message"));
                }
            }
            case PLAYER_STATUS -> {
                String name = packet.get(2);
                plugin.pendingIds.remove(name);
                // isBanned
                if (Boolean.parseBoolean(packet.get(3))) {
                    if (plugin.getConfig().getBoolean("enable-whitelist")) {
                        plugin.kickPlayerIfOnline(name, plugin.getConfig().getString("banned-message"));
                    }
                    break;
                }
                // isLinked
                if (!Boolean.parseBoolean(packet.get(1))) {
                    if (plugin.getConfig().getBoolean("enable-whitelist")) {
                        plugin.kickPlayerIfOnline(name, plugin.getConfig().getString("timeout-message"));

                    }
                }
            }
            case LIST_REQUEST -> {
                SocketPacket listPacket = new SocketPacket(SocketPacket.PacketType.GROUP_TEXT);
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                if (players.isEmpty()) {
                    listPacket.set(0, this.serverName + "无人在线");
                } else {
                    StringBuilder listMessage = new StringBuilder();
                    for (int i = 0; i < players.size(); i++) {
                        listMessage.append(players.get(i).getName());
                        if (i != players.size() - 1)
                            listMessage.append(", ");
                    }
                    listPacket.set(0, this.serverName + "有" + players.size() + "人在线: " + listMessage);
                }
                plugin.getClient().sendPacket(listPacket);
            }
            case SERVER_COMMAND -> {
                if (packet.get(0).equals(this.trigger) || packet.get(0).equals(this.broadcastTrigger)) {
                    Bukkit.getScheduler().runTask(plugin,
                            () -> Bukkit.dispatchCommand(plugin.getCommandSender(), packet.get(1)));
                }
            }
        }
        return true;
    }
}
