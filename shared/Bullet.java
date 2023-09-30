package n.e.k.o.shared;

import java.awt.*;
import java.util.UUID;

public class Bullet {

    public final int playerId; // The player that fired this bullet
    public final UUID uuid;

    public float x, y;
    public float angle, angleCos, angleSin;
    public float speed;

    public boolean sentToServer;

    public Bullet(int playerId, UUID uuid, float x, float y, float angle, float speed) {
        this.playerId = playerId;
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.angleCos = (float) Math.cos(angle);
        this.angleSin = (float) Math.sin(angle);
        this.speed = speed;
    }

    public void update(float delta) {
        x += speed * delta * angleCos;
        y += speed * delta * angleSin;
    }

    public void render(Graphics2D g2d) {
        g2d.setColor(Color.black);
        g2d.fillOval((int) (x - 3), (int) (y - 3), 6, 6);
    }

}
