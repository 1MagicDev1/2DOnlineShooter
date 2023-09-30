package n.e.k.o.shared.packets.player;

import n.e.k.o.shared.packets.FloatPacket;
import n.e.k.o.shared.packets.IntPacket;
import n.e.k.o.shared.packets.StringPacket;
import n.e.k.o.shared.packets.internal.APacket;

import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;

public class PlayerSpawnPacket extends APacket<PlayerSpawnPacket> {

    public int id;

    public String username;

    public Color color;

    public float x, y;

    @Override
    public PlayerSpawnPacket readPacket(InputStream in) throws Throwable {

        // Read player id
        this.id = new IntPacket().readPacket(in).parse().value;

        // Read username
        this.username = new StringPacket().readPacket(in).parse().value;

        // Read player color
        int rgba = new IntPacket().readPacket(in).parse().value;
        this.color = new Color(rgba, true);

        // Read player position
        this.x = new FloatPacket().readPacket(in).parse().value;
        this.y = new FloatPacket().readPacket(in).parse().value;

        return this;
    }

    @Override
    public PlayerSpawnPacket parse() throws Throwable {
        return this;
    }

    @Override
    public PlayerSpawnPacket build(Object value) throws Throwable {
        Object[] obj = (Object[]) value;
        // id - int
        // username - string
        // color - Color
        // x - float
        // y - float
        this.id = (int) obj[0];
        this.username = (String) obj[1];
        this.color = (Color) obj[2];
        this.x = (float) obj[3];
        this.y = (float) obj[4];
        return this;
    }

    @Override
    public PlayerSpawnPacket sendPacket(OutputStream out) throws Throwable {
        out.write(PLAYER_SPAWN_TYPE);

        // Write player id
        out.write(new IntPacket().build(this.id).parse().bytes);

        // Write username
        out.write(new StringPacket().build(this.username).parse().bytes);

        // Write player color
        out.write(new IntPacket().build(this.color.getRGB()).parse().bytes);

        // Write player position
        out.write(new FloatPacket().build(this.x).parse().bytes);
        out.write(new FloatPacket().build(this.y).parse().bytes);

        out.flush();
        return this;
    }

}
