package com.petermarshall.main;

import static com.petermarshall.main.FinchLimits.MIN_LED_VAL;
import static com.petermarshall.main.FinchLimits.MAX_LED_VAL;

//Class is designed to set the Finch's beak brightness based on the light readings at the time. This is not done using
//a straight mapping of light reading to beak brightness, but rather uses altered ranges so that a greater range of colour
//is displayed during normal use of the program. Instead of using the light range of 0-255 to set the brightness, the
//@minLightForDetection is used for the minimum red component we want to display, and @lightForMaxBrightness is used for the max.
//The @minLightForDetection is the light at which the Finch will start to follow the light, so anything at or below this value
//will display the minimum light level.
public class BeakIntensity {
    //private constructor as this class should not be instantiated.
    private BeakIntensity() {}

    private static final int MIN_RED_COMPONENT = 30;
    private static final int MAX_RED_COMPONENT = 255;
    private static final int RED_COMPONENT_RANGE = MAX_RED_COMPONENT - MIN_RED_COMPONENT;

    public static int getRedLightIntensity(int lightVal, int minLightForDetection) {
        int lightForMaxBrightness = Math.max(200, minLightForDetection + 30);
        int potentialLightRange = lightForMaxBrightness - minLightForDetection;
        double distIntoRange = (double)(lightVal - minLightForDetection) / potentialLightRange;
        int redComponent = (int)(distIntoRange * RED_COMPONENT_RANGE + MIN_RED_COMPONENT);
        // It's possible that we may return values outside of the Finch's LED allowable range.
        // due to the Finch staying in the "Following light" state for 4seconds after it has lost the light source.
        // Here, the light readings would be below the @minLightForDetection, which would give a negative @redComponent.
        // It's also possible to get higher than max val, if there is a @lightVal > @lightForMaxBrightnes
        return limit(redComponent);
    }

    public static int limit(int reqLedIntensity) {
        if (reqLedIntensity < MIN_LED_VAL) {
            return MIN_LED_VAL;
        } else if (reqLedIntensity > MAX_LED_VAL) {
            return MAX_LED_VAL;
        } else {
            return reqLedIntensity;
        }
    }
}
