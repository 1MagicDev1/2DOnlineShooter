package n.e.k.o.shared.packets.internal;

import n.e.k.o.shared.packets.*;
import n.e.k.o.shared.packets.player.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public interface IPacket {

    APacket<?> readPacket(InputStream in) throws Throwable;
    APacket<?> parse() throws Throwable;
    APacket<?> build(Object value) throws Throwable;
    APacket<?> sendPacket(OutputStream out) throws Throwable;

    byte BYTE_TYPE = 1;
    byte SHORT_TYPE = 2;
    byte INT_TYPE = 3;
    byte FLOAT_TYPE = 4;
    byte LONG_TYPE = 5;
    byte DOUBLE_TYPE = 6;
    byte STRING_TYPE = 7;

    // Player types
    byte PLAYER_SPAWN_TYPE = 10;
    byte PLAYER_DISCONNECT_TYPE = 11;
    byte PLAYER_MOVEMENT_TYPE = 12;
    byte PLAYER_SHOOTING_TYPE = 13;
    byte PLAYER_HIT_TYPE = 14;
    byte PLAYER_RESPAWN_TYPE = 15;

    // Others
    byte KEEP_ALIVE_TYPE = 100;

    Map<Class<? extends APacket>, Byte> packetTypes = new HashMap<>() {{
        put(APacket.class, (byte)0);
        // Packet types
        put(BytePacket.class, BYTE_TYPE);
        put(ShortPacket.class, SHORT_TYPE);
        put(IntPacket.class, INT_TYPE);
        put(FloatPacket.class, FLOAT_TYPE);
        put(LongPacket.class, LONG_TYPE);
        put(DoublePacket.class, DOUBLE_TYPE);
        put(StringPacket.class, STRING_TYPE);
        // Event types
        put(PlayerSpawnPacket.class, PLAYER_SPAWN_TYPE);
        put(PlayerDisconnectPacket.class, PLAYER_DISCONNECT_TYPE);
        put(PlayerMovementPacket.class, PLAYER_MOVEMENT_TYPE);
        put(PlayerShootingPacket.class, PLAYER_SHOOTING_TYPE);
        put(PlayerHitPacket.class, PLAYER_HIT_TYPE);
        put(PlayerRespawnPacket.class, PLAYER_RESPAWN_TYPE);
    }};

    Map<Byte, Class<? extends APacket>> reversedPacketTypes = new HashMap<>() {{
        packetTypes.forEach((clz, type) -> put(type, clz));
    }};

}
