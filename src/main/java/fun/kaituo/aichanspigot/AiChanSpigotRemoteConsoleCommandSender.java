package fun.kaituo.aichanspigot;

import fun.kaituo.aichanspigot.client.SocketPacket;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AiChanSpigotRemoteConsoleCommandSender implements RemoteConsoleCommandSender {

    private final AiChanSpigot plugin;
    private boolean isOp = true;
    private final Set<PermissionAttachment> permissionAttachments = new HashSet<>();
    private final String serverPrefix;

    public AiChanSpigotRemoteConsoleCommandSender(AiChanSpigot plugin) {
        this.plugin = plugin;
        this.serverPrefix = plugin.getConfig().getString("server-prefix");
    }

    @Override
    public void sendMessage(@NotNull String s) {
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.GROUP_TEXT);
        String msg = s.replaceAll("&.|§.", "");
        packet.set(0, serverPrefix + msg);
        plugin.getClient().sendPacket(packet);
    }

    @Override
    public void sendMessage(String... strings) {
        for (String s : strings) {
            sendMessage(s);
        }
    }

    @Override
    public void sendMessage(UUID uuid, @NotNull String s) {
        sendMessage(s);
    }

    @Override
    public void sendMessage(UUID uuid, String... strings) {
        sendMessage(strings);
    }

    @Override
    public @NotNull Server getServer() {
        return plugin.getServer();
    }

    @Override
    public @NotNull String getName() {
        return "AiChan";
    }

    @Override
    public @NotNull Spigot spigot() {
        return plugin.getCommandSender().spigot();
    }

    @Override
    public boolean isPermissionSet(@NotNull String s) {
        return true;
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull String s) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return true;
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b) {
        PermissionAttachment attachment = new PermissionAttachment(plugin, this);
        attachment.setPermission(s, b);
        permissionAttachments.add(attachment);
        return attachment;
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        PermissionAttachment attachment = new PermissionAttachment(plugin, this);
        permissionAttachments.add(attachment);
        return attachment;
    }

    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i) {
        PermissionAttachment attachment = new PermissionAttachment(plugin, this);
        attachment.setPermission(s, b);
        permissionAttachments.add(attachment);
        Bukkit.getScheduler().runTaskLater(plugin, () -> permissionAttachments.remove(attachment), i);
        return attachment;
    }

    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
        PermissionAttachment attachment = new PermissionAttachment(plugin, this);
        permissionAttachments.add(attachment);
        Bukkit.getScheduler().runTaskLater(plugin, () -> permissionAttachments.remove(attachment), i);
        return attachment;
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment permissionAttachment) {
        permissionAttachments.remove(permissionAttachment);
    }

    @Override
    public void recalculatePermissions() {

    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        Set<PermissionAttachmentInfo> info = new HashSet<>();
        for (PermissionAttachment a : new HashSet<>(permissionAttachments)) {
            for (Map.Entry<String, Boolean> e : a.getPermissions().entrySet()) {
                info.add(new PermissionAttachmentInfo(a.getPermissible(), e.getKey(), a, e.getValue()));
            }
        }
        return info;
    }

    @Override
    public boolean isOp() {
        return isOp;
    }

    @Override
    public void setOp(boolean b) {
        isOp = b;
    }

    @NotNull
    @Override
    public SocketAddress getAddress() {
        try {
            InetAddress address = InetAddress.getByName(plugin.getConfig().getString("ip"));
            SocketAddress socketAddress = new InetSocketAddress(address, plugin.getConfig().getInt("port"));
            return socketAddress;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
