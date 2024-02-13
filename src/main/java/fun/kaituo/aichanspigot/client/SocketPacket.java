package fun.kaituo.aichanspigot.client;


import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Remember to update Packet on the other end
public class SocketPacket {

    public static final Gson GSON = new Gson();
    @Expose
    @SerializedName("packetType")
    private final PacketType packetType;
    @Expose
    @SerializedName("channelId")
    private String channelId;
    @Expose
    @SerializedName("content")
    private String content;

    public SocketPacket(PacketType packetType) {
        this.packetType = packetType;
    }

    @SuppressWarnings("unused")
    public static SocketPacket fromJsonString(String string) {
        return GSON.fromJson(string, SocketPacket.class);
    }

    @SuppressWarnings("unused")
    public String toJsonString() {
        return GSON.toJson(this);
    }

    public PacketType getPacketType() {
        return packetType;
    }

    @SuppressWarnings("unused")
    public String getChannelId() {
        return channelId;
    }

    @SuppressWarnings("unused")
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @SuppressWarnings("unused")
    public String getContent() {
        return content;
    }

    @SuppressWarnings("unused")
    public void setContent(String content) {
        this.content = content;
    }

    @SuppressWarnings("unused")
    public enum PacketType {
        HEARTBEAT, MESSAGE_TO_SERVER, MESSAGE_TO_CHANNEL, COMMAND, LIST_REQUEST
    }
}