package com.petermarshall;

import edu.cmu.ri.createlab.terk.robot.finch.Finch;

import java.util.ArrayList;

public class LightInterfaceThread extends Thread {

    private static Finch sharedFinch;

    @Override
    public void run() {
        //need to reinitialise the Finch each time else we have trouble connect
//        sharedFinch = new Finch();
        SearchForLight.END_RUN = false;
        SearchForLight.start(sharedFinch);
    }
    

    public static void init(Finch finch) {
        //separated to try speed up starting of the task when user clicks on it.
        sharedFinch = finch;
    }

    public static void startSearch() {
        SearchForLight.END_RUN = false;
        sharedFinch = new Finch();
        SearchForLight.start(sharedFinch);
    }

    public void stopProgram() {
        SearchForLight.END_RUN = true;
//        sharedFinch.quit();
    }

    //TODO: will also need methods to get the live data out to the UI

    public static long getTimeElapsedInNS() throws NullPointerException {
        return SearchForLight.scriptStartTime - getMostRecentStats().getTimestamp();
    }

    //throws NullPointerException just in case we request the stats before the run has started.
    private static SpeedLightStats getMostRecentStats() throws NullPointerException {
        int lastIndex = SearchForLight.stats.size() - 1;
        return SearchForLight.stats.get(lastIndex);
    }

    public static int getLeftLightAtStart() {
        SpeedLightStats firstEntry = SearchForLight.stats.get(0);
        return firstEntry.getLeftLightIntensity();
    }

    public static int getRightLightAtStart() {
        SpeedLightStats firstEntry = SearchForLight.stats.get(0);
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

    public static FinchState getCurrentFinchState() throws NullPointerException {
        return getMostRecentStats().getCurrState();
    }

    public static TimeInStates getTimeInEachState() {
        TimeInStates collectedStats = new TimeInStates();
        long lastTimestamp = getFirstStat().getTimestamp();

        for (int i = 1; i<SearchForLight.stats.size(); i++) {
            SpeedLightStats currStats = SearchForLight.stats.get(i);
            addTimeInState(lastTimestamp, currStats, collectedStats);
            lastTimestamp = currStats.getTimestamp();
        }
        return collectedStats;
    }

    private static void addTimeInState(long lastTimestamp, SpeedLightStats stats, TimeInStates collectedStats) {
        FinchState state = stats.getCurrState();
        long timeElapsed = stats.getTimestamp() - lastTimestamp;
        collectedStats.addTime(timeElapsed, state);
    }

    private static SpeedLightStats getFirstStat() {
        return SearchForLight.stats.get(0);
    }

    //chosen to make individual methods here to ensure no extra calculations needed in the UI code.
    //Any conversion work to be done in this interface class.
    public static RawAndPecentage getLatestLeftVelStats() throws NullPointerException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getLeftWheelVelocity();
        int max = SearchForLight.MAX_WHEEL_VEL;
        int min = SearchForLight.MIN_WHEEL_VEL;

        return new RawAndPecentage(rawVal, max, min);
    }

    public static RawAndPecentage getLatestRightVelStats() throws NullPointerException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getRightWheelVelocity();
        int max = SearchForLight.MAX_WHEEL_VEL;
        int min = SearchForLight.MIN_WHEEL_VEL;

        return new RawAndPecentage(rawVal, max, min);
    }

    public static RawAndPecentage getLatestLeftLightStats() throws NullPointerException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getLeftLightIntensity();
        int max = SearchForLight.MAX_LIGHT_INTENSITY;
        int min = SearchForLight.MIN_LIGHT_INTENSITY;

        return new RawAndPecentage(rawVal, max, min);
        //could have chosen to just calc percentage in here, however would increase chance of error in terms of how we calc percentage.
        //i.e. 1 method might times by 100, the other may not.
    }

    public static RawAndPecentage getLatestRightLightStats() throws NullPointerException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getRightLightIntensity();
        int max = SearchForLight.MAX_LIGHT_INTENSITY;
        int min = SearchForLight.MIN_LIGHT_INTENSITY;

        return new RawAndPecentage(rawVal, max, min);
    }

    public static ArrayList<SpeedLightStats> getStats() {
        //deep copy created to ensure stats integrity.
        ArrayList<SpeedLightStats> copy = new ArrayList<>();
        SearchForLight.stats.forEach(s -> {
            copy.add(new SpeedLightStats(
                    s.getLeftLightIntensity(), s.getRightLightIntensity(),
                    s.getLeftWheelVelocity(), s.getRightWheelVelocity(),
                    s.getCurrState(),
                    s.getTimestamp()));
        });
        return copy;
    }
}
