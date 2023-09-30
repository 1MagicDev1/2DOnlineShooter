package n.e.k.o.shared.packets;

import n.e.k.o.shared.NetworkUtils;
import n.e.k.o.shared.packets.internal.APacket;

import java.io.InputStream;
import java.io.OutputStream;

public class DoublePacket extends APacket<DoublePacket> {

    public double value;

    @Override
    public DoublePacket readPacket(InputStream in) throws Throwable {
        this.bytes = NetworkUtils.ensureRead(8, in);
        return this;
    }

    @Override
    public DoublePacket parse() throws Throwable {
        long l =
            ((long)(this.bytes[7] & 0xFF)        |
            ((long)(this.bytes[6] & 0xFF) << 8)  |
            ((long)(this.bytes[5] & 0xFF) << 16) |
            ((long)(this.bytes[4] & 0xFF) << 24) |
            ((long)(this.bytes[3] & 0xFF) << 32) |
            ((long)(this.bytes[2] & 0xFF) << 40) |
            ((long)(this.bytes[1] & 0xFF) << 48) |
            ((long)(this.bytes[0] & 0xFF) << 56));
        double d = Double.longBitsToDouble(l);
        super.value = this.value = d;
        return this;
    }

    @Override
    public DoublePacket build(Object value) throws Throwable {
        double d = (double) value;
        super.value = this.value = d;
        long l = Double.doubleToLongBits(d);
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
    public DoublePacket sendPacket(OutputStream out) throws Throwable {
        out.write(DOUBLE_TYPE);
        out.write(this.bytes);
        out.flush();
        return this;
    }

}
