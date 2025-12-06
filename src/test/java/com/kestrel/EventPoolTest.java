package com.kestrel;
import com.kestrel.core.EventPool;
import com.kestrel.core.Event;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EventPoolTest {
    
    @Test
    public void testEventReuse(){
        EventPool pool = new EventPool(2);

        Event e1 = pool.get();
        e1.set(1, 100, 10, (byte)1, (byte)1);
        
        Event e2 = pool.get();
        e2.set(2, 200, 20, (byte)0, (byte)1);

        Event e3 = pool.get(); // should wrap around

        assertSame(e1, e3); // reused object

    }


}
