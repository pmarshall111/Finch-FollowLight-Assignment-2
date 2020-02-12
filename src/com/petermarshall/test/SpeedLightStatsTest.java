package com.petermarshall.test;

import com.petermarshall.main.FinchState;
import com.petermarshall.main.SpeedLightStats;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpeedLightStatsTest {
    private SpeedLightStats stats;
    private long timestamp;
    private int leftLight;
    private int rightLight;
    private int leftWheel;
    private int rightWheel;
    private FinchState state;

    @Before
    public void setup() {
        timestamp = System.nanoTime();
        leftLight = 60;
        rightLight = 9;
        leftWheel = -230;
        rightWheel = 230;
        state = FinchState.FOLLOWING;

        this.stats = new SpeedLightStats(leftLight, rightLight, leftWheel, rightWheel, state, timestamp);
    }

    @Test
    public void getTimestamp() {
        assertEquals(this.timestamp, stats.getTimestamp());
    }

    @Test
    public void getLeftLightIntensity() {
        assertEquals(this.leftLight, stats.getLeftLightIntensity());
    }

    @Test
    public void getRightLightIntensity() {
        assertEquals(this.rightLight, stats.getRightLightIntensity());
    }

    @Test
    public void getLeftWheelVelocity() {
        assertEquals(this.leftWheel, stats.getLeftWheelVelocity());
    }

    @Test
    public void getRightWheelVelocity() {
        assertEquals(this.rightWheel, stats.getRightWheelVelocity());
    }

    @Test
    public void getCurrState() {
        assertEquals(this.state, stats.getCurrState());
    }
}
