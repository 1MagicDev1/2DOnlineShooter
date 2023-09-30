package n.e.k.o.shared.packets.player;

import n.e.k.o.shared.packets.IntPacket;
import n.e.k.o.shared.packets.LongPacket;
import n.e.k.o.shared.packets.ShortPacket;
import n.e.k.o.shared.packets.internal.APacket;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class PlayerHitPacket extends APacket<PlayerHitPacket> {

    public int id; // Player that shot the bullet

    public UUID bulletUuid; // The uuid of the bullet
    public long mostBits, leastBits;

    public int targetId; // The player that was hit
    public short newHealth;

    @Override
    public PlayerHitPacket readPacket(InputStream in) throws Throwable {

        // Read player id
        this.id = new IntPacket().readPacket(in).parse().value;

        // Read bullet UUID (unique id)
        this.mostBits = new LongPacket().readPacket(in).parse().value;
        this.leastBits = new LongPacket().readPacket(in).parse().value;
        this.bulletUuid = new UUID(this.mostBits, this.leastBits);

        // Read target-id and new-target-health
        this.targetId = new IntPacket().readPacket(in).parse().value;
        this.newHealth = new ShortPacket().readPacket(in).parse().value;

        return this;
    }

    @Override
    public PlayerHitPacket parse() throws Throwable {
        return this;
    }

    @Override
    public PlayerHitPacket build(Object value) {
        Object[] obj = (Object[]) value;
        // id - int
        // bulletUuid - UUID
        // targetId - int
        // newTargetHealth - short
        this.id = (int) obj[0];
        this.bulletUuid = (UUID) obj[1];
        this.mostBits = this.bulletUuid.getMostSignificantBits();
        this.leastBits = this.bulletUuid.getLeastSignificantBits();
        this.targetId = (int) obj[2];
        this.newHealth = (short) obj[3];
        return this;
    }

    @Override
    public PlayerHitPacket sendPacket(OutputStream out) throws Throwable {
        out.write(PLAYER_HIT_TYPE);

        // Write player id
        out.write(new IntPacket().build(this.id).parse().bytes);

        // Write bullet uuid (unique id)
        out.write(new LongPacket().build(this.mostBits).parse().bytes);
        out.write(new LongPacket().build(this.leastBits).parse().bytes);

        // Write target-id and new-target-health
        out.write(new IntPacket().build(this.targetId).parse().bytes);
        out.write(new ShortPacket().build(this.newHealth).parse().bytes);

        out.flush();
        return this;
    }

}
