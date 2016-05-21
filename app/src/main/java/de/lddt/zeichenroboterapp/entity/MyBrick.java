package de.lddt.zeichenroboterapp.entity;

/**
 * Entity of a nxt brick
 */
public class MyBrick {
    //name of the brick
    private String name;
    //unique bluetooth mac address of the brick
    private String macAddress;

    public MyBrick(String brick, String macAddress) {
        this.name = brick;
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }
}
