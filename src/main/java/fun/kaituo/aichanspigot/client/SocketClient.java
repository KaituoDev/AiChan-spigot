package fun.kaituo.aichanspigot.client;


import com.alibaba.fastjson2.JSON;
import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;
import fun.kaituo.aichanspigot.AiChanSpigot;
import org.bukkit.Bukkit;
import org.xsocket.connection.NonBlockingConnection;

import java.io.IOException;

public class SocketClient {
    private final AiChanSpigot plugin;
    private NonBlockingConnection nbc;
    private String ip;
    private int port;
    private int heartbeatInterval;

    public SocketClient(AiChanSpigot plugin) {
        this.plugin = plugin;
        this.ip = plugin.getConfig().getString("ip");
        this.port = plugin.getConfig().getInt("port");
        this.heartbeatInterval = plugin.getConfig().getInt("heart-beat-interval");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::connect);
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, this::keepAlive, this.heartbeatInterval, this.heartbeatInterval);
    }

    public boolean sendPacket(SocketPacket packet) {

        String data = JSON.toJSONString(packet);
        String encryptedData;
        try {
            Key key = new Key(plugin.getConfig().getString("token"));
            Token token = Token.generate(key, data);
            encryptedData = token.serialise();
        } catch (Exception e) {
            this.plugin.getLogger().warning("信息加密失败，请检查token是否合法！");
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
        if (this.nbc != null) {
            if (this.nbc.isOpen()) {
                SocketPacket packet = new SocketPacket(SocketPacket.PacketType.HEARTBEAT);
                packet.set(0, "Heartbeat test message");
                sendPacket(packet);
            } else {
                connect();
            }
        } else {
            connect();
        }
    }

    private void connect() {
        if (this.nbc != null &&
                this.nbc.isOpen())
            this.plugin.getLogger().warning("连接未中断，取消建立新连接");
        try {
            this.nbc = new NonBlockingConnection(this.ip, this.port, plugin.getHandler());
            this.nbc.setEncoding("UTF-8");
            this.nbc.setAutoflush(true);
        } catch (IOException e) {
            this.plugin.getLogger().warning("连接到" + this.ip + ":" + this.port + "失败！");
        }
    }

    public void close() {
        if (this.nbc != null && this.nbc.isOpen())
            try {
                this.nbc.close();
            } catch (IOException e) {
                this.plugin.getLogger().warning("客户端关闭失败！");
            }
        this.nbc = null;
    }
}
