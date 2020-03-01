package com.petermarshall.test;

import com.petermarshall.main.BeakIntensity;
import com.petermarshall.main.FollowingWheelVel;
import org.junit.Test;

import static com.petermarshall.main.BeakIntensity.getRedLightIntensity;
import static com.petermarshall.main.FinchLimits.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BeakIntensityTest {
    @Test
    public void valsWithinLimitsAreNotChanged() {
        for (int i = MIN_LED_VAL; i <= MAX_LED_VAL; i++) {
            assertEquals(i, BeakIntensity.limit(i));
        }
    }

    @Test
    public void roundsValsBelowLimit() {
        assertEquals(MIN_LED_VAL, BeakIntensity.limit(MIN_LED_VAL-1));
        assertEquals(MIN_LED_VAL, BeakIntensity.limit(MIN_LED_VAL-100));
    }

    @Test
    public void roundsValsAboveLimit() {
        assertEquals(MAX_LED_VAL, BeakIntensity.limit(MAX_LED_VAL+1));
        assertEquals(MAX_LED_VAL, BeakIntensity.limit(MAX_LED_VAL+100));
    }

    @Test
    public void brightnessIncreasesWithLight() {
        for (int lightForDetection = 30; lightForDetection<=200; lightForDetection+=10) {
            for (int i = MIN_LIGHT_INTENSITY; i < MAX_LIGHT_INTENSITY; i++) {
                int currIntensity = BeakIntensity.getRedLightIntensity(i, lightForDetection);
                int nextIntensity = BeakIntensity.getRedLightIntensity(i + 1, lightForDetection);
                assertTrue(currIntensity <= nextIntensity);
            }
        }
    }
}
