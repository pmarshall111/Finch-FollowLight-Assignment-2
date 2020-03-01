package com.petermarshall.main;

import static com.petermarshall.main.FinchLimits.MAX_WHEEL_VEL;
import static com.petermarshall.main.FinchLimits.MIN_WHEEL_VEL;
import static com.petermarshall.main.SearchForLight.BASE_WHEEL_VEL;

//Following the light is based on 2 factors; the distance to the light and orientation of the light to the Finch.
//Speed is increased if the light is moving away from the finch and reduced if the light moves closer. Note that under
//the hood this works by having an ideal light intensity set at the beginning of the program (@finchIntensityToMatch)
//and ajdusts the speed based on if the current light reading is above/below that value.
//After an initial speed is calculated, direction is taken into account by comparing the difference between the left
//and right light readings of the Finch.
public class FollowingWheelVel {
    //private constructor as do not want class to be instantiated.
    private FollowingWheelVel() {}

    public static final int NORMAL_SPEED = BASE_WHEEL_VEL;

    public static int[] getVel(int intensityToMatch, int currLeftVel, int currRightVel, int leftLight, int rightLight) {
        int maxDiffLeftAndRight = 90; //lowering increases steering sensitivity
        int accelMultiplier = 300; //lowering reduces the acceleration when the light is moved closer/further away from the Finch
        //First adjusting speed based on how far the Finch is from light.
        int avgLight = (leftLight + rightLight) / 2;
        int lightDiff = intensityToMatch - avgLight;
        double diffAsRatio = (double)(lightDiff)/intensityToMatch;
        int initialVel = NORMAL_SPEED + (int)(accelMultiplier*diffAsRatio);
//        int initialVel = (int)(accelMultiplier*diffAsRatio);
        //Later adding direction based on difference between left and right sensors.
        int MAX_LEFT_INCREASE = MAX_WHEEL_VEL - currLeftVel;
        int MAX_RIGHT_INCREASE = MAX_WHEEL_VEL - currRightVel;
        int diffBetweenLeftAndRight = leftLight - rightLight;
        int newLeftVel = initialVel - ((MAX_LEFT_INCREASE)*diffBetweenLeftAndRight/maxDiffLeftAndRight);
        int newRightVel = initialVel + ((MAX_RIGHT_INCREASE)*diffBetweenLeftAndRight/maxDiffLeftAndRight);
        return new int[]{limit(newLeftVel), limit(newRightVel)};
    }

    public static int limit(int reqVel) {
        if (reqVel > MAX_WHEEL_VEL) {
            return MAX_WHEEL_VEL;
        } else if (reqVel < MIN_WHEEL_VEL) {
            return MIN_WHEEL_VEL;
        } else {
            return reqVel;
        }
    }
}
