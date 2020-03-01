package com.petermarshall.main;

import edu.cmu.ri.createlab.terk.robot.finch.Finch;

import java.awt.*;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;

import static com.petermarshall.main.FinchLimits.MAX_WHEEL_VEL;
import static com.petermarshall.main.TimeHelper.*;

//The main class that executes the SearchForLight task. Not to be used by outside modules such as GUI's - see LightInterfaceThread
//for that. Class is kept package-private for this reason, along with some methods. Most methods and fields are kept private.
class SearchForLight {
    private Finch finch;
    private FinchState finchState;
    private long scriptStartTime;
    private int numbDetections;
    private int currLeftVel;
    private int currRightVel;
    private int finchIntensityToMatch;
    private int MIN_LIGHT_DETECT;
    static final int BASE_WHEEL_VEL = 100;
    private ArrayList<SpeedLightStats> statList;
    //IntSummaryStatistics used so that we don't have to search through the whole statList every time we want the
    //highestVal or lowestVal or avgVal etc. Included as a speed optimisation.
    private IntSummaryStatistics leftLightSummary;
    private IntSummaryStatistics rightLightSummary;
    boolean RUNNING = false;

    SearchForLight(Finch sharedFinch) {
        finch = sharedFinch;
    }

    //This is the main method to be called to begin execution of the program.
    void startProgram() {
        RUNNING = true;
        initVariables();
        waitForFinchToBeLevel();
        setDetectionLevels();
        beginSearching();
        detectLight();
        stopFinch();
    }

    void stop() {
        RUNNING = false;
    }

    boolean isRunning() {
        return RUNNING;
    }

    private void waitForFinchToBeLevel() {
        finchState = FinchState.WAITING_TO_BE_LEVEL;
        finch.setLED(Color.BLUE);
        long timeLastMoved = System.nanoTime();
        while (!xSecondsPassed(timeLastMoved, 3)) {
            if (finchIsntLevel()) {
                timeLastMoved = System.nanoTime();
            }
            recordLightReadings();
        }
    }

    private boolean finchIsntLevel() {
        return !finch.isFinchLevel();
    }

    private void checkFinchBeakUp() {
        if (finch.isBeakUp()) {
            RUNNING = false;
        }
    }

    private void initVariables(){
        scriptStartTime = System.nanoTime(); //function allows for time period of 292years. So it's effectively impossible for our program to be affected by overflow errors.
        numbDetections = 0;
        currLeftVel = 0;
        currRightVel = 0;
        statList = new ArrayList<>();
        leftLightSummary = new IntSummaryStatistics();
        rightLightSummary = new IntSummaryStatistics();
    }

    //The variables that govern the levels at which the finch reacts to light are set here based on ambient light at the time
    //of calling. @MIN_LIGHT_DETECT is used to govern the light reading at which the Finch will detect light.
    // @finchIntensityToMatch is used to determine the light reading the Finch would ideally have - used to change
    //the speed of the Finch based on the distance to the light source.
    //Light levels are set dynamically rather than hard coded so the finch is able to follow light regardless of whether
    //it starts in a light or dark environment.
    private void setDetectionLevels() {
        int avgLight = getAvgLight();
        MIN_LIGHT_DETECT = DetectionLevels.calcMinLightForDetection(avgLight);
        finchIntensityToMatch = DetectionLevels.calcFinchIntensityToMatch(MIN_LIGHT_DETECT);
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

    private void beginSearching() {
        finchState = FinchState.SEARCH;
        finch.setLED(Color.YELLOW);
        moveForwardLowSpeed();
    }

    private void stopFinch() {
        finch.setWheelVelocities(0,0);
        finch.setLED(0,0,0, 0); //turn off LED
    }

    private void recordLightReadings() {
        int left = finch.getLeftLightSensor();
        int right = finch.getRightLightSensor();
        statList.add(new SpeedLightStats(left, right, currLeftVel, currRightVel, finchState, System.nanoTime()));
        leftLightSummary.accept(left);
        rightLightSummary.accept(right);
    }

    private void finchFollow() {
        if (!finchState.equals(FinchState.FOLLOWING)) {
            numbDetections++;
            finchState = FinchState.FOLLOWING;
        }
        setBeakIntensity();
        int[] newWheelVels = FollowingWheelVel.getVel(finchIntensityToMatch, currLeftVel, currRightVel,
                                                        finch.getLeftLightSensor(), finch.getRightLightSensor());
        currLeftVel = newWheelVels[0];
        currRightVel = newWheelVels[1];
        finch.setWheelVelocities(currLeftVel, currRightVel);
    }

    private void setBeakIntensity() {
        int redComponent = BeakIntensity.getRedLightIntensity(getAvgLight(), MIN_LIGHT_DETECT);
        finch.setLED(redComponent, 0, 0);
    }

    private int getAvgLight() {
        return (finch.getLeftLightSensor() + finch.getRightLightSensor())/2;
    }

    private void finchSearch() {
        finchState = FinchState.SEARCH;
        finch.setLED(Color.YELLOW);
        finch.setWheelVelocities(0,0);
        sleep(500);
        turnFinch90Deg();
        moveForwardLowSpeed();
    }

    //Method chooses randomly whether to turn left or right.
    private void turnFinch90Deg() {
        int multiplier = (int) Math.round(Math.random());
        currLeftVel = multiplier * BASE_WHEEL_VEL;
        currRightVel = Math.abs(multiplier-1) * BASE_WHEEL_VEL;
        //NOTE: cannot use built in method (finch.setWheelVelocities(leftVel, rightVel, 2000)) to hold wheel velocities
        // for a certain amount of time as this blocks the thread execution. i.e. nothing else can be done on this thread
        // while waiting for the time to elapse. So we can't use the built in method as we want to record light and velocity
        // values at all times.
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

    private boolean finchDetectsLight() {
        return (finch.isLeftLightSensor(MIN_LIGHT_DETECT) || finch.isRightLightSensor(MIN_LIGHT_DETECT));
    }

    private boolean finchFollowingButLightLvlsTooLow() {
        return finchState.equals(FinchState.FOLLOWING) && !finchDetectsLight();
    }

    //Method created to remove try catch block from other methods.
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    long getScriptStartTime() {
        return scriptStartTime;
    }

    int getNumbDetections() {
        return numbDetections;
    }

    ArrayList<SpeedLightStats> getStatList() {
        return statList;
    }

    IntSummaryStatistics getLeftLightSummary() {
        return leftLightSummary;
    }

    IntSummaryStatistics getRightLightSummary() {
        return rightLightSummary;
    }
}
