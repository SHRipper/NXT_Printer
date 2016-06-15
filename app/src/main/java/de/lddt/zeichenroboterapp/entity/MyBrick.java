package de.lddt.zeichenroboterapp.entity;

import android.content.Context;

import de.lddt.zeichenroboterapp.R;

/**
 * Entity of a nxt brick
 */
public class MyBrick {
    //name of the brick
    private final String name;
    //unique bluetooth mac address of the brick
    private final String macAddress;

    public MyBrick(String name, String macAddress) {
        this.name = name;
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }

    /**
     * Creates a MyBrick instance with name and mac address specified in the resource file
     *
     * @param context needed to access the config xml file
     * @return the created MyyBrick Object
     */
    public static MyBrick getDefaultBrick(Context context) {
        return new MyBrick(context.getString(R.string.brick_name),
                context.getString(R.string.brick_mac_address));
    }
}
