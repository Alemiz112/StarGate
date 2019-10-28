package alemiz.stargate.gate.packets;

/* This class helps you to create your own/custom Packets
* Your packet must extend this class so 'extends StarGatePacket'
* I recommend to look into some official packet to better understanding*/

import alemiz.stargate.gate.*;

public abstract class StarGatePacket implements Cloneable{

    /* Literally its packet Name*/
    protected String type;

    /* Every packet must have its own ID
    * ID must be unique to prevent crashes or packets rewrites
    * Official IDs are registered in @class Packets*/

    protected int ID;

    public String encoded;
    public boolean isEncoded = true;

    /* UUID is used for returning response if is needed*/
    public String uuid;

    /** We use this functions to be able work with string compression
     * encode() => Converts data to string and save it to $encoded
     * decode() => Converts from string in $encoded and saves it
     * Every packet has custom data, so you must adjust it yourself
     * Try to inspire by official packets*/

    public abstract void encode();
    public abstract void decode();

    public abstract StarGatePacket copy() throws CloneNotSupportedException;


    /* When you are creating Packet you must define in Constructor Type and ID
    * Its really simple: 'super(Type, ID)'*/

    public StarGatePacket(String type, int ID){
        this.type = type;
        this.ID = ID;
    }

    /* Put packet to server*/
    public String putPacket(String client){
        return GateAPI.putPacket(client, this);
    }

    public String getType(){
        return type;
    }

    public int getID(){
        return ID;
    }

    public String getUuid() {
        return uuid;
    }
}
