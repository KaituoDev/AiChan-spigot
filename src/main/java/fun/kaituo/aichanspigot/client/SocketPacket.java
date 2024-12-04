package fun.kaituo.aichanspigot.client;


import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// Remember to update Packet on the other end
public class SocketPacket {

    public static final Gson GSON = new Gson();
    @Expose
    @SerializedName("packetType")
    private final PacketType packetType;
    @Expose
    @SerializedName("content")
    private final List<String> content = new ArrayList<>();

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
    public List<String> getContent() {
        return new ArrayList<>(content);
    }

    @SuppressWarnings("unused")
    public String get(int index) {
        return content.get(index);
    }

    @SuppressWarnings("unused")
    public void add(int index, String data) {
        this.content.add(index, data);
    }

    @SuppressWarnings("unused")
    public enum PacketType {
        HEARTBEAT_TO_BOT, SERVER_CHAT_TO_BOT, GROUP_CHAT_TO_SERVER,
        PLAYER_LOOKUP_REQUEST_TO_BOT, PLAYER_LOOKUP_RESULT_TO_SERVER,
        PLAYER_NOT_FOUND_TO_SERVER, LIST_REQUEST_TO_SERVER, COMMAND_TO_SERVER,
        SERVER_INFORMATION_TO_BOT
    }
}