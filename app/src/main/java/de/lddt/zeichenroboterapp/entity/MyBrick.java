package de.lddt.zeichenroboterapp.entity;

/**
 * Created by Tim on 11.05.2016.
 */
public class MyBrick {
    private String name;
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
