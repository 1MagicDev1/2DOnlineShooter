package n.e.k.o.shared.packets.player;

import n.e.k.o.shared.packets.FloatPacket;
import n.e.k.o.shared.packets.IntPacket;
import n.e.k.o.shared.packets.StringPacket;
import n.e.k.o.shared.packets.internal.APacket;

import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;

public class PlayerRespawnPacket extends APacket<PlayerRespawnPacket> {

    public int id;

    public float x, y;

    @Override
    public PlayerRespawnPacket readPacket(InputStream in) throws Throwable {

        // Read player id
        this.id = new IntPacket().readPacket(in).parse().value;

        // Read player position
        this.x = new FloatPacket().readPacket(in).parse().value;
        this.y = new FloatPacket().readPacket(in).parse().value;
        return this;
    }

    @Override
    public PlayerRespawnPacket parse() throws Throwable {
        return this;
    }

    @Override
    public PlayerRespawnPacket build(Object value) throws Throwable {
        Object[] obj = (Object[]) value;
        // id - int
        // x - float
        // y - float
        this.id = (int) obj[0];
        this.x = (float) obj[1];
        this.y = (float) obj[2];
        return this;
    }

    @Override
    public PlayerRespawnPacket sendPacket(OutputStream out) throws Throwable {
        out.write(PLAYER_RESPAWN_TYPE);

        // Write player id
        out.write(new IntPacket().build(this.id).parse().bytes);

        // Write player position
        out.write(new FloatPacket().build(this.x).parse().bytes);
        out.write(new FloatPacket().build(this.y).parse().bytes);

        out.flush();
        return this;
    }

}
