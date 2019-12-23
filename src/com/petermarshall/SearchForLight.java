package com.petermarshall;

import com.github.freva.asciitable.AsciiTable;
import edu.cmu.ri.createlab.terk.robot.finch.Finch;

import java.awt.*;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.concurrent.TimeUnit;

public class SearchForLight {

    private static Finch finch;
    private static FinchState finchState;
    private static int finchIntensityToMatch;

    private static ArrayList<Integer> lightReadings;
    private static long scriptStartTime;
    private static int numbDetections;

    private static final int MIN_LIGHT_INTENSITY = 80;

    private static final int MAX_WHEEL_VEL = 255;
    private static final int MIN_WHEEL_VEL = -255;
    private static final int BASE_WHEEL_VEL = 100;

    private static int currLeftVel;
    private static int currRightVel;

    private static ArrayList<SpeedLightStats> stats;


    public static void start(Finch sharedFinch) {
        finch = sharedFinch;

        waitForFinchToBeLevel();
        init();
        detectLight();
    }

    private static void waitForFinchToBeLevel() {
        long timeLastMoved = System.nanoTime();

        while (notStillForXSeconds(timeLastMoved, 3)) {
            if (finchIsntLevel()) {
                timeLastMoved = System.nanoTime();
            }
        }
    }

    private static boolean notStillForXSeconds(long prevTime, int xSeconds) {
        return !xSecondsPassed(prevTime, xSeconds);
    }

    private static boolean finchIsntLevel() {
        return !finch.isFinchLevel();
    }

    private static boolean finchsBeakIsntUp() {
        return !finch.isBeakUp();
    }

    private static void init(){
        lightReadings = new ArrayList<Integer>();
        lightReadings.add(finch.getLeftLightSensor());
        lightReadings.add(finch.getRightLightSensor());

        scriptStartTime = System.nanoTime(); //function allows for time period of 292years. So no worrying about overflows.
        numbDetections = 0;

        finch.setWheelVelocities(BASE_WHEEL_VEL,BASE_WHEEL_VEL); //max values +/-255
        finch.setLED(Color.YELLOW);
        finchState = FinchState.SEARCH;

        currLeftVel = 0;
        currRightVel = 0;

        stats = new ArrayList<>();
    }

    private static void detectLight() {
        long prevTime = System.nanoTime();

        while (finchsBeakIsntUp()) {
            if (finchDetectsLight()) {
                finchFollow();
                prevTime = System.nanoTime();
            } else if (fourSecondsElapsed(prevTime)) {
                finchSearch();
                prevTime = System.nanoTime();
            } else {
                //if we've lost our light and it's not been 4 seconds yet, we want to shoot forward in desperation to find it?
                //or do we lower the min light intensity???
            }

            recordLightReadings();
        }

        showStats();
        finch.quit();
    }

    private static void showStats() {
        System.out.println(getStatsTable());
    }

    private static boolean finchDetectsLight() {
        return (finch.isLeftLightSensor(MIN_LIGHT_INTENSITY) || finch.isRightLightSensor(MIN_LIGHT_INTENSITY));
    }

    private static void recordLightReadings() {
        lightReadings.add(finch.getLeftLightSensor());
        lightReadings.add(finch.getRightLightSensor());

        stats.add(new SpeedLightStats(finch.getLeftLightSensor(), finch.getRightLightSensor(), currLeftVel, currRightVel));
    }

    //TODO: definitely needs tidying up.
    private static void finchFollow() {
        addNumbDetections();
        checkIfModeChange();

        //max difference seems to be about 90.
        int typicalMaxDifference = 90;
        int maxAmountWeCanAdd = MAX_WHEEL_VEL - BASE_WHEEL_VEL;
        int MAX_WE_CAN_INCREASE_BY = MAX_WHEEL_VEL - MIN_WHEEL_VEL;

        int diffBetweenLeftAndRight = finch.getLeftLightSensor() - finch.getRightLightSensor();
        int avgLight = getAvgLight();

        if (finchState.equals(FinchState.KEEP_DISTANCE)) {
            finch.setLED(avgLight, 0, avgLight);




            //when avgLight is within 20 of min light intensity, we need 255 speed.

            //perhaps we add in a concept of acceleration. so we look over the light intensities of the last few iterations and decide if we need to speed up or slow down
            //based on how the last few vals have changed.

            //need acceleration


            //distance to match is not about the avg light, it's the max. If I put the light source really close over the left sensor so the right sensor doesn't get much readings,
            //it;s still closer than if it was the avg light for both.

            int lightDiff = finchIntensityToMatch - avgLight;

            int maxToAddLeft = MAX_WHEEL_VEL - currLeftVel;
            int maxToRemoveLeft = currLeftVel + MIN_WHEEL_VEL;

            int maxToAddRight = MAX_WHEEL_VEL - currRightVel;
            int maxToRemoveRight = currRightVel + MIN_WHEEL_VEL;

            //now we know how much we have to play with we need to know what sort of differences we can expect.
            int maxLightDiffLeftToRight = 90;
            int lightAt2cm = 200;

            //i think first we must focus on keeping the light the same distance.
            int maxLightDiff = lightAt2cm - MIN_LIGHT_INTENSITY;

            SpeedLightStats mostRecent = stats.get(stats.size()-1);
            SpeedLightStats previous = stats.get(stats.size()-2);

            int mostRecentLeftLightDiff = finchIntensityToMatch - mostRecent.getLeftLightIntensity();
            int mostRecentRightLightDiff = finchIntensityToMatch - mostRecent.getRightLightIntensity();

            int prevLeftLightDiff = finchIntensityToMatch - previous.getLeftLightIntensity();
            int prevRightLightDiff = finchIntensityToMatch - previous.getRightLightIntensity();

            int leftLightDiff = mostRecentLeftLightDiff - prevLeftLightDiff;
            int rightLightDiff = mostRecentRightLightDiff - prevRightLightDiff;

            //if we're at 100 target, but 80 intens. then we must increase speed.

            double toAddLeft = MAX_WE_CAN_INCREASE_BY * (double) leftLightDiff/maxLightDiff;
            double toAddRight = MAX_WE_CAN_INCREASE_BY * (double) rightLightDiff/maxLightDiff;

            currLeftVel = getWheelVelInRange(currLeftVel + (int) toAddLeft);
            currRightVel = getWheelVelInRange(currRightVel + (int) toAddRight);

            finch.setWheelVelocities(currLeftVel, currRightVel);


        } else {
            finch.setLED(avgLight, 0, 0);

            //if left is bigger we want to slow down left wheel

            //we divide by 2 for max we can increase by as works best with smaller increment
            currLeftVel = getWheelVelInRange(BASE_WHEEL_VEL - ((MAX_WE_CAN_INCREASE_BY/2)*diffBetweenLeftAndRight/typicalMaxDifference));
            currRightVel = getWheelVelInRange(BASE_WHEEL_VEL + ((MAX_WE_CAN_INCREASE_BY/2)*diffBetweenLeftAndRight/typicalMaxDifference));
            //NOTE: normal works best when using a lower amount we can add.

            finch.setWheelVelocities(currLeftVel, currRightVel);
        }

        System.out.println(finch.getLeftLightSensor() + " | " + finch.getRightLightSensor());


    }

    private static void finchKeepDistance() {
        finch.setLED(getAvgLight(), 0, getAvgLight());


    }

    private static void finchScared() {
        //we want the finch to run away from the light and try to find the darkest area. Basically the opposite of
        //the follow function.
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


    private static void addNumbDetections() {
        if (finchState.equals(FinchState.SEARCH)) {
            finchState = FinchState.FOLLOWING;
            numbDetections++;
        }
    }

    private static void checkIfModeChange() {
        if (finch.isTapped()) {
            finchState = FinchState.swapFollowAndKeepDist(finchState);

            if (finchState.equals(FinchState.KEEP_DISTANCE)) {
                int lightReading = getMaxLight();
                finchIntensityToMatch = atLeast20PercAboveMinLight(lightReading);
            }
        }
    }

    private static int atLeast20PercAboveMinLight(int lightReading) {
        int newMinLight = (int) (MIN_LIGHT_INTENSITY * 1.2);

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

        int multiplier = (int) Math.round(Math.random());
        currLeftVel = multiplier * BASE_WHEEL_VEL;
        currRightVel = Math.abs(multiplier-1) * BASE_WHEEL_VEL;
        finch.setWheelVelocities(currLeftVel, currRightVel, 3000); //values to be played around with to get roughly 90degs.

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

    private static String getStatsTable() {
        IntSummaryStatistics summary = lightReadings.stream().mapToInt(Integer::intValue).summaryStatistics();

        int maxLightVal = summary.getMax();
        int minLightVal = summary.getMin();
        double avg = summary.getAverage();
        long scriptDuration = System.nanoTime() - scriptStartTime;

        long minutes = TimeUnit.NANOSECONDS.toMinutes(scriptDuration);
        long seconds = TimeUnit.NANOSECONDS.toSeconds(scriptDuration) - minutes*60;

        String[][] rows = {
                {"Left light sensor at beginning", lightReadings.get(0)+""},
                {"Right light sensor at beginning", lightReadings.get(1)+""},
                {"",""},
                {"Highest sensor reading", maxLightVal+""},
                {"Lowest sensor reading", minLightVal+""},
                {"Average sensor reading", String.format("%.1f",avg)}, //need to know how this works fully before viva. rounds to 1dp
                {"",""},
                {"Script duration", String.format("%1dm %2ds", minutes, seconds)},
                {"Numb detections", numbDetections+""}
        };

        return AsciiTable.getTable(rows);
    }
}
