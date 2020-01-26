package com.petermarshall;

import edu.cmu.ri.createlab.terk.robot.finch.Finch;

import java.util.ArrayList;

public class LightInterfaceThread extends Thread {

    private static Finch sharedFinch;

    @Override
    public void run() {
        //need to reinitialise the Finch each time else we have trouble connect
//        sharedFinch = new Finch();
        SearchForLight.RUNNING = true;
        SearchForLight.start(sharedFinch);
    }
    

    public static void init(Finch finch) {
        //separated to try speed up starting of the task when user clicks on it.
        sharedFinch = finch;
    }

    public void stopProgram() {
        SearchForLight.RUNNING = false;
//        sharedFinch.quit();
    }

    public static boolean isRunning() {
        return SearchForLight.RUNNING;
    }

    //TODO: will also need methods to get the live data out to the UI

    public static long getTimeElapsedInNS() throws NullPointerException, ArrayIndexOutOfBoundsException {
        return getMostRecentStats().getTimestamp() - SearchForLight.scriptStartTime;
    }

    //throws NullPointerException just in case we request the statList before the run has started.
    //throws ArrayIndexOutOfBoundsException if we request first index from statList before any readings have been stored.
    private static SpeedLightStats getMostRecentStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        int lastIndex = SearchForLight.statList.size() - 1;
        return SearchForLight.statList.get(lastIndex);
    }

    public static int getLeftLightAtStart() {
        SpeedLightStats firstEntry = SearchForLight.statList.get(0);
        return firstEntry.getLeftLightIntensity();
    }

    public static int getRightLightAtStart() {
        SpeedLightStats firstEntry = SearchForLight.statList.get(0);
        return firstEntry.getRightLightIntensity();
    }

    //method returns min int value if no current light readings
    public static int getHighestLeftLightReading() {
        return SearchForLight.leftLightSummary.getMax();
    }

    public static int getHighestRightLightReading() {
        return SearchForLight.rightLightSummary.getMax();
    }

    //method returns Max int val if no light readings
    public static int getLowestLeftLightReading() {
        return SearchForLight.leftLightSummary.getMin();
    }

    public static int getLowestRightLightReading() {
        return SearchForLight.rightLightSummary.getMin();
    }

    public static double getAverageLightSensorReading() {
        //left and right sensors SHOULD have same number of entries so simpler way of computing average is to just average the averages.
        //However, best to do proper way just in case and to provide protection against any future changes.
        long leftTotal = SearchForLight.leftLightSummary.getSum();
        long rightTotal = SearchForLight.rightLightSummary.getSum();

        long leftCount = SearchForLight.leftLightSummary.getCount();
        long rightCount = SearchForLight.rightLightSummary.getCount();

        return (double)(leftTotal + rightTotal)/(leftCount + rightCount);
    }

    public static double getAverageLeftLightSensorReading() {
        return SearchForLight.leftLightSummary.getAverage();
    }

    public static double getAverageRightLightSensorReading() {
        return SearchForLight.rightLightSummary.getAverage();
    }

    public static int getNumbLightDetections() {
        return SearchForLight.numbDetections;
    }

    public static FinchState getCurrentFinchState() throws NullPointerException, ArrayIndexOutOfBoundsException {
        return getMostRecentStats().getCurrState();
    }

    public static TimeInStates getTimeInEachState() {
        TimeInStates collectedStats = new TimeInStates();

        for (int i = 0; i<SearchForLight.statList.size()-1; i++) {
            SpeedLightStats currStats = SearchForLight.statList.get(i);
            SpeedLightStats nextStats = SearchForLight.statList.get(i+1);
            addTimeInState(currStats, nextStats, collectedStats);
        }
        return collectedStats;
    }

    private static void addTimeInState(SpeedLightStats currStats, SpeedLightStats nextStats, TimeInStates collectedStats) {
        FinchState state = currStats.getCurrState();
        long timeElapsed = nextStats.getTimestamp() - currStats.getTimestamp();
        collectedStats.addTime(timeElapsed, state);
    }

    private static SpeedLightStats getFirstStat() {
        return SearchForLight.statList.get(0);
    }

    //chosen to make individual methods here to ensure no extra calculations needed in the UI code.
    //Any conversion work to be done in this interface class.
    public static RawAndPecentage getLatestLeftVelStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getLeftWheelVelocity();
        int max = SearchForLight.MAX_WHEEL_VEL;
        int min = SearchForLight.MIN_WHEEL_VEL;

        return new RawAndPecentage(rawVal, max, min);
    }

    public static RawAndPecentage getLatestRightVelStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getRightWheelVelocity();
        int max = SearchForLight.MAX_WHEEL_VEL;
        int min = SearchForLight.MIN_WHEEL_VEL;

        return new RawAndPecentage(rawVal, max, min);
    }

    public static RawAndPecentage getLatestLeftLightStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getLeftLightIntensity();
        int max = SearchForLight.MAX_LIGHT_INTENSITY;
        int min = SearchForLight.MIN_LIGHT_INTENSITY;

        return new RawAndPecentage(rawVal, max, min);
        //could have chosen to just calc percentage in here, however would increase chance of error in terms of how we calc percentage.
        //i.e. 1 method might times by 100, the other may not.
    }

    public static RawAndPecentage getLatestRightLightStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getRightLightIntensity();
        int max = SearchForLight.MAX_LIGHT_INTENSITY;
        int min = SearchForLight.MIN_LIGHT_INTENSITY;

        return new RawAndPecentage(rawVal, max, min);
    }

    public static ArrayList<SpeedLightStats> getStats() {
        //deep copy created to ensure statList integrity.
        ArrayList<SpeedLightStats> copy = new ArrayList<>();
        SearchForLight.statList.forEach(s -> {
            copy.add(new SpeedLightStats(
                    s.getLeftLightIntensity(), s.getRightLightIntensity(),
                    s.getLeftWheelVelocity(), s.getRightWheelVelocity(),
                    s.getCurrState(),
                    s.getTimestamp()));
        });
        return copy;
    }
}
