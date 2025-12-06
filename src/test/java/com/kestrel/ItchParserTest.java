package com.kestrel;

import com.kestrel.core.EventPool;
import com.kestrel.parser.ItchParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class ItchParserTest {

    @Test
    public void testParserLoadsFile() {
        EventPool pool = new EventPool(1024);
        ItchParser parser = new ItchParser(pool);

        assertDoesNotThrow(() -> parser.parse("src/test/resources/sample.itch"));

    }

}
