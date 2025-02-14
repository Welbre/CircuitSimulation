package kuse.welbre.sim.electrical.tools;

import kuse.welbre.tools.Tools;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ToolsTest {
    @Test
    void proprietyToSi(){
        assertEquals("9.999E+15W", Tools.proprietyToSi(9999E12, "W"));
        assertEquals("10TW", Tools.proprietyToSi(9999E9, "W"));
        assertEquals("1.26GW", Tools.proprietyToSi(1.259E9, "W"));
        assertEquals("1.25MW", Tools.proprietyToSi(1254896.23, "W"));
        assertEquals("10.5MW", Tools.proprietyToSi(10.5E6, "W"));

        assertEquals("1.31kV", Tools.proprietyToSi(1313, "V"));
        assertEquals("1.31kV", Tools.proprietyToSi(1313.001, "V"));
        assertEquals("1.31kV", Tools.proprietyToSi(1313.0009, "V"));

        assertEquals("117V", Tools.proprietyToSi(117, "V"));
        assertEquals("117V", Tools.proprietyToSi(117.001, "V"));
        assertEquals("117V", Tools.proprietyToSi(117.0009, "V"));

        assertEquals("12V", Tools.proprietyToSi(12, "V"));
        assertEquals("12V", Tools.proprietyToSi(12.001, "V"));
        assertEquals("12V", Tools.proprietyToSi(12.0009, "V"));

        assertEquals("6Ω", Tools.proprietyToSi(6, "Ω"));
        assertEquals("6Ω", Tools.proprietyToSi(6.001, "Ω"));
        assertEquals("6Ω", Tools.proprietyToSi(6.0009, "Ω"));

        assertEquals("750mF", Tools.proprietyToSi(0.750, "F"));
        assertEquals("750mF", Tools.proprietyToSi(0.750001, "F"));
        assertEquals("750mF", Tools.proprietyToSi(0.7500009, "F"));

        assertEquals("75mF", Tools.proprietyToSi(0.075, "F"));
        assertEquals("75mF", Tools.proprietyToSi(0.0750001, "F"));
        assertEquals("75mF", Tools.proprietyToSi(0.0750009, "F"));

        assertEquals("9mF", Tools.proprietyToSi(0.009, "F"));
        assertEquals("9mF", Tools.proprietyToSi(0.009001, "F"));
        assertEquals("9.01mF", Tools.proprietyToSi(0.009009, "F"));
        assertEquals("6mF", Tools.proprietyToSi(6E-3, "F"));

        assertEquals("953μF", Tools.proprietyToSi(0.000953004, "F"));
        assertEquals("153.13μF", Tools.proprietyToSi(0.000153126, "F"));
        assertEquals("12.99μF", Tools.proprietyToSi(0.000012994, "F"));
        assertEquals("1.12μF", Tools.proprietyToSi(0.000001116, "F"));

        assertEquals("953.136nF", Tools.proprietyToSi(953.136e-9, "F", 3));
        assertEquals("153.138nF", Tools.proprietyToSi(153.1376e-9, "F", 3));
        assertEquals("12.991nF", Tools.proprietyToSi(12.991e-9, "F", 3));
        assertEquals("1nF", Tools.proprietyToSi(1e-9, "F", 3));
    }
}