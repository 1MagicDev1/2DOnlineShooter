package n.e.k.o.shared.packets;

import n.e.k.o.shared.NetworkUtils;
import n.e.k.o.shared.packets.internal.APacket;

import java.io.InputStream;
import java.io.OutputStream;

public class LongPacket extends APacket<LongPacket> {

    public long value;

    @Override
    public LongPacket readPacket(InputStream in) throws Throwable {
        this.bytes = NetworkUtils.ensureRead(8, in);
        return this;
    }

    @Override
    public LongPacket parse() throws Throwable {
        super.value = this.value =
            ((long)(this.bytes[7] & 0xFF)        |
            ((long)(this.bytes[6] & 0xFF) << 8)  |
            ((long)(this.bytes[5] & 0xFF) << 16) |
            ((long)(this.bytes[4] & 0xFF) << 24) |
            ((long)(this.bytes[3] & 0xFF) << 32) |
            ((long)(this.bytes[2] & 0xFF) << 40) |
            ((long)(this.bytes[1] & 0xFF) << 48) |
            ((long)(this.bytes[0] & 0xFF) << 56));
        return this;
    }

    @Override
    public LongPacket build(Object value) throws Throwable {
        long l = (long) value;
        super.value = this.value = l;
        this.bytes = new byte[] {
            (byte) ((l >> 56) & 0xFF),
            (byte) ((l >> 48) & 0xFF),
            (byte) ((l >> 40) & 0xFF),
            (byte) ((l >> 32) & 0xFF),
            (byte) ((l >> 24) & 0xFF),
            (byte) ((l >> 16) & 0xFF),
            (byte) ((l >> 8) & 0xFF),
            (byte) (l & 0xFF)
        };
        return this;
    }

    @Override
    public LongPacket sendPacket(OutputStream out) throws Throwable {
        out.write(LONG_TYPE);
        out.write(this.bytes);
        out.flush();
        return this;
    }

}
