package fun.kaituo.aichanspigot;

import fun.kaituo.aichanspigot.client.SocketPacket;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RemoteSender implements RemoteConsoleCommandSender {

    private final AiChanSpigot plugin;
    private final Set<PermissionAttachment> permissionAttachments = new HashSet<>();

    public RemoteSender(AiChanSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sendMessage(@NotNull String s) {
        SocketPacket packet = new SocketPacket(SocketPacket.PacketType.MESSAGE_TO_CHANNEL);
        packet.setChannelId(plugin.getChannelId());
        String msg = s;
        msg = msg.replaceAll("&.", "");
        msg = msg.replaceAll("§.", "");
        packet.setContent(msg);
        plugin.getClient().sendPacket(packet);
    }

    @Override
    public void sendMessage(@NotNull String... strings) {
        for (String s : strings) {
            sendMessage(s);
        }
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String s) {
        sendMessage(s);
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String... strings) {
        sendMessage(strings);
    }

    @NotNull
    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @NotNull
    @Override
    public String getName() {
        return "AiChan Console";
    }

    @NotNull
    @Override
    public Spigot spigot() {
        plugin.getLogger().warning("What are you doing?");
        return new Spigot();
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
        return true;
    }

    @Override
    public void setOp(boolean b) {

    }

    @NotNull
    @Override
    public SocketAddress getAddress() {
        return plugin.getClient().getRemoteSocketAddress();
    }
}
