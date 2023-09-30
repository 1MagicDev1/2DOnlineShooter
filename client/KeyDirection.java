package n.e.k.o.client;

public enum KeyDirection {

    UP(0),
    RIGHT(1),
    DOWN(2),
    LEFT(3);

    final int id;

    KeyDirection(int id) {
        this.id = id;
    }

    public static KeyDirection getDir(int key) {
        if (key == 38 || key == 87) return UP;
        if (key == 39 || key == 68) return RIGHT;
        if (key == 40 || key == 83) return DOWN;
        if (key == 37 || key == 65) return LEFT;
        return null;
    }

}
