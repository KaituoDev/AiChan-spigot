package fun.kaituo.aichanspigot;

import fun.kaituo.aichanspigot.client.SocketPacket;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.Set;
import java.util.UUID;

public class ConsoleSender implements ConsoleCommandSender {

    private final ConsoleCommandSender console;

    private final AiChanSpigot plugin;

    public ConsoleSender(AiChanSpigot plugin, ConsoleCommandSender console) {
        this.console = console;
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
        return console.getServer();
    }

    @NotNull
    @Override
    public String getName() {
        return "AiChan Console";
    }

    @NotNull
    @Override
    public Spigot spigot() {
        return console.spigot();
    }

    @Override
    public boolean isPermissionSet(@NotNull String s) {
        return console.isPermissionSet(s);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        return console.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(@NotNull String s) {
        return console.hasPermission(s);
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return console.hasPermission(permission);
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b) {
        return console.addAttachment(plugin, s, b);
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return console.addAttachment(plugin);
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i) {
        return console.addAttachment(plugin, s, b, i);
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
        return console.addAttachment(plugin, i);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment permissionAttachment) {
        console.removeAttachment(permissionAttachment);
    }

    @Override
    public void recalculatePermissions() {
        console.recalculatePermissions();
    }

    @NotNull
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return console.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean b) {

    }

    @Override
    public boolean isConversing() {
        return console.isConversing();
    }

    @Override
    public void acceptConversationInput(@NotNull String s) {
        plugin.getLogger().info("accept");
        console.acceptConversationInput(s);
    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        plugin.getLogger().info("begin");
        return console.beginConversation(conversation);
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {
        plugin.getLogger().info("abandon");
        console.abandonConversation(conversation);
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent conversationAbandonedEvent) {

        plugin.getLogger().info("abandon");
        console.abandonConversation(conversation, conversationAbandonedEvent);
    }

    @Override
    public void sendRawMessage(@NotNull String s) {
        sendMessage(s);
    }

    @Override
    public void sendRawMessage(@Nullable UUID uuid, @NotNull String s) {
        sendMessage(s);
    }
}
