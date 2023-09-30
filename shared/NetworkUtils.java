package n.e.k.o.shared;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class NetworkUtils {

    public final static int DISCONNECT_TYPE = -1;
    public final static int SPAWN_TYPE = 0;
    public final static int MOVE_TYPE = 1;
    public final static int SHOOT_TYPE = 2;
    public final static int HIT_TYPE = 3;

    public static byte[] ensureRead(int len, InputStream in) throws Throwable {
        byte[] out = new byte[len];
        int read = 0;
        while (read < len) {
            int r = in.read(out, read, out.length - read);
            if (r == -1) throw new Throwable("Disconnected?");
            read += r;
        }
        return out;
    }

    public static float readFloat(InputStream in) throws Throwable {
        byte[] bytes = ensureRead(4, in);
        int i = ((bytes[3] & 0xFF))       |
                ((bytes[2] & 0xFF) << 8)  |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[0] & 0xFF) << 24);
        return Float.intBitsToFloat(i);
    }

    public static void writeFloat(float f, OutputStream out) throws Throwable {
        int i = Float.floatToIntBits(f);
        byte[] bytes = {
                (byte) ((i >> 24) & 0xFF),  // 23 <-> 31
                (byte) ((i >> 16) & 0xFF),  // 16 <-> 23
                (byte) ((i >> 8) & 0xFF),   // 8 <-> 15
                (byte) (i & 0xFF),          // 0 <-> 7
        };
        out.write(bytes);
        out.flush();
    }

    public static int readInt(InputStream in) throws Throwable {
        byte[] bytes = ensureRead(4, in);
        return  ((bytes[3] & 0xFF))       |
                ((bytes[2] & 0xFF) << 8)  |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[0] & 0xFF) << 24);
    }

    public static void writeInt(int i, OutputStream out) throws Throwable {
        byte[] bytes = {
                (byte) ((i >> 24) & 0xFF),
                (byte) ((i >> 16) & 0xFF),
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF),
        };
        out.write(bytes);
        out.flush();
    }

    public static long readLong(InputStream in) throws Throwable {
        byte[] bytes = ensureRead(8, in);
        return  ((long)(bytes[7] & 0xFF))       |
                ((long)(bytes[6] & 0xFF) << 8)  |
                ((long)(bytes[5] & 0xFF) << 16) |
                ((long)(bytes[4] & 0xFF) << 24) |
                ((long)(bytes[3] & 0xFF) << 32) |
                ((long)(bytes[2] & 0xFF) << 40) |
                ((long)(bytes[1] & 0xFF) << 48) |
                ((long)(bytes[0] & 0xFF) << 56);
    }

    public static void writeLong(long l, OutputStream out) throws Throwable {
        byte[] bytes = {
                (byte) ((l >> 56) & 0xFF),
                (byte) ((l >> 48) & 0xFF),
                (byte) ((l >> 40) & 0xFF),
                (byte) ((l >> 32) & 0xFF),
                (byte) ((l >> 24) & 0xFF),
                (byte) ((l >> 16) & 0xFF),
                (byte) ((l >> 8) & 0xFF),
                (byte) (l & 0xFF),
        };
        out.write(bytes);
        out.flush();
    }

    public static String readString(InputStream in) throws Throwable {
        int len = readInt(in);
        byte[] bytes = ensureRead(len, in);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(String str, OutputStream out) throws Throwable {
        writeInt(str.length(), out);
        out.write(str.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

}
