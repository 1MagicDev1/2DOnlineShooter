package n.e.k.o.client;

import n.e.k.o.shared.Bullet;
import n.e.k.o.shared.Constants;
import n.e.k.o.shared.packets.FloatPacket;
import n.e.k.o.shared.packets.IntPacket;
import n.e.k.o.shared.packets.StringPacket;
import n.e.k.o.shared.packets.internal.APacket;
import n.e.k.o.shared.packets.internal.IPacket;
import n.e.k.o.shared.packets.player.*;
import n.e.k.o.shared.players.ClientPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static n.e.k.o.shared.ScreenUtils.size;

public class Main {

    // Sets the 4 buttons, up, right, down left
    public static final boolean[] keys = new boolean[4];
    public static final Point mousePosition = new Point(0, 0);
    public static boolean mousePressed;

    public String username;
    public Color color;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        UUID.randomUUID();

        try {
            this.username = JOptionPane.showInputDialog("What's your username?", "Moojic");
            if (this.username == null || this.username.isBlank())
                return;
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }

        this.color = new Color(0, 13, 255, 255);

        JFrame frame = new JFrame();
        // Automatically exits the application when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Renders new window in GameWindow
        GameWindow gameWindow = new GameWindow(this);
        gameWindow.setPreferredSize(size);
        frame.setContentPane(gameWindow);

        // Registers the wasd or arrow keys being pressed and released
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == 82) {
                    if (ourPlayer != null && !ourPlayer.isAlive && !ourPlayer.requestSpawn) {
                        ourPlayer.requestSpawn = true;
                    }
                    return;
                }
                KeyDirection dir = KeyDirection.getDir(key);
                if (dir != null) {
                    keys[dir.id] = true;
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                KeyDirection dir = KeyDirection.getDir(key);
                if (dir != null) {
                    keys[dir.id] = false;
                }
            }
        });

        gameWindow.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition.move(e.getX(), e.getY());
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                mousePosition.move(e.getX(), e.getY());
            }
        });
        gameWindow.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mousePressed = true;
            }
        });

        // :D and packs everything in the window as referenced in the preferred size of the content pane (line20 - 21)
        frame.setResizable(false);
        frame.setTitle(":D");
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Run the doNetworking function in a new thread
        new Thread(this::doNetworking).start();

        runGameLoop(frame, gameWindow);
    }

    private void runGameLoop(JFrame frame, GameWindow gameWindow) {
        final int targetFps = 240;
        final long optimalTime = 1_000_000_000 / targetFps;

        final long targetTps = Constants.TARGET_TPS;
        final float updateTarget = 1000f / targetTps;

        long lastDeltaUpdate = System.nanoTime();

        long lastLoopTime = System.nanoTime();
        long lastFpsTime = 0;
        int fps = 0;

        // While loop for displaying fps
        while (frame.isVisible()) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;

            lastFpsTime += updateLength;
            fps++;

            if (lastFpsTime >= 1_000_000_000) {
                lastFpsTime = 0;
                System.out.println("(FPS: " + fps + ")");
                frame.setTitle(":D FPS: " + fps);
                fps = 0;
            }

            // UPDATE
            now = System.nanoTime();
            float delta = (now - lastDeltaUpdate) / 1_000_000f;
            update(delta / updateTarget);
            lastDeltaUpdate = now;

            // RENDER
            gameWindow.repaint();

            try {
                long sleepTime = (lastLoopTime - System.nanoTime() + optimalTime);
                long sleepMillis = sleepTime / 1_000_000;
                int sleepNanos = (int) (sleepTime % 1_000_000);
                long startSleepTime = System.nanoTime();

                if (sleepMillis > 0 && sleepNanos >= 0)
                    Thread.sleep(sleepMillis, sleepNanos);

                long endSleepTime = System.nanoTime();
                long sleepDiff = endSleepTime - startSleepTime - sleepTime;
                lastLoopTime += sleepDiff;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void doNetworking() {
        try {
            //InetAddress host = InetAddress.getByName("dns.obzcu.re");
            String host = "86.133.143.76";
            try (Socket socket = new Socket()) {
                socket.setTcpNoDelay(true);
                socket.connect(new InetSocketAddress(host,42069), 1000);

                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();

                // Send username
                APacket.send(out, username, StringPacket.class);

                // Send color
                APacket.send(out, color.getRGB(), IntPacket.class);

                // Read id
                int id = APacket.read(in, IntPacket.class).value;

                // Read spawn position
                float x = APacket.read(in, FloatPacket.class).value;
                float y = APacket.read(in, FloatPacket.class).value;

                // Create player instance
                ourPlayer = new ClientPlayer(id, x, y, color);
                ourPlayer.username = username;

                isConnected = true;

                // Start writing packets
                new Thread(() -> startWriting(out)).start();

                // Start reading packets
                while (isConnected) {
                    var packet = APacket.read(in);
                    switch (packet.type) {
                        case IPacket.PLAYER_SPAWN_TYPE: {
                            var spawn = (PlayerSpawnPacket) packet;
                            if (spawn.id == ourPlayer.id)
                                isInGame = true;
                            else {
                                var player = new ClientPlayer(spawn.id, spawn.x, spawn.y, spawn.color);
                                player.username = spawn.username;
                                player.move(spawn.x, spawn.y);
                                otherPlayers.put(spawn.id, player);
                            }
                        } break;
                        case IPacket.PLAYER_DISCONNECT_TYPE: {
                            var disconnect = (PlayerDisconnectPacket) packet;
                            otherPlayers.remove(disconnect.id);
                        } break;
                        case IPacket.PLAYER_MOVEMENT_TYPE: {
                            var move = (PlayerMovementPacket) packet;
                            if (otherPlayers.containsKey(move.id)) {
                                var otherPlayer = otherPlayers.get(move.id);
                                otherPlayer.move(move.x, move.y);
                            }
                        } break;
                        case IPacket.PLAYER_SHOOTING_TYPE: {
                            var shoot = (PlayerShootingPacket) packet;
                            if (otherPlayers.containsKey(shoot.id)) {
                                var bullet = new Bullet(
                                        shoot.id, shoot.bulletUuid, shoot.x, shoot.y,
                                        shoot.bulletAngle, shoot.bulletSpeed
                                );
                                bullet.sentToServer = true;
                                bullets.put(shoot.bulletUuid, bullet);
                            }
                        } break;
                        case IPacket.PLAYER_HIT_TYPE: {
                            var hit = (PlayerHitPacket) packet;
                            if (hit.targetId == ourPlayer.id) {
                                ourPlayer.health = hit.newHealth;
                                ourPlayer.isAlive = ourPlayer.health > 0;
                                if (!ourPlayer.isAlive) {
                                    if (otherPlayers.containsKey(hit.id)) {
                                        var otherPlayer = otherPlayers.get(hit.id);
                                        killFeed.add(otherPlayer.username + " Killed " + ourPlayer.username + "!");
                                    }
                                    else if (otherPlayers.containsKey(hit.targetId)) {
                                        var otherPlayer = otherPlayers.get(hit.targetId);
                                        killFeed.add(otherPlayer.username + " Killed " + ourPlayer.username + "!");
                                    } else {
                                        killFeed.add("An Enemy Killed " + ourPlayer.username + "!");
                                    }
                                    if (killFeed.size() > 10)
                                        killFeed.remove(0);
                                }
                            }
                            else if (otherPlayers.containsKey(hit.targetId)) {
                                var player = otherPlayers.get(hit.targetId);
                                player.health = hit.newHealth;
                                player.isAlive = player.health > 0;
                                if (!player.isAlive) {
                                    if (hit.id == ourPlayer.id) {
                                        killFeed.add(ourPlayer.username + " Killed " + player.username + "!");
                                    } else if (otherPlayers.containsKey(hit.id)) {
                                        var otherPlayer = otherPlayers.get(hit.id);
                                        killFeed.add(otherPlayer.username + " Killed " + player.username + "!");
                                    } else {
                                        killFeed.add("An Enemy Killed " + player.username + "!");
                                    }
                                    if (killFeed.size() > 10)
                                        killFeed.remove(0);
                                }
                            }
                            bullets.remove(hit.bulletUuid);
                        } break;
                        case IPacket.PLAYER_RESPAWN_TYPE: {
                            var respawn = (PlayerRespawnPacket) packet;
                            if (respawn.id == ourPlayer.id) {
                                ourPlayer.x = respawn.x;
                                ourPlayer.y = respawn.y;
                                ourPlayer.move(respawn.x, respawn.y);
                                ourPlayer.health = ourPlayer.maxHealth;
                                ourPlayer.isAlive = true;
                                ourPlayer.requestSpawn = false;
                                ourPlayer.hasRequestedSpawn = false;
                            }
                            else if (otherPlayers.containsKey(respawn.id)) {
                                var player = otherPlayers.get(respawn.id);
                                player.x = respawn.x;
                                player.y = respawn.y;
                                player.move(respawn.x, respawn.y);
                                player.health = player.maxHealth;
                                player.isAlive = true;
                            }
                        } break;
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            failedConnecting = true;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            isConnected = false;
            isInGame = false;
        }
    }

    private void startWriting(OutputStream out) {
        try {
            while (isConnected) {
                APacket.send(out, new Object[] { ourPlayer.id, ourPlayer.x, ourPlayer.y }, PlayerMovementPacket.class);

                for (Bullet bullet : bullets.values()) {
                    if (!bullet.sentToServer) {
                        bullet.sentToServer = true;
                        APacket.send(out, new Object[] {
                                ourPlayer.id, bullet.x, bullet.y,
                                bullet.uuid, bullet.angle, bullet.speed
                        }, PlayerShootingPacket.class);
                    }
                }

                if (ourPlayer.requestSpawn && !ourPlayer.hasRequestedSpawn) {
                    APacket.send(out, new Object[] { ourPlayer.id, 0f, 0f }, PlayerRespawnPacket.class);
                    ourPlayer.hasRequestedSpawn = true;
                }

                Thread.sleep(1000 / 240);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            failedConnecting = true;
        } finally {
            isConnected = false;
            isInGame = false;
        }
    }

    ClientPlayer ourPlayer;

    boolean isConnected, failedConnecting, isInGame;

    ConcurrentMap<Integer, ClientPlayer> otherPlayers = new ConcurrentHashMap<>();
    ConcurrentMap<UUID, Bullet> bullets = new ConcurrentHashMap<>();

    private void update(float delta) {
        if (!isInGame) return;

        if (ourPlayer.isAlive) {
            float speed = 2.5f * delta;
            if (keys[KeyDirection.UP.id]) {
                ourPlayer.y -= speed;
                if (ourPlayer.y < 0f)
                    ourPlayer.y = 0f;
            }
            if (keys[KeyDirection.DOWN.id]) {
                ourPlayer.y += speed;
                if (ourPlayer.y > size.height - 32f)
                    ourPlayer.y = size.height - 32f;
            }
            if (keys[KeyDirection.LEFT.id]) {
                ourPlayer.x -= speed;
                if (ourPlayer.x < 0f)
                    ourPlayer.x = 0f;
            }
            if (keys[KeyDirection.RIGHT.id]) {
                ourPlayer.x += speed;
                if (ourPlayer.x > size.width - 32f)
                    ourPlayer.x = size.width - 32f;
            }
        }

        // Check if we want to shoot a bullet
        if (mousePressed) {
            mousePressed = false;

            if (ourPlayer.isAlive) {
                // Angle from out player to our mouse
                float dx = mousePosition.x - ourPlayer.x - 16;
                float dy = mousePosition.y - ourPlayer.y - 16;
                float angle = (float) Math.atan2(dy, dx);

                Bullet bullet = new Bullet(ourPlayer.id, UUID.randomUUID(), ourPlayer.x + 16, ourPlayer.y + 16, angle, 15f);
                bullets.put(bullet.uuid, bullet);
            }
        }

        // Update all bullets
        for (Bullet bullet : bullets.values())
            bullet.update(delta);

        // Update other players
        for (ClientPlayer p : otherPlayers.values())
            p.update(delta);

        // Check bullet collision
        for (Bullet bullet : bullets.values()) {
            float bX = bullet.x;
            float bY = bullet.y;
            ClientPlayer didHit = null;
            for (ClientPlayer p : otherPlayers.values())
                if (p.isAlive && p.isHit(bX, bY)) {
                    didHit = p;
                    break;
                }
            if (didHit != null || bX < 0 || bY < 0 || bX > size.width || bY > size.height) {
                if (didHit != null && didHit.id == bullet.playerId) continue;
                bullets.remove(bullet.uuid);
            }
        }
    }

    private final Font deadFont = new JLabel().getFont().deriveFont(Font.BOLD, 69f);

    public static java.util.List<String> killFeed = new CopyOnWriteArrayList<>();

    public void render(Graphics2D g2d) {

        // Clear canvas
        g2d.setColor(Color.lightGray);
        g2d.fillRect(0,0, size.width, size.height);

        // Checking if we are connected
        if (!isConnected) {
            g2d.setColor(Color.black);
            if (failedConnecting)
                g2d.drawString("Connection failed -_-", 10, 25);
            else
                g2d.drawString("Connecting, please wait...", 10, 25);
            return;
        }

        // Checking if we are in game yet (has spawned)
        if (!isInGame) {
            g2d.setColor(Color.black);
            g2d.drawString("Spawning, please wait...", 10, 25);
            return;
        }

        // If we are dead, show respawn screen
        if (!ourPlayer.isAlive) {
            var font = g2d.getFont();
            g2d.setFont(deadFont);
            g2d.setColor(new Color(175, 15, 15));
            var metrics = g2d.getFontMetrics(deadFont);

            var line1 = "You are dead!";
            var bounds = metrics.getStringBounds(line1, g2d);
            g2d.drawString(line1, (int) (size.width / 2f - bounds.getWidth() / 2f),
                                (int) (size.height / 2f - bounds.getHeight() / 2f - (bounds.getHeight() / 2f)));

            var line2 = "Press R to respawn.";
            bounds = metrics.getStringBounds(line2, g2d);
            g2d.drawString(line2, (int) (size.width / 2f - bounds.getWidth() / 2f),
                    (int) (size.height / 2f - bounds.getHeight() / 2f + (bounds.getHeight() / 2f)));

            g2d.setFont(font);
            return;
        }

        // Render other players
        for (ClientPlayer p : otherPlayers.values())
            p.render(g2d);

        // Render our own player
        ourPlayer.render(g2d);

        // Render bullets
        for (Bullet bullet : bullets.values())
            bullet.render(g2d);

        // Draw mouse box
        g2d.setColor(Color.black);
        g2d.drawRect(mousePosition.x - 5, mousePosition.y - 5, 10, 10);

        // Draw kill-feed text
        if (!killFeed.isEmpty()) {
            g2d.setColor(new Color(0, 0, 0, 100));
            var metrics = g2d.getFontMetrics();
            int longest = 0, strHeight = 0;
            for (String str : killFeed) {
                var bounds = metrics.getStringBounds(str, g2d);
                if (bounds.getHeight() > strHeight)
                    strHeight = (int) Math.ceil(bounds.getHeight());
                if (bounds.getWidth() > longest)
                    longest = (int) Math.ceil(bounds.getWidth());
            }
            g2d.fillRect(size.width - 20 - longest, 10, longest + 10,
                    (int) ((strHeight + 4.5f) * killFeed.size() + 10)
            );
            g2d.setColor(Color.white);
            int strY = 25;
            for (String str : killFeed) {
                g2d.drawString(str, size.width - longest - 15, strY);
                strY += strHeight + 5;
            }
        }
    }

}