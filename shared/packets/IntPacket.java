package n.e.k.o.shared.packets;

import n.e.k.o.shared.NetworkUtils;
import n.e.k.o.shared.packets.internal.APacket;

import java.io.InputStream;
import java.io.OutputStream;

public class IntPacket extends APacket<IntPacket> {

    public int value;

    @Override
    public IntPacket readPacket(InputStream in) throws Throwable {
        this.bytes = NetworkUtils.ensureRead(4, in);
        return this;
    }

    @Override
    public IntPacket parse() throws Throwable {
        super.value = this.value =
            ((this.bytes[3] & 0xFF)        |
            ((this.bytes[2] & 0xFF) << 8)  |
            ((this.bytes[1] & 0xFF) << 16) |
            ((this.bytes[0] & 0xFF) << 24));
        return this;
    }

    @Override
    public IntPacket build(Object value) throws Throwable {
        int i = (int) value;
        super.value = this.value = i;
        this.bytes = new byte[] {
            (byte) ((i >> 24) & 0xFF),
            (byte) ((i >> 16) & 0xFF),
            (byte) ((i >> 8) & 0xFF),
            (byte) (i & 0xFF)
        };
        return this;
    }

    @Override
    public IntPacket sendPacket(OutputStream out) throws Throwable {
        out.write(INT_TYPE);
        out.write(this.bytes);
        out.flush();
        return this;
    }

}
