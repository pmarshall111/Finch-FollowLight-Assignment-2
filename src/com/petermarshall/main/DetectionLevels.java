package com.petermarshall.main;

//Class is used to dynamically set the levels at which the finch will detect and follow light based on the light readings
//at the time of calling. Dynamically setting these levels allow the finch to have different intensities at which it should
//detect light, so the finch is able to follow light in both light and dark environments.
public class DetectionLevels {
    public static final int MAX_LIGHT_DETECTION_LEVELS = 200;
    public static final int LIGHT_DETECTION_BUFFER = 30;
    public static final int MIN_INTENSITY_TO_MATCH = 150;
    public static final double INTENSITY_MULTIPLIER = 1.2;

    //private constructor as this class should not be instantiated.
    private DetectionLevels() {}

    //minLightForDetection is used to govern the light reading at which the Finch will detect light, and is set to a
    //max governed by @MAX_LIGHT_DETECTION_LEVELS. The minLightForDetecion is also set to 30 above the light value
    // so that there is a buffer between the starting light and the light level where the finch will start following the light.
    public static int calcMinLightForDetection(int lightVal) {
        int proposedVal = lightVal + LIGHT_DETECTION_BUFFER;
        if (proposedVal > MAX_LIGHT_DETECTION_LEVELS) {
            return MAX_LIGHT_DETECTION_LEVELS;
        } else {
            return proposedVal;
        }
    }

    //intensityToMatch is used to determine whether the Finch should speed up or slow down based on if the current light
    //is higher or lower than this intensityToMatch value. Note that this is set based on what the minLightDetect is
    //(the light level at which the Finch starts following light). This value is multiplied by a multiplier so that
    //the light level at which the Finch wants to be at is higher than the minimum light at which it starts following.
    public static int calcFinchIntensityToMatch(int minLightDetect) {
        int proposedVal = (int)(minLightDetect* INTENSITY_MULTIPLIER);
        if (proposedVal < MIN_INTENSITY_TO_MATCH) {
            return MIN_INTENSITY_TO_MATCH;
        } else {
            return proposedVal;
        }
    }
}
