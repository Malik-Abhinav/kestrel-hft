package com.kestrel.core;

public class EventPool {
    private final Event[] pool;
    private int index;
    private final int size;

    public EventPool(int size){
        this.size = size;
        this.pool = new Event[size];

        for (int i = 0; i<size; i++){
            pool[i] = new Event();
        }
    }

    public Event get(){
        if (index == size){
            index = 0;      // wrap around
        }
        return pool[index++];
    }

}
