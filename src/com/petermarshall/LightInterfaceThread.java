package com.petermarshall;

import edu.cmu.ri.createlab.terk.robot.finch.Finch;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;

public class LightInterfaceThread extends Thread {

    private static Finch sharedFinch;

    @Override
    public void run() {
        //need to reinitialise the Finch each time else we have trouble connect
        sharedFinch.quit();
        sharedFinch = new Finch();
        SearchForLight.END_RUN = false;
        SearchForLight.start(sharedFinch);
    }

    public static void init(Finch finch) {
        //separated to try speed up starting of the task when user clicks on it.
        sharedFinch = finch;
    }

    public void startSearch() {
        SearchForLight.start(sharedFinch);
    }

    public void stopProgram() {
        SearchForLight.END_RUN = true;
    }

    //TODO: will also need methods to get the live data out to the UI

    public long getTimeElapsedInNS() {
        return SearchForLight.scriptStartTime - getMostRecentStats().getTimestamp();
    }

    public SpeedLightStats getMostRecentStats() {
        int lastIndex = SearchForLight.stats.size() - 1;
        return SearchForLight.stats.get(lastIndex);
    }

    public int getLeftLightAtStart() {
        SpeedLightStats firstEntry = SearchForLight.stats.get(0);
        return firstEntry.getLeftLightIntensity();
    }

    public int getRightLightAtStart() {
        SpeedLightStats firstEntry = SearchForLight.stats.get(0);
        return firstEntry.getRightLightIntensity();
    }

    //method returns -1 if no current light readings
    public int getHighestLightReading() {
        return SearchForLight.lightSummary.getMax();
    }

    //method returns -1 if no current light readings
    public int getLowestLightReading() {
        return SearchForLight.lightSummary.getMin();
    }

    public double getAverageLightSensorReading() {
        return SearchForLight.lightSummary.getAverage();
    }

    public int getNumbLightDetections() {
        return SearchForLight.numbDetections;
    }

    public FinchState getCurrentFinchState() {
        return getMostRecentStats().getCurrState();
    }

    public void getTimeInEachState() {
        StateStats collectedStats = new StateStats();
        long lastTimestamp = getFirstStat().getTimestamp();

        for (int i = 1; i<SearchForLight.stats.size(); i++) {
            SpeedLightStats currStats = SearchForLight.stats.get(i);
            addTimeInState(lastTimestamp, currStats, collectedStats);
            lastTimestamp = currStats.getTimestamp();
        }
    }

    private void addTimeInState(long lastTimestamp, SpeedLightStats stats, StateStats collectedStats) {
        FinchState state = stats.getCurrState();
        long timeElapsed = stats.getTimestamp() - lastTimestamp;
        collectedStats.addTime(timeElapsed, state);
    }

    private SpeedLightStats getFirstStat() {
        return SearchForLight.stats.get(0);
    }

    //chosen to make individual methods here to ensure no extra calculations needed in the UI code.
    //Any conversion work to be done in this interface class.
    public RawAndPecentage getLatestLeftVelStats() {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getLeftWheelVelocity();
        int max = SearchForLight.MAX_WHEEL_VEL;
        int min = SearchForLight.MIN_WHEEL_VEL;

        return new RawAndPecentage(rawVal, max, min);
    }

    public RawAndPecentage getLatestRightVelStats() {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getRightWheelVelocity();
        int max = SearchForLight.MAX_WHEEL_VEL;
        int min = SearchForLight.MIN_WHEEL_VEL;

        return new RawAndPecentage(rawVal, max, min);
    }

    public RawAndPecentage getLatestLeftLightStats() {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getLeftLightIntensity();
        int max = SearchForLight.MAX_LIGHT_INTENSITY;
        int min = SearchForLight.MIN_LIGHT_INTENSITY;

        return new RawAndPecentage(rawVal, max, min);
        //could have chosen to just calc percentage in here, however would increase chance of error in terms of how we calc percentage.
        //i.e. 1 method might times by 100, the other may not.
    }

    public RawAndPecentage getLatestRightLightStats() {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getRightLightIntensity();
        int max = SearchForLight.MAX_LIGHT_INTENSITY;
        int min = SearchForLight.MIN_LIGHT_INTENSITY;

        return new RawAndPecentage(rawVal, max, min);
    }
}
