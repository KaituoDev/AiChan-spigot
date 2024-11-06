package fun.kaituo.aichanspigot.client;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

// Remember to update Packet on the other end
public class SocketPacket {
    public static final String DELIMITER = "|DELIMITER|";

    //public static final int MAX_DATA_SIZE = 8;

    public enum PacketType {
        HEARTBEAT_TO_BOT, SERVER_CHAT_TO_BOT, GROUP_CHAT_TO_SERVER,
        PLAYER_LOOKUP_REQUEST_TO_BOT, PLAYER_LOOKUP_RESULT_TO_SERVER,
        PLAYER_NOT_FOUND_TO_SERVER, LIST_REQUEST_TO_SERVER, COMMAND_TO_SERVER,
        SERVER_INFORMATION_TO_BOT
    }

    public SocketPacket(PacketType packetType) {
        this.packetType = packetType;
    }

    public static SocketPacket parsePacket(String string) {
        JsonObject packetObject = JsonParser.parseString(string).getAsJsonObject();
        JsonArray contentArray =packetObject.get("content").getAsJsonArray();
        SocketPacket result = new SocketPacket(
                PacketType.valueOf(packetObject.get("packetType").getAsString())
        );
        if (contentArray != null) {
            IntStream.range(0, contentArray.size()).forEach(
                    i -> result.set(i, contentArray.get(i).getAsString())
            );
        }
        return result;
    }

    public void set(int index, String data) {
        content.add(index, data);
    }

    public String get(int index) {
        return content.get(index);
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public List<String> getContent() {
        return content;
    }

    @Expose
    private final PacketType packetType;

    @Expose
    private final List<String> content = new ArrayList<>();
}