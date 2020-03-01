package com.petermarshall.test;

import com.petermarshall.main.DetectionLevels;
import org.junit.Test;

import static com.petermarshall.main.DetectionLevels.*;
import static org.junit.Assert.assertEquals;

public class DetectionLevelsTest {
    @Test
    public void minLightForDetectionNotAboveMaxVal() {
        int oneAbove = calcMinLightForDetection(MAX_LIGHT_DETECTION_LEVELS + 1);
        assertEquals(MAX_LIGHT_DETECTION_LEVELS, oneAbove);
        int moreAbove = calcMinLightForDetection(MAX_LIGHT_DETECTION_LEVELS + 20);
        assertEquals(MAX_LIGHT_DETECTION_LEVELS, moreAbove);
    }

    @Test
    public void normalMinLightForDetectionUnchanged() {
        for (int i = 0; i<=MAX_LIGHT_DETECTION_LEVELS-LIGHT_DETECTION_BUFFER; i++) {
            int v = calcMinLightForDetection(i);
            assertEquals(i+LIGHT_DETECTION_BUFFER,v);
        }
    }

    @Test
    public void intensityToMatchNotBelowMin() {
        int below = calcFinchIntensityToMatch(10);
        assertEquals(MIN_INTENSITY_TO_MATCH, below);
    }

    @Test
    public void normalIntensityValsUnaffected() {
        int minVal = (int) Math.ceil(MIN_INTENSITY_TO_MATCH/INTENSITY_MULTIPLIER);
        for (int i = minVal; i<=MAX_LIGHT_DETECTION_LEVELS; i++) {
            assertEquals((int)(i*INTENSITY_MULTIPLIER), calcFinchIntensityToMatch(i));
        }
    }
}
