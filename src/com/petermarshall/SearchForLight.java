package com.petermarshall;

//import com.github.freva.asciitable.AsciiTable;
import edu.cmu.ri.createlab.terk.robot.finch.Finch;

import java.awt.*;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;

public class SearchForLight {

    private static Finch finch;
    private static FinchState finchState;
    private static int finchIntensityToMatch;

    static ArrayList<Integer> lightReadings;
    static long scriptStartTime;
    static int numbDetections;

    private static final int MIN_LIGHT_DETECT = 80;

    static final int MIN_LIGHT_INTENSITY = 0;
    static final int MAX_LIGHT_INTENSITY = 100;

    static final int MAX_WHEEL_VEL = 255;
    static final int MIN_WHEEL_VEL = -255;
    private static final int BASE_WHEEL_VEL = 100;

    private static int currLeftVel;
    private static int currRightVel;

    static ArrayList<SpeedLightStats> stats;
    static IntSummaryStatistics leftLightSummary;
    static IntSummaryStatistics rightLightSummary;

    static boolean END_RUN = false;


    public static void start(Finch sharedFinch) {
        finch = sharedFinch;

        init();
        waitForFinchToBeLevel();
        detectLight();
        stopFinch();
    }

    private static void waitForFinchToBeLevel() {
        long timeLastMoved = System.nanoTime();
        finch.setLED(Color.BLUE);

        while (notStillForXSeconds(timeLastMoved, 3)) {
            if (finchIsntLevel()) {
                timeLastMoved = System.nanoTime();
                recordLightReadings();
            }
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
            END_RUN = true;
        }
    }

    private static void init(){
        lightReadings = new ArrayList<Integer>();
        lightReadings.add(finch.getLeftLightSensor());
        lightReadings.add(finch.getRightLightSensor());

        scriptStartTime = System.nanoTime(); //function allows for time period of 292years. So no worrying about overflows.
        numbDetections = 0;

        finch.setWheelVelocities(BASE_WHEEL_VEL,BASE_WHEEL_VEL); //max values +/-255
        finch.setLED(Color.YELLOW);
        finchState = FinchState.WAITING_TO_BE_LEVEL;

        currLeftVel = 0;
        currRightVel = 0;

        stats = new ArrayList<>();
        leftLightSummary = new IntSummaryStatistics();
        rightLightSummary = new IntSummaryStatistics();
        recordLightReadings();
    }

    private static void detectLight() {
        long prevTime = System.nanoTime();

        while (!END_RUN) {
            if (finchDetectsLight()) {
                finchFollow();
                prevTime = System.nanoTime();
            } else if (fourSecondsElapsed(prevTime)) {
                finchSearch();
                prevTime = System.nanoTime();
            } else {
                //if we've lost our light and it's not been 4 seconds yet, we want to shoot forward in desperation to find it?
                //or do we lower the min light intensity???

//                finchDesperate();
            }

            recordLightReadings();
            checkFinchBeakUp();
        }
        System.out.println("Ending run");

//        showStats();
//        finch.quit();
    }

    private static void stopFinch() {
        finch.setWheelVelocities(0,0);
        finch.setLED(Color.black, 0);
    }

    //to be called when the light has been lost, but 4 seconds have not yet passed.
//    private static void finchDesperate() {
//        //perhaps a good idea at the start would be to do a full circle to see if it sees the light again?
//
//
//        //will go back through light reading history until we last saw the light.
//        //then we go back for a bit more and see how the pattern of the light changes. Expecting 3 basic patterns:
//        // - goes forwards out of range (shown by both light sensors decreasing at roughly same intervals)
//        // - goes right out of range (shown by left sensor decreasing faster)
//        // - ^^ same for LHS
//
//        //thinking is that if it goes backwards out of range the finch would have time to make a turn and so actually it would be to one side out of range.
//
//        //potentially we take into account how long ago we last saw the light was?? Anything more than 2 seconds and it's probably pretty useless.
//        //TODO: test for how long we should do this for.
//
//        //thought... what do we do when we've already taken action and it hasn't found it. i presume that through this method we will jsut go in circles.
//        //perhaps we can solve by timing out... but then we wouldn't be able to detect new light.
//        //perhaps a locking wheels idea? or maybe this method only gets called once every so often.
//        //fuck it lets just make it and then experiment.
//
//
//        SpeedLightStats currStats = stats.get(stats.size()-1);
//        int indexFromEnd = findLastLight();
//        SpeedLightStats lastDetection = stats.get(stats.size() - indexFromEnd);
//
//        //now we can get a rough direction
//        SpeedLightStats fiveBeforeLastDetection = stats.get(stats.size() - indexFromEnd - 5);
//
//        //if we go through the most recent ones before this, how can we establish how the light is moving??
//        int leftDiff = lastDetection.getLeftLightIntensity() - fiveBeforeLastDetection.getLeftLightIntensity();
//        int rightDiff = lastDetection.getRightLightIntensity() - fiveBeforeLastDetection.getRightLightIntensity();
//
//        if (leftDiff < rightDiff) {
//            //TODO: think through what the the left and right diff mean in terms of positive and negative and decide what to do with each.
//        }
//    }
//
//    private static int findLastLight() {
//        int subtractFromSize = 2; //no point going to the last entry as we know that we did not see light otherwise we wouldn't be in this function
//        SpeedLightStats historicStats = stats.get(stats.size()-subtractFromSize);
//
//        while (!lightAboveMinThreshold(historicStats)) {
//            historicStats = stats.get(stats.size() - --subtractFromSize);
//        }
//
//        return subtractFromSize;
//    }

    private static boolean lightAboveMinThreshold(SpeedLightStats stats) {
        return Math.max(stats.getLeftLightIntensity(), stats.getRightLightIntensity()) >= MIN_LIGHT_DETECT;
    }

    private static void showStats() {
        System.out.println(getStatsTable());
    }

    private static boolean finchDetectsLight() {
        return (finch.isLeftLightSensor(MIN_LIGHT_DETECT) || finch.isRightLightSensor(MIN_LIGHT_DETECT));
    }

    private static void recordLightReadings() {
        lightReadings.add(finch.getLeftLightSensor()); //TODO: possible redundancy now.
        lightReadings.add(finch.getRightLightSensor());

        int left = finch.getLeftLightSensor();
        int right = finch.getRightLightSensor();

        stats.add(new SpeedLightStats(left, right, currLeftVel, currRightVel, finchState, System.nanoTime()));
        leftLightSummary.accept(left);
        rightLightSummary.accept(right);
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
            int maxLightDiff = lightAt2cm - MIN_LIGHT_DETECT;

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

//        System.out.println(finch.getLeftLightSensor() + " | " + finch.getRightLightSensor());


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
        if (!finchState.equals(FinchState.FOLLOWING)) {
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
//        IntSummaryStatistics summary = lightReadings.stream().mapToInt(Integer::intValue).summaryStatistics();
//
//        int maxLightVal = summary.getMax();
//        int minLightVal = summary.getMin();
//        double avg = summary.getAverage();
//        long scriptDuration = System.nanoTime() - scriptStartTime;
//
//        long minutes = TimeUnit.NANOSECONDS.toMinutes(scriptDuration);
//        long seconds = TimeUnit.NANOSECONDS.toSeconds(scriptDuration) - minutes*60;
//
//        String[][] rows = {
//                {"Left light sensor at beginning", lightReadings.get(0)+""},
//                {"Right light sensor at beginning", lightReadings.get(1)+""},
//                {"",""},
//                {"Highest sensor reading", maxLightVal+""},
//                {"Lowest sensor reading", minLightVal+""},
//                {"Average sensor reading", String.format("%.1f",avg)}, //need to know how this works fully before viva. rounds to 1dp
//                {"",""},
//                {"Script duration", String.format("%1dm %2ds", minutes, seconds)},
//                {"Numb detections", numbDetections+""}
//        };
//
//        return AsciiTable.getTable(rows);

        return "Output table removed for testing purposes of UI. Uncomment to restore.";
    }

}
