package n.e.k.o.shared.packets;

import n.e.k.o.shared.NetworkUtils;
import n.e.k.o.shared.packets.internal.APacket;

import java.io.InputStream;
import java.io.OutputStream;

public class ShortPacket extends APacket<ShortPacket> {

    public short value;

    @Override
    public ShortPacket readPacket(InputStream in) throws Throwable {
        this.bytes = NetworkUtils.ensureRead(2, in);
        return this;
    }

    @Override
    public ShortPacket parse() throws Throwable {
        super.value = this.value = (short) (
            ((this.bytes[1] & 0xFF) << 8) |
             (this.bytes[0] & 0xFF)
        );
        return this;
    }

    @Override
    public ShortPacket build(Object value) throws Throwable {
        short s = (short) value;
        super.value = this.value = s;
        this.bytes = new byte[] {
            (byte) (s & 0xFF),
            (byte) ((s >> 8) & 0xFF)
        };
        return this;
    }

    @Override
    public ShortPacket sendPacket(OutputStream out) throws Throwable {
        out.write(SHORT_TYPE);
        out.write(this.bytes);
        out.flush();
        return this;
    }

}
