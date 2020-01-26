package com.petermarshall;

//import com.github.freva.asciitable.AsciiTable;
import edu.cmu.ri.createlab.terk.robot.finch.Finch;

import java.awt.*;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;

public class SearchForLight {

    private static Finch finch;
    private static FinchState finchState;

    static long scriptStartTime;
    static int numbDetections;

    private static int finchIntensityToMatch;
    private static int MIN_LIGHT_DETECT;

    static final int MIN_LIGHT_INTENSITY = 0;
    static final int MAX_LIGHT_INTENSITY = 255;
    static final int MAX_WHEEL_VEL = 255;
    static final int MIN_WHEEL_VEL = -255;
    private static final int BASE_WHEEL_VEL = 100;

    private static int currLeftVel;
    private static int currRightVel;

    static ArrayList<SpeedLightStats> statList;
    static IntSummaryStatistics leftLightSummary;
    static IntSummaryStatistics rightLightSummary;

    static boolean RUNNING = true;

    public static void start(Finch sharedFinch) {
        finch = sharedFinch;

        init();
        waitForFinchToBeLevel();
        setDetectionLevels();
        beginSearching();
        detectLight();
        stopFinch();
    }

    private static void waitForFinchToBeLevel() {
        finchState = FinchState.WAITING_TO_BE_LEVEL;
        finch.setLED(Color.BLUE);

        long timeLastMoved = System.nanoTime();

        while (notStillForXSeconds(timeLastMoved, 3)) {
            if (finchIsntLevel()) {
                timeLastMoved = System.nanoTime();
            }
            recordLightReadings();
        }
    }

    private static boolean notStillForXSeconds(long prevTime, int xSeconds) {
        return !xSecondsPassed(prevTime, xSeconds);
    }

    private static boolean finchIsntLevel() {
        return !finch.isFinchLevel();
    }

    private static void checkFinchBeakUp() {
        if (finch.isBeakUp()) {
            RUNNING = false;
        }
    }

    private static void init(){
        scriptStartTime = System.nanoTime(); //function allows for time period of 292years. So no worrying about overflows.
        numbDetections = 0;

        currLeftVel = 0;
        currRightVel = 0;

        statList = new ArrayList<>();
        leftLightSummary = new IntSummaryStatistics();
        rightLightSummary = new IntSummaryStatistics();
    }

    private static void setDetectionLevels() {
        //will set it based on current light levels.
        //going to start with just 20% more, and intensity to match should be 20% more than that.
        int avgLight = getAvgLight();

        MIN_LIGHT_DETECT = Math.min(200, (int)(avgLight + 30));
        finchIntensityToMatch = Math.max(150, (int)(MIN_LIGHT_DETECT * 1.2));

        System.out.println("MIN light: " + MIN_LIGHT_DETECT + "\nIntens to match: " + finchIntensityToMatch);
    }

    private static void detectLight() {
        long lastAction = System.nanoTime();

        while (RUNNING) {
            if (finchDetectsLight()) {
                finchFollow();
                lastAction = System.nanoTime();
            } else if (fourSecondsElapsed(lastAction)) {
                finchSearch();
                lastAction = System.nanoTime();
            } else {
                //if we've lost our light and it's not been 4 seconds yet, we want to shoot forward in desperation to find it?
                //or do we lower the min light intensity?
            }

            recordLightReadings();
            checkFinchBeakUp();
        }
    }

    private static void beginSearching() {
        finchState = FinchState.SEARCH;
        finch.setLED(Color.YELLOW);
        moveForwardLowSpeed();
    }

    private static void stopFinch() {
        finch.setWheelVelocities(0,0);
        finch.setLED(Color.black, 0);
    }

    private static boolean lightAboveMinThreshold(SpeedLightStats stats) {
        return Math.max(stats.getLeftLightIntensity(), stats.getRightLightIntensity()) >= MIN_LIGHT_DETECT;
    }

    private static boolean finchDetectsLight() {
        return (finch.isLeftLightSensor(MIN_LIGHT_DETECT) || finch.isRightLightSensor(MIN_LIGHT_DETECT));
    }

    private static void recordLightReadings() {
        int left = finch.getLeftLightSensor();
        int right = finch.getRightLightSensor();

        statList.add(new SpeedLightStats(left, right, currLeftVel, currRightVel, finchState, System.nanoTime()));
        leftLightSummary.accept(left);
        rightLightSummary.accept(right);
    }


    // following light is based on the light difference between the left and right sensor as well as the overall light difference to how close we want to be to the light.
    // i.e. doesn't let us shoot past the light
    private static void finchFollow() {
        if (!finchState.equals(FinchState.FOLLOWING)) {
            numbDetections++;
            finchState = FinchState.FOLLOWING;
            finch.setLED(Color.RED); //TODO: need to set the brightness of the beak based on how bright the light is.
        }

        int MAX_LEFT_INCREASE = MAX_WHEEL_VEL - currLeftVel;
        int MAX_RIGHT_INCREASE = MAX_WHEEL_VEL - currRightVel;

        int maxDiffLeftAndRight = 90; //lowering increases steering sensitivity
        int accelMultiplier = 300; //increasing increases speed increase as light moves further away TODO: might need to set this based on what the light values are.
        int diffBetweenLeftAndRight = finch.getLeftLightSensor() - finch.getRightLightSensor();

        // adjusting starting vel based on how far we are from light.
        int lightDiff = finchIntensityToMatch - getAvgLight();
        double diffAsRatio = (double)lightDiff/finchIntensityToMatch;
        int initialVel = BASE_WHEEL_VEL + (int)(accelMultiplier*diffAsRatio);

        currLeftVel = getWheelVelInRange(initialVel - ((MAX_LEFT_INCREASE)*diffBetweenLeftAndRight/maxDiffLeftAndRight));
        currRightVel = getWheelVelInRange(initialVel + ((MAX_RIGHT_INCREASE)*diffBetweenLeftAndRight/maxDiffLeftAndRight));
        finch.setWheelVelocities(currLeftVel, currRightVel);
    }


    private static int getWheelVelInRange(int reqVel) {
        if (reqVel > MAX_WHEEL_VEL) {
            return MAX_WHEEL_VEL;
        } else if (reqVel < MIN_WHEEL_VEL) {
            return MIN_WHEEL_VEL;
        } else {
            return reqVel;
        }
    }

    private static int atLeast20PercAboveMinLight(int lightReading) {
        int newMinLight = (int) (MIN_LIGHT_DETECT * 1.2);

        if (lightReading < newMinLight) {
            return newMinLight;
        } else {
            return lightReading;
        }
    }

    private static int getAvgLight() {
        return (finch.getLeftLightSensor() + finch.getRightLightSensor())/2;
    }

    private static int getMaxLight() {
        return Math.max(finch.getLeftLightSensor(), finch.getRightLightSensor());
    }

    private static void finchSearch() {
        finchState = FinchState.SEARCH;
        finch.setLED(Color.YELLOW);
        finch.setWheelVelocities(0,0);
        sleep(500);

        turnFinch90Deg();
        moveForwardLowSpeed();
    }

    private static void turnFinch90Deg() {
        int multiplier = (int) Math.round(Math.random());
        currLeftVel = multiplier * BASE_WHEEL_VEL;
        currRightVel = Math.abs(multiplier-1) * BASE_WHEEL_VEL;

        //NOTE: cannot use built in method to hold wheel velocities for a certain amount of time as this blocks the thread
        //execution. Can't do this as we want to record values at all times.
        long startTime = System.nanoTime();
        while (!xSecondsPassed(startTime, 3)) {
            finch.setWheelVelocities(currLeftVel, currRightVel);
            recordLightReadings();
        }
    }



    private static void moveForwardLowSpeed() {
        currLeftVel = BASE_WHEEL_VEL;
        currRightVel = BASE_WHEEL_VEL;
        finch.setWheelVelocities(currLeftVel,currRightVel);
    }

    private static boolean fourSecondsElapsed(long prevTime) {
        return xSecondsPassed(prevTime, 4);
    }

    private static boolean xSecondsPassed(long prevTime, double xSeconds) {
        long currTime = System.nanoTime();
        long nanoSecs = (long) (xSeconds*Math.pow(10,9));
        return currTime - prevTime > nanoSecs;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
