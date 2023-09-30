package n.e.k.o.shared.players;

import java.awt.*;

public class ClientPlayer extends APlayer {

    public boolean requestSpawn, hasRequestedSpawn;

    public ClientPlayer(int id, float x, float y, Color color) {
        super(id, x, y, color);
    }

    public void update(float delta) {
        if (!isAlive) return;

        float speed = 2.5f * delta;

        float dx = x2 - x;
        float dy = y2 - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > speed) {
            float angle = (float) Math.atan2(dy, dx);
            x += speed * Math.cos(angle);
            y += speed * Math.sin(angle);
        } else {
            x = x2;
            y = y2;
        }
    }

    public void render(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillRect((int) x, (int) y, 32, 32);

        g2d.setColor(Color.black);
        int above = 10, height = 12, width = 48;
        int x = (int) (this.x + 16 - (width / 2));
        g2d.fillRect(x, (int) (this.y - height - above), width, height);

        g2d.setColor(new Color(175, 15, 15));
        g2d.fillRect(x + 1, (int) (this.y - height - above) + 1, width - 2, height - 2);

        g2d.setColor(new Color(38, 150, 38));
        float percentage = (health * 100f) / maxHealth / 100f;
        int health = (int) (width * percentage) - 2;
        if (health < 0) health = 0;
        g2d.fillRect(x + 1, (int) (this.y - height - above) + 1, health, height - 2);
    }

}
