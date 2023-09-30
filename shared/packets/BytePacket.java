package n.e.k.o.shared.packets;

import n.e.k.o.shared.NetworkUtils;
import n.e.k.o.shared.packets.internal.APacket;

import java.io.InputStream;
import java.io.OutputStream;

public class BytePacket extends APacket<BytePacket> {

    public byte value;
    public boolean booleanValue;

    @Override
    public BytePacket readPacket(InputStream in) throws Throwable {
        this.bytes = NetworkUtils.ensureRead(1, in);
        return this;
    }

    @Override
    public BytePacket parse() throws Throwable {
        super.value = this.value = this.bytes[0];
        this.booleanValue = this.value != 0;
        return this;
    }

    @Override
    public BytePacket build(Object value) throws Throwable {
        byte b;
        if (value instanceof Byte)
            b = (byte) value;
        else
            b = (byte) (((boolean) value) ? 1 : 0);
        super.value = this.value = b;
        this.bytes = new byte[] { b };
        return this;
    }

    @Override
    public BytePacket sendPacket(OutputStream out) throws Throwable {
        out.write(BYTE_TYPE);
        out.write(value);
        out.flush();
        return this;
    }

}
