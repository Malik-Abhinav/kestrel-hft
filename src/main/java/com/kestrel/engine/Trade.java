package com.kestrel.engine;

public class Trade {
    public long takerOrderId;
    public long makerOrderId;
    public long price;
    public int quantity;

    public Trade(long takerOrderId, long makerOrderId, long price, int quantity) {
        this.takerOrderId = takerOrderId;
        this.makerOrderId = makerOrderId;
        this.price = price;
        this.quantity = quantity;
    }
}
