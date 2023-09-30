package n.e.k.o.shared.packets.internal;

import n.e.k.o.shared.NetworkUtils;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class APacket<T> implements IPacket {

    public Object value;
    public byte[] bytes;

    public byte type;

    public static APacket<? extends APacket> read(InputStream in) throws Throwable {
        return (APacket<APacket>) read(in, APacket.class);
    }

    public static <T extends APacket<T>> T read(InputStream in, Class<T> type) throws Throwable {
        byte packetType = NetworkUtils.ensureRead(1, in)[0];
        if (!reversedPacketTypes.containsKey(packetType))
            throw new IllegalStateException("Illegal packet type received: " + packetType);
        else if (type == null)
            type = (Class<T>) reversedPacketTypes.get(packetType);
        else if (packetType != packetTypes.get(type)) {
            byte _type = packetTypes.get(type);
            if (_type != 0)
                throw new IllegalStateException("Wrong packet with type " + _type + ". Expected packet: " + type.getSimpleName() +
                        ". Received packet: " + reversedPacketTypes.getOrDefault(packetType, APacket.class).getSimpleName());
            type = (Class<T>) reversedPacketTypes.get(packetType);
        }
        T instance = type.newInstance(); // new IntPacket(), new FloatPacket(), new WhateverPacket();
        instance.type = packetType;
        instance.readPacket(in);
        instance.parse();
        return instance;
    }

    public static <T extends APacket<T>> T send(OutputStream out, Object value, Class<T> type) throws Throwable {
        if (type == null)
            throw new NullPointerException("Can't build an empty packet (type = null).");
        T instance = type.newInstance();
        instance.build(value);
        instance.sendPacket(out);
        return instance;
    }

}
