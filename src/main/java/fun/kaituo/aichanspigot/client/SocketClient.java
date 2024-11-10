package fun.kaituo.aichanspigot.client;


import com.google.gson.Gson;
import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;
import fun.kaituo.aichanspigot.AiChanSpigot;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.xsocket.connection.NonBlockingConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketClient {
    private final AiChanSpigot plugin;
    private NonBlockingConnection nbc;
    private final String ip;
    private final int port;

    public SocketAddress getSocketAddress() {
        return new InetSocketAddress(nbc.getRemoteAddress(), nbc.getRemotePort());
    }

    public SocketClient(AiChanSpigot plugin) {
        FileConfiguration config = plugin.getConfig();

        this.plugin = plugin;
        this.ip = config.getString("ip");
        this.port = config.getInt("port");
        int heartbeatInterval = config.getInt("heart-beat-interval");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::connect);
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, this::keepAlive, heartbeatInterval, heartbeatInterval);
    }

    public boolean sendPacket(SocketPacket packet) {
        String data = new Gson().toJson(packet);
        String encryptedData;
        try {
            Key key = new Key(plugin.getConfig().getString("token"));
            Token token = Token.generate(key, data);
            encryptedData = token.serialise();
        } catch (Exception e) {
            this.plugin.getLogger().warning("信息加密失败，请检查 Token 是否合法！");
            return false;
        }

        if (this.nbc != null && this.nbc.isOpen())
            try {
                this.nbc.write(encryptedData + SocketPacket.DELIMITER);
            } catch (IOException e) {
                this.plugin.getLogger().warning("发送信息失败，重新建立连接");
                keepAlive();
                return false;
            }
        return true;
    }

    private void keepAlive() {
        if (this.nbc != null && this.nbc.isOpen()) {
            SocketPacket packet = new SocketPacket(SocketPacket.PacketType.HEARTBEAT_TO_BOT);
            packet.set(0, "Heartbeat test message");
            sendPacket(packet);
        } else {
            connect();
        }
    }

    private void connect() {
        if (this.nbc != null && this.nbc.isOpen()) {
            this.plugin.getLogger().warning("连接未中断，取消建立新连接");
            return;
        }
        try {
            this.nbc = new NonBlockingConnection(this.ip, this.port, plugin.getHandler());
            this.nbc.setEncoding("UTF-8");
            this.nbc.setAutoflush(true);
        } catch (IOException e) {
            this.plugin.getLogger().warning(String.format("连接到 %s:%d 失败！", this.ip, this.port));
        }
    }

    public void close() {
        if (this.nbc != null && this.nbc.isOpen()) {
            try {
                this.nbc.close();
            } catch (IOException e) {
                this.plugin.getLogger().warning("客户端关闭失败！");
            }
        }
        this.nbc = null;
    }
}
