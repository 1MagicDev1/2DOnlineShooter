package n.e.k.o.shared.packets;

import n.e.k.o.shared.NetworkUtils;
import n.e.k.o.shared.packets.internal.APacket;

import java.io.InputStream;
import java.io.OutputStream;

public class FloatPacket extends APacket<FloatPacket> {

    public float value;

    @Override
    public FloatPacket readPacket(InputStream in) throws Throwable {
        this.bytes = NetworkUtils.ensureRead(4, in);
        return this;
    }

    @Override
    public FloatPacket parse() throws Throwable {
        int i =
            ((this.bytes[3] & 0xFF)        |
            ((this.bytes[2] & 0xFF) << 8)  |
            ((this.bytes[1] & 0xFF) << 16) |
            ((this.bytes[0] & 0xFF) << 24));
        float f = Float.intBitsToFloat(i);
        super.value = this.value = f;
        return this;
    }

    @Override
    public FloatPacket build(Object value) throws Throwable {
        float f = (float) value;
        super.value = this.value = f;
        int i = Float.floatToIntBits(f);
        this.bytes = new byte[] {
            (byte) ((i >> 24) & 0xFF),
            (byte) ((i >> 16) & 0xFF),
            (byte) ((i >> 8) & 0xFF),
            (byte) (i & 0xFF)
        };
        return this;
    }

    @Override
    public FloatPacket sendPacket(OutputStream out) throws Throwable {
        out.write(FLOAT_TYPE);
        out.write(this.bytes);
        out.flush();
        return this;
    }

}
