package n.e.k.o.shared.packets;

import n.e.k.o.shared.NetworkUtils;
import n.e.k.o.shared.packets.internal.APacket;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StringPacket extends APacket<StringPacket> {

    public String value;

    @Override
    public StringPacket readPacket(InputStream in) throws Throwable {
        // Read string length
        var len = new IntPacket().readPacket(in).parse();

        // Read string bytes
        var bytes = NetworkUtils.ensureRead(len.value, in);

        this.bytes = new byte[4 + bytes.length];
        System.arraycopy(len.bytes, 0, this.bytes, 0, 4);
        System.arraycopy(bytes, 0, this.bytes, 4, bytes.length);
        return this;
    }

    @Override
    public StringPacket parse() throws Throwable {
        super.value = this.value = new String(this.bytes, 4, this.bytes.length - 4, StandardCharsets.UTF_8);
        return this;
    }

    @Override
    public StringPacket build(Object value) throws Throwable {
        String s = (String) value;
        super.value = this.value = s;

        var len = new IntPacket().build(s.length()).parse();
        var bytes = s.getBytes(StandardCharsets.UTF_8);

        this.bytes = new byte[4 + bytes.length];
        System.arraycopy(len.bytes, 0, this.bytes, 0, 4);
        System.arraycopy(bytes, 0, this.bytes, 4, bytes.length);

        return this;
    }

    @Override
    public StringPacket sendPacket(OutputStream out) throws Throwable {
        out.write(STRING_TYPE);

        // Write string length and data
        out.write(this.bytes);

        out.flush();
        return this;
    }
}
