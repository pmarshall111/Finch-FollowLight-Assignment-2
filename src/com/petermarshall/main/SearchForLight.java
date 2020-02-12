package com.petermarshall.main;

import edu.cmu.ri.createlab.terk.robot.finch.Finch;

import java.awt.*;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;

import static com.petermarshall.main.FinchLimits.MAX_WHEEL_VEL;
import static com.petermarshall.main.FinchLimits.MIN_WHEEL_VEL;
import static com.petermarshall.main.TimeHelper.*;

//could potentially make this instantiable with getter methods. would allow for possible additional feature of
public class SearchForLight {

    private Finch finch;
    private FinchState finchState;

    private long scriptStartTime;
    private int numbDetections;

    private int finchIntensityToMatch;
    private int MIN_LIGHT_DETECT;

    private final int BASE_WHEEL_VEL = 100;

    private int currLeftVel;
    private int currRightVel;

    private ArrayList<SpeedLightStats> statList;
    private IntSummaryStatistics leftLightSummary;
    private IntSummaryStatistics rightLightSummary;

    boolean RUNNING = true;

    public SearchForLight(Finch sharedFinch) {
        finch = sharedFinch;
    }

    public void start() {
        RUNNING = true;
        init();
        waitForFinchToBeLevel();
        setDetectionLevels();
        beginSearching();
        detectLight();
        stopFinch();
    }

    public void stop() {
        RUNNING = false;
    }

    public boolean isRunning() {
        return RUNNING;
    }

    private void waitForFinchToBeLevel() {
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

    private boolean notStillForXSeconds(long prevTime, int xSeconds) {
        return !xSecondsPassed(prevTime, xSeconds);
    }

    private boolean finchIsntLevel() {
        return !finch.isFinchLevel();
    }

    private void checkFinchBeakUp() {
        if (finch.isBeakUp()) {
            RUNNING = false;
        }
    }

    private void init(){
        scriptStartTime = System.nanoTime(); //function allows for time period of 292years. So no worrying about overflows.
        numbDetections = 0;

        currLeftVel = 0;
        currRightVel = 0;

        statList = new ArrayList<>();
        leftLightSummary = new IntSummaryStatistics();
        rightLightSummary = new IntSummaryStatistics();
    }

    private void setDetectionLevels() {
        //will set it based on current light levels.
        //going to start with just 20% more, and intensity to match should be 20% more than that.
        int avgLight = getAvgLight();

        MIN_LIGHT_DETECT = Math.min(200, (int)(avgLight + 30));
        finchIntensityToMatch = Math.max(150, (int)(MIN_LIGHT_DETECT * 1.2));

        System.out.println("MIN light: " + MIN_LIGHT_DETECT + "\nIntens to match: " + finchIntensityToMatch);
    }

    private void detectLight() {
        long lastAction = System.nanoTime();

        while (RUNNING) {
            if (fourSecondsElapsed(lastAction)) {
                finchSearch();
                lastAction = System.nanoTime();
            } else if (finchDetectsLight()) {
                finchFollow();
                lastAction = System.nanoTime();
            } else if (finchFollowingButLightLvlsTooLow()) {
                finchFollow();
            }

            recordLightReadings();
            checkFinchBeakUp();
        }
    }

    private boolean finchFollowingButLightLvlsTooLow() {
        return finchState.equals(FinchState.FOLLOWING) && !finchDetectsLight();
    }

    private void beginSearching() {
        finchState = FinchState.SEARCH;
        finch.setLED(Color.YELLOW);
        moveForwardLowSpeed();
    }

    private void stopFinch() {
        finch.setWheelVelocities(0,0);
        finch.setLED(Color.black, 0);
    }

    private boolean lightAboveMinThreshold(SpeedLightStats stats) {
        return Math.max(stats.getLeftLightIntensity(), stats.getRightLightIntensity()) >= MIN_LIGHT_DETECT;
    }

    private boolean finchDetectsLight() {
        return (finch.isLeftLightSensor(MIN_LIGHT_DETECT) || finch.isRightLightSensor(MIN_LIGHT_DETECT));
    }

    private void recordLightReadings() {
        int left = finch.getLeftLightSensor();
        int right = finch.getRightLightSensor();

        statList.add(new SpeedLightStats(left, right, currLeftVel, currRightVel, finchState, System.nanoTime()));
        leftLightSummary.accept(left);
        rightLightSummary.accept(right);
    }


    // following light is based on the light difference between the left and right sensor as well as the overall light difference to how close we want to be to the light.
    // i.e. doesn't let us shoot past the light
    private void finchFollow() {
        if (!finchState.equals(FinchState.FOLLOWING)) {
            numbDetections++;
            finchState = FinchState.FOLLOWING;
        }
        setBeakIntensity();

        int maxDiffLeftAndRight = 90; //lowering increases steering sensitivity
        int accelMultiplier = 300; //increasing increases speed increase as light moves further away TODO: might need to set this based on what the light values are.

        // adjusting starting vel based on how far we are from light.
        int lightDiff = finchIntensityToMatch - getAvgLight();
        double diffAsRatio = (double)lightDiff/finchIntensityToMatch;
        int initialVel = BASE_WHEEL_VEL + (int)(accelMultiplier*diffAsRatio);

        int MAX_LEFT_INCREASE = MAX_WHEEL_VEL - currLeftVel;
        int MAX_RIGHT_INCREASE = MAX_WHEEL_VEL - currRightVel;
        int diffBetweenLeftAndRight = finch.getLeftLightSensor() - finch.getRightLightSensor();

        currLeftVel = getWheelVelInRange(initialVel - ((MAX_LEFT_INCREASE)*diffBetweenLeftAndRight/maxDiffLeftAndRight));
        currRightVel = getWheelVelInRange(initialVel + ((MAX_RIGHT_INCREASE)*diffBetweenLeftAndRight/maxDiffLeftAndRight));
        finch.setWheelVelocities(currLeftVel, currRightVel);
    }

    private void setBeakIntensity() {
        int MIN_RED_COMPONENT = 30;
        int MAX_RED_COMPONENT = 255;
        int RED_COMPONENT_RANGE = MAX_RED_COMPONENT - MIN_RED_COMPONENT;

        int lightForMaxBrightness = 200;
        int potentialLightRange = lightForMaxBrightness - MIN_LIGHT_DETECT;
        double distIntoRange = Math.max(0d, (double)(getAvgLight() - MIN_LIGHT_DETECT) / potentialLightRange); //do not want to set LED below min component. therefore min distance into range = 0
        int redComponent = (int)(distIntoRange * RED_COMPONENT_RANGE + MIN_RED_COMPONENT);
        finch.setLED(redComponent, 0, 0);
//        System.out.println("Light: " + getAvgLight() + " | red: " + redComponent);
    }


    private int getWheelVelInRange(int reqVel) {
        if (reqVel > MAX_WHEEL_VEL) {
            return MAX_WHEEL_VEL;
        } else if (reqVel < MIN_WHEEL_VEL) {
            return MIN_WHEEL_VEL;
        } else {
            return reqVel;
        }
    }

    private int atLeast20PercAboveMinLight(int lightReading) {
        int newMinLight = (int) (MIN_LIGHT_DETECT * 1.2);

        if (lightReading < newMinLight) {
            return newMinLight;
        } else {
            return lightReading;
        }
    }

    private int getAvgLight() {
        return (finch.getLeftLightSensor() + finch.getRightLightSensor())/2;
    }

    private int getMaxLight() {
        return Math.max(finch.getLeftLightSensor(), finch.getRightLightSensor());
    }

    private void finchSearch() {
        finchState = FinchState.SEARCH;
        finch.setLED(Color.YELLOW);
        finch.setWheelVelocities(0,0);
        sleep(500);

        turnFinch90Deg();
        moveForwardLowSpeed();
    }

    private void turnFinch90Deg() {
        int multiplier = (int) Math.round(Math.random());
        currLeftVel = multiplier * BASE_WHEEL_VEL;
        currRightVel = Math.abs(multiplier-1) * BASE_WHEEL_VEL;

        //NOTE: cannot use built in method to hold wheel velocities for a certain amount of time as this blocks the thread
        //execution. Can't do this as we want to record values at all times.
        long startTime = System.nanoTime();
        while (!xSecondsPassed(startTime, 2)) {
            finch.setWheelVelocities(currLeftVel, currRightVel);
            recordLightReadings();
        }
    }



    private void moveForwardLowSpeed() {
        currLeftVel = BASE_WHEEL_VEL;
        currRightVel = BASE_WHEEL_VEL;
        finch.setWheelVelocities(currLeftVel,currRightVel);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getScriptStartTime() {
        return scriptStartTime;
    }

    public int getNumbDetections() {
        return numbDetections;
    }

    public ArrayList<SpeedLightStats> getStatList() {
        return statList;
    }

    public IntSummaryStatistics getLeftLightSummary() {
        return leftLightSummary;
    }

    public IntSummaryStatistics getRightLightSummary() {
        return rightLightSummary;
    }
}
