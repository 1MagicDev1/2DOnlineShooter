package n.e.k.o.shared.players;

import n.e.k.o.server.Server;
import n.e.k.o.shared.Bullet;

import n.e.k.o.shared.ScreenUtils;
import n.e.k.o.shared.packets.FloatPacket;
import n.e.k.o.shared.packets.IntPacket;
import n.e.k.o.shared.packets.StringPacket;
import n.e.k.o.shared.packets.internal.APacket;
import n.e.k.o.shared.packets.internal.IPacket;
import n.e.k.o.shared.packets.player.PlayerMovementPacket;
import n.e.k.o.shared.packets.player.PlayerRespawnPacket;
import n.e.k.o.shared.packets.player.PlayerShootingPacket;
import n.e.k.o.shared.packets.player.PlayerSpawnPacket;

import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerPlayer extends APlayer {

    private final Socket socket;
    private final OutputStream out;
    private final InputStream in;

    public int color;
    public boolean isConnected;

    private final CopyOnWriteArrayList<APacket> packetsToSend = new CopyOnWriteArrayList<>();

    private final Random random = new Random();

    public ServerPlayer(Socket sock) {
        super(Server.playerId.getAndIncrement());
        try {
            this.socket = sock;
            this.in = sock.getInputStream();
            this.out = sock.getOutputStream();

            this.x = random.nextInt(ScreenUtils.size.width - 64) + 32;
            this.y = random.nextInt(ScreenUtils.size.height - 64) + 32;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void move(float x, float y) {
        super.move(x, y);
        this.x = x;
        this.y = y;
    }

    @Override
    public void update(float delta) {
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

    @Override
    public void render(Graphics2D g2d) {

    }

    @Override
    public void doNetworking() throws Throwable {
        isConnected = true;

        // read username
        this.username = APacket.read(in, StringPacket.class).value;

        // read color
        this.color = APacket.read(in, IntPacket.class).value;

        // write id
        APacket.send(out, id, IntPacket.class);

        // write spawn position
        APacket.send(out, x, FloatPacket.class);
        APacket.send(out, y, FloatPacket.class);

        // send existing player's data
        sendExistingPlayers();

        // start write thread
        new Thread(this::startWriting).start();

        // send spawn packet to everyone else
        Server.sendToAll(new PlayerSpawnPacket().build(new Object[] {
                id, username, new Color(color, true), x, y
        }), null);

        // start read loop
        while (isConnected) {
            var packet = APacket.read(in);
            // System.out.println("[SERVER] Received packet: " + packet);

            switch (packet.type) {
                case IPacket.PLAYER_MOVEMENT_TYPE: {
                    var move = (PlayerMovementPacket) packet;
                    if (Server.players.containsKey(move.id)) {
                        Server.sendToAll(packet, this);
                        var player = Server.players.get(move.id);
                        player.move(move.x, move.y);
                    }
                } break;
                case IPacket.PLAYER_SHOOTING_TYPE: {
                    var shoot = (PlayerShootingPacket) packet;
                    if (Server.players.containsKey(shoot.id)) {
                        Server.sendToAll(packet, this);
                        Server.bullets.put(shoot.bulletUuid, new Bullet(
                                id,
                                shoot.bulletUuid, shoot.x, shoot.y,
                                shoot.bulletAngle, shoot.bulletSpeed
                        ));
                    }
                } break;
                case IPacket.PLAYER_RESPAWN_TYPE: {
                    var respawn = (PlayerRespawnPacket) packet;
                    if (Server.players.containsKey(respawn.id)) {
                        this.health = maxHealth;
                        this.isAlive = true;
                        respawn.x = random.nextInt(ScreenUtils.size.width - 64) + 32;
                        respawn.y = random.nextInt(ScreenUtils.size.height - 64) + 32;
                        Server.sendToAll(packet, null);
                    }
                } break;
            }
        }
    }

    private void sendExistingPlayers() throws Throwable {
        for (ServerPlayer p : Server.players.values()) {
            APacket.send(out, new Object[] {
                    p.id, p.username, new Color(p.color, true),
                    p.x, p.y
            }, PlayerSpawnPacket.class);
        }
    }

    private void startWriting() {
        try {
            while (isConnected) {
                while (!packetsToSend.isEmpty()) {
                    APacket packet = packetsToSend.remove(0);
                    packet.sendPacket(out);
                }
                Thread.sleep(1000 / 240);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            isConnected = false;
        }
    }

    public void sendPacket(APacket packet) {
        packetsToSend.add(packet);
    }

}
