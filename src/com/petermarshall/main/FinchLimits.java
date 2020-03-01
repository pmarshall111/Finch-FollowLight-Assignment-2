package com.petermarshall.main;

//Class is used to share the min and max values of the Finch througout the program.
public class FinchLimits {
    //private constructor as this class should not be instantiated.
    private FinchLimits() {}

    public static final int MIN_LIGHT_INTENSITY = 0;
    public static final int MAX_LIGHT_INTENSITY = 255;
    public static final int MAX_WHEEL_VEL = 255;
    public static final int MIN_WHEEL_VEL = -255;
    public static final int MIN_LED_VAL = 0;
    public static final int MAX_LED_VAL = 255;
}