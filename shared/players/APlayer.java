package n.e.k.o.shared.players;

import java.awt.*;

public abstract class APlayer {

    public final int id;

    public String username;
    public float x, y, x2, y2;
    public Color color;

    public short health = 100, maxHealth = 100;
    public boolean isAlive = true;

    public APlayer(int id, float x, float y, Color color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public APlayer(int id) {
        this.id = id;
    }

    public void move(float x, float y) {
        this.x2 = x;
        this.y2 = y;
    }

    public abstract void update(float delta);

    public abstract void render(Graphics2D g2d);

    public boolean isHit(float x, float y) {
        int offset = 2;
        return (x >= this.x2 + offset && x <= this.x2 - offset + 32f) &&
               (y >= this.y2 + offset && y <= this.y2 - offset + 32f);
    }
    public void doNetworking() throws Throwable {

    }

}
