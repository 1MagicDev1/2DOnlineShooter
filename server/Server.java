package n.e.k.o.server;

import n.e.k.o.shared.Bullet;
import n.e.k.o.shared.Constants;
import n.e.k.o.shared.packets.internal.APacket;
import n.e.k.o.shared.packets.player.PlayerDisconnectPacket;
import n.e.k.o.shared.packets.player.PlayerHitPacket;
import n.e.k.o.shared.players.ServerPlayer;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static n.e.k.o.shared.ScreenUtils.size;

public class Server {

    public static AtomicInteger playerId = new AtomicInteger(0);

    public static ConcurrentMap<Integer, ServerPlayer> players = new ConcurrentHashMap<>();

    public static ConcurrentMap<UUID, Bullet> bullets = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            new Thread(Server::runGameLoop).start();
            try (ServerSocket server = new ServerSocket()) {
                server.setReuseAddress(true);
                server.bind(new InetSocketAddress(42069));
                System.out.println("[server] Listening to port " + server.getLocalPort() + "!");
                Socket socket;
                while ((socket = server.accept()) != null) {
                    final Socket cock = socket;
                    new Thread(() -> {
                        System.out.println("[Server] Connection accepted from " + cock.getInetAddress().getHostAddress());
                        ServerPlayer player = null;
                        try {
                            cock.setTcpNoDelay(true);
                            player = new ServerPlayer(cock);
                            players.put(player.id, player);
                            player.doNetworking();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        } finally {
                            if (player != null) {
                                player.isConnected = false;
                                players.remove(player.id);
                                sendToAll(new PlayerDisconnectPacket().build(player.id), null);
                            }
                        }
                    }).start();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void runGameLoop() {
        final long targetTps = Constants.TARGET_TPS;
        final float updateTarget = 1000f / targetTps;
        final long optimalTime = 1_000_000_000 / targetTps;

        long lastDeltaUpdate = System.nanoTime();
        long lastLoopTime = System.nanoTime();

        long lastTpsTime = 0;
        int tps = 0;

        while (true) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;

            lastTpsTime += updateLength;
            tps++;

            if (lastTpsTime >= 1_000_000_000) {
                lastTpsTime = 0;
                System.out.println("(TPS: " + tps + ")");
                tps = 0;
            }

            // UPDATE
            now = System.nanoTime();
            float delta = (now - lastDeltaUpdate) / 1_000_000f;
            update(delta / updateTarget);
            lastDeltaUpdate = now;

            try {
                long sleepTime = (lastLoopTime - System.nanoTime() + optimalTime);
                long sleepMillis = sleepTime / 1_000_000;
                int sleepNanos = (int) (sleepTime % 1_000_000);
                long startSleepTime = System.nanoTime();

                Thread.sleep(sleepMillis, sleepNanos);

                long endSleepTime = System.nanoTime();
                long sleepDiff = endSleepTime - startSleepTime - sleepTime;
                lastLoopTime += sleepDiff;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void update(float delta) {

        // Update all bullets
        for (Bullet bullet : bullets.values()) {
            bullet.update(delta);
        }

        // Update other players
        for (ServerPlayer p : players.values())
            p.update(delta);

        // Check bullet collision
        for (Bullet bullet : bullets.values()) {
            float bX = bullet.x;
            float bY = bullet.y;
            ServerPlayer didHit = null;
            for (ServerPlayer p : players.values())
                if (p.isAlive && p.isHit(bX, bY) && p.id != bullet.playerId) {
                    didHit = p;
                    break;
                }
            if (didHit != null || bX < 0 || bY < 0 || bX > size.width || bY > size.height) {
                bullets.remove(bullet.uuid);
                if (didHit != null) {
                    // 1. Lower player's health
                    didHit.health -= 25;
                    didHit.isAlive = didHit.health > 0;

                    // 2. Send hit packet to all players
                    sendToAll(new PlayerHitPacket().build(new Object[] {
                            bullet.playerId, bullet.uuid,
                            didHit.id, didHit.health
                    }), null);
                }
            }
        }

    }

    public static void sendToAll(APacket packet, ServerPlayer ignore) {
        for (ServerPlayer player : players.values()) {
            if (player == ignore) continue;
            player.sendPacket(packet);
        }
    }

}
