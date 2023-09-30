package n.e.k.o.shared.packets.player;

import n.e.k.o.shared.packets.FloatPacket;
import n.e.k.o.shared.packets.IntPacket;
import n.e.k.o.shared.packets.StringPacket;
import n.e.k.o.shared.packets.internal.APacket;

import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;

public class PlayerDisconnectPacket extends APacket<PlayerDisconnectPacket> {

    public int id;

    @Override
    public PlayerDisconnectPacket readPacket(InputStream in) throws Throwable {
        // Read player id
        this.id = new IntPacket().readPacket(in).parse().value;
        return this;
    }

    @Override
    public PlayerDisconnectPacket parse() throws Throwable {
        return this;
    }

    @Override
    public PlayerDisconnectPacket build(Object value) {
        this.id = (int) value;
        return this;
    }

    @Override
    public PlayerDisconnectPacket sendPacket(OutputStream out) throws Throwable {
        out.write(PLAYER_DISCONNECT_TYPE);

        // Write player id
        out.write(new IntPacket().build(this.id).parse().bytes);

        out.flush();
        return this;
    }

}