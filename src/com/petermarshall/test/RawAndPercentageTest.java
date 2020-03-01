package com.petermarshall.test;

import com.petermarshall.main.RawAndPecentage;
import org.junit.Before;
import org.junit.Test;

import static com.petermarshall.main.FinchLimits.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//TODO: need to add more boundary test cases.
//not testing for boundaries other than MIN and MAX vals of Finch as these will never change.
public class RawAndPercentageTest {
    private RawAndPecentage lightStats;
    private RawAndPecentage wheelStats;
    private int rawLight = 51;
    private int rawWheel = -51;

    @Before
    public void setup() {
        lightStats = new RawAndPecentage(rawLight, MAX_LIGHT_INTENSITY, MIN_LIGHT_INTENSITY);
        wheelStats = new RawAndPecentage(rawWheel, MAX_WHEEL_VEL, MIN_WHEEL_VEL);
    }

    @Test
    public void protectsAgainstMaxMinWrongWayRound() {
        RawAndPecentage wrongWay = new RawAndPecentage(1,0,3);
        assertEquals(33, wrongWay.getPercentage());
    }

    @Test
    public void canGetRawVal() {
        assertEquals(rawLight, lightStats.getRaw());
        assertEquals(rawWheel, wheelStats.getRaw());
    }

    @Test
    public void canGetPercVal() {
        int expectedLightPerc = 100*rawLight/MAX_LIGHT_INTENSITY;
        assertEquals(expectedLightPerc, lightStats.getPercentage());
        int expectedWheelPerc = 100*(rawWheel-MIN_WHEEL_VEL) / (MAX_WHEEL_VEL-MIN_WHEEL_VEL); //making the raw and max on a scale from 0 - max
        assertEquals(expectedWheelPerc, wheelStats.getPercentage());
    }

    @Test
    public void canGetPosNegPercVal() {
        int expectedLightPerc = 100*rawLight/MAX_LIGHT_INTENSITY;
        assertEquals(expectedLightPerc, lightStats.getPosNegPercentage());
        int expectedWheelPerc = rawWheel >= 0 ? 100*rawWheel/MAX_WHEEL_VEL : -100*rawWheel/MIN_WHEEL_VEL;
        assertEquals(expectedWheelPerc, wheelStats.getPosNegPercentage());
        assertTrue(wheelStats.getPosNegPercentage() < 0);
    }

    @Test
    public void roundsOutOfRangeValsToMinMax() {
        RawAndPecentage tooLarge = new RawAndPecentage(600, 255, 0);
        assertEquals(255, tooLarge.getRaw());
        assertEquals(100, tooLarge.getPercentage());
        assertEquals(100, tooLarge.getPosNegPercentage());
        RawAndPecentage tooSmall = new RawAndPecentage(-30, 255, -10);
        assertEquals(-10, tooSmall.getRaw());
        assertEquals(0, tooSmall.getPercentage());
        assertEquals(-100, tooSmall.getPosNegPercentage());
    }
}
