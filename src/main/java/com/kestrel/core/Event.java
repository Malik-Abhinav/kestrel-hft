package com.kestrel.core;

public class Event {
    public long orderId;
    public long price;
    public int quantity;
    public byte side; //1=buy, 0=sell
    public byte type; //1=add, 2=cancel, 3=execute

    public void set(long orderId, long price, int quantity, byte side, byte type) {
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.side = side;
        this.type = type;
    }
}
