package it.polimi.ingsw.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void getModularAt() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        assertEquals(0, Util.getModularAt(list, 0));
        assertEquals(3, Util.getModularAt(list, 3));
        assertEquals(3, Util.getModularAt(list, -7));
        assertEquals(3, Util.getModularAt(list, -17));
        assertEquals(4, Util.getModularAt(list, 74));
        assertEquals(9, Util.getModularAt(list, -1));
        assertEquals(9, Util.getModularAt(list, 9));
        assertEquals(0, Util.getModularAt(list, 10));
    }
}