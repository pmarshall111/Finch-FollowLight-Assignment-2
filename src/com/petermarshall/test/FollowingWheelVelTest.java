package com.petermarshall.test;

import com.petermarshall.main.FollowingWheelVel;
import org.junit.Test;

import static com.petermarshall.main.FinchLimits.MAX_WHEEL_VEL;
import static com.petermarshall.main.FinchLimits.MIN_WHEEL_VEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FollowingWheelVelTest {
    @Test
    public void valsWithinLimitsAreNotChanged() {
        for (int i = MIN_WHEEL_VEL; i <= MAX_WHEEL_VEL; i++) {
            assertEquals(i, FollowingWheelVel.limit(i));
        }
    }

    @Test
    public void roundsValsBelowLimit() {
        assertEquals(MIN_WHEEL_VEL, FollowingWheelVel.limit(MIN_WHEEL_VEL-1));
        assertEquals(MIN_WHEEL_VEL, FollowingWheelVel.limit(MIN_WHEEL_VEL-100));
    }

    @Test
    public void roundsValsAboveLimit() {
        assertEquals(MAX_WHEEL_VEL, FollowingWheelVel.limit(MAX_WHEEL_VEL+1));
        assertEquals(MAX_WHEEL_VEL, FollowingWheelVel.limit(MAX_WHEEL_VEL+100));
    }

    @Test
    public void staysAtNormalSpeedIfPerfect() {
        int intensityToMatch = 150;
        int currLeftVel = 0, currRightVel = 0;
        int leftLight = 150, rightLight = 150;
        int[] newVels = FollowingWheelVel.getVel(intensityToMatch, currLeftVel, currRightVel, leftLight, rightLight);
        assertEquals(FollowingWheelVel.NORMAL_SPEED, newVels[0]);
        assertEquals(FollowingWheelVel.NORMAL_SPEED, newVels[1]);
    }

    @Test
    public void speedsUpIfTooFarAway() {
        int intensityToMatch = 150;
        int currLeftVel = 0, currRightVel = 0;
        int leftLight = 120, rightLight = 120;
        int[] newVels = FollowingWheelVel.getVel(intensityToMatch, currLeftVel, currRightVel, leftLight, rightLight);
        assertTrue(FollowingWheelVel.NORMAL_SPEED < newVels[0]);
        assertTrue(FollowingWheelVel.NORMAL_SPEED < newVels[1]);
    }

    @Test
    public void slowsDownIfTooClose() {
        int intensityToMatch = 150;
        int currLeftVel = 0, currRightVel = 0;
        int leftLight = 180, rightLight = 180;
        int[] newVels = FollowingWheelVel.getVel(intensityToMatch, currLeftVel, currRightVel, leftLight, rightLight);
        assertTrue(FollowingWheelVel.NORMAL_SPEED > newVels[0]);
        assertTrue(FollowingWheelVel.NORMAL_SPEED > newVels[1]);
    }

    @Test
    public void turnsLeft() {
        int intensityToMatch = 150;
        int currLeftVel = 0, currRightVel = 0;
        int leftLight = 200, rightLight = 150;
        int[] newVels = FollowingWheelVel.getVel(intensityToMatch, currLeftVel, currRightVel, leftLight, rightLight);
        assertTrue(newVels[0] < newVels[1]);
    }

    @Test
    public void turnsRight() {
        int intensityToMatch = 150;
        int currLeftVel = 0, currRightVel = 0;
        int leftLight = 150, rightLight = 200;
        int[] newVels = FollowingWheelVel.getVel(intensityToMatch, currLeftVel, currRightVel, leftLight, rightLight);
        assertTrue( newVels[0] > newVels[1]);
    }
}
