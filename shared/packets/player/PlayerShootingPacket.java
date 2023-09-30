package n.e.k.o.shared.packets.player;

import n.e.k.o.shared.Bullet;
import n.e.k.o.shared.packets.FloatPacket;
import n.e.k.o.shared.packets.IntPacket;
import n.e.k.o.shared.packets.LongPacket;
import n.e.k.o.shared.packets.internal.APacket;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class PlayerShootingPacket extends APacket<PlayerShootingPacket> {

    public int id;

    public float x, y;

    public UUID bulletUuid;
    public long mostBits, leastBits;

    public float bulletAngle, bulletSpeed;

    @Override
    public PlayerShootingPacket readPacket(InputStream in) throws Throwable {

        // Read player id
        this.id = new IntPacket().readPacket(in).parse().value;

        // Read player position
        this.x = new FloatPacket().readPacket(in).parse().value;
        this.y = new FloatPacket().readPacket(in).parse().value;

        // Read bullet UUID (unique id)
        this.mostBits = new LongPacket().readPacket(in).parse().value;
        this.leastBits = new LongPacket().readPacket(in).parse().value;
        this.bulletUuid = new UUID(this.mostBits, this.leastBits);

        // Read bullet angle and speed
        this.bulletAngle = new FloatPacket().readPacket(in).parse().value;
        this.bulletSpeed = new FloatPacket().readPacket(in).parse().value;

        return this;
    }

    @Override
    public PlayerShootingPacket parse() throws Throwable {
        return this;
    }

    @Override
    public PlayerShootingPacket build(Object value) throws Throwable {
        Object[] obj = (Object[]) value;
        // id - int
        // x - float
        // y - float
        // bulletUuid - UUID
        // angle - float
        // speed - float
        this.id = (int) obj[0];
        this.x = (float) obj[1];
        this.y = (float) obj[2];
        this.bulletUuid = (UUID) obj[3];
        this.mostBits = this.bulletUuid.getMostSignificantBits();
        this.leastBits = this.bulletUuid.getLeastSignificantBits();
        this.bulletAngle = (float) obj[4];
        this.bulletSpeed = (float) obj[5];
        return this;
    }

    @Override
    public PlayerShootingPacket sendPacket(OutputStream out) throws Throwable {
        out.write(PLAYER_SHOOTING_TYPE);

        // Write player id
        out.write(new IntPacket().build(this.id).parse().bytes);

        // Write player position
        out.write(new FloatPacket().build(this.x).parse().bytes);
        out.write(new FloatPacket().build(this.y).parse().bytes);

        // Write bullet uuid (unique id)
        out.write(new LongPacket().build(this.mostBits).parse().bytes);
        out.write(new LongPacket().build(this.leastBits).parse().bytes);

        // Write bullet angle and speed
        out.write(new FloatPacket().build(this.bulletAngle).parse().bytes);
        out.write(new FloatPacket().build(this.bulletSpeed).parse().bytes);

        out.flush();
        return this;
    }

}
