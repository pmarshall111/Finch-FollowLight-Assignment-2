package com.petermarshall.main;

import edu.cmu.ri.createlab.terk.robot.finch.Finch;
import java.util.ArrayList;
import static com.petermarshall.main.FinchLimits.*;

//The interface to the search for light task from any other project. No other classes should be needed. Class extends @Thread
//so that the application can be run in another thread. Important as this program uses constant while loops until stopped, so if
//this application was called from a GUI without creating a new thread, it would block the GUI thread and the GUI would become
//unresponsive.
public class LightInterfaceThread extends Thread {
    private Finch sharedFinch;
    private SearchForLight program;

    @Override
    public void run() {
        program = new SearchForLight(sharedFinch);
        program.startProgram();
    }

    //finch is passed in as a variable and not instantiated here to speed up the time it takes for the task to start.
    public LightInterfaceThread(Finch finch) {
        sharedFinch = finch;
    }

    public void stopProgram() {
        program.stop();
    }

    public boolean isRunning() {
        return program.isRunning();
    }

    //throws NullPointerException just in case we request the statList before the run has started.
    //throws ArrayIndexOutOfBoundsException if we request first index from statList before any readings have been stored.
    private SpeedLightStats getMostRecentStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        int lastIndex = program.getStatList().size() - 1;
        return program.getStatList().get(lastIndex);
    }

    public long getTimeElapsedInNS() throws NullPointerException, ArrayIndexOutOfBoundsException {
        return getMostRecentStats().getTimestamp() - program.getScriptStartTime();
    }

    public int getLeftLightAtStart() {
        SpeedLightStats firstEntry = program.getStatList().get(0);
        return firstEntry.getLeftLightIntensity();
    }

    public int getRightLightAtStart() {
        SpeedLightStats firstEntry = program.getStatList().get(0);
        return firstEntry.getRightLightIntensity();
    }

    //method returns min int value if no current light readings
    public int getHighestLeftLightReading() {
        return program.getLeftLightSummary().getMax();
    }
    public int getHighestRightLightReading() {
        return program.getRightLightSummary().getMax();
    }

    //method returns Max int val if no light readings
    public int getLowestLeftLightReading() {
        return program.getLeftLightSummary().getMin();
    }
    public int getLowestRightLightReading() {
        return program.getRightLightSummary().getMin();
    }

    public double getAverageLightSensorReading() {
        long leftTotal = program.getLeftLightSummary().getSum();
        long rightTotal = program.getRightLightSummary().getSum();
        long leftCount = program.getLeftLightSummary().getCount();
        long rightCount = program.getRightLightSummary().getCount();
        return (double)(leftTotal + rightTotal)/(leftCount + rightCount);
    }

    public double getAverageLeftLightSensorReading() {
        return program.getLeftLightSummary().getAverage();
    }

    public double getAverageRightLightSensorReading() {
        return program.getRightLightSummary().getAverage();
    }

    public int getNumbLightDetections() {
        return program.getNumbDetections();
    }

    public FinchState getCurrentFinchState() throws NullPointerException, ArrayIndexOutOfBoundsException {
        return getMostRecentStats().getCurrState();
    }

    public TimeInStates getTimeInEachState() {
        TimeInStates collectedStats = new TimeInStates();
        for (int i = 0; i<program.getStatList().size()-1; i++) {
            SpeedLightStats currStats = program.getStatList().get(i);
            SpeedLightStats nextStats = program.getStatList().get(i+1);
            addTimeInState(currStats, nextStats, collectedStats);
        }
        return collectedStats;
    }

    private void addTimeInState(SpeedLightStats currStats, SpeedLightStats nextStats, TimeInStates collectedStats) {
        FinchState state = currStats.getCurrState();
        long timeElapsed = nextStats.getTimestamp() - currStats.getTimestamp();
        collectedStats.addTime(timeElapsed, state);
    }

    private SpeedLightStats getFirstStat() {
        return program.getStatList().get(0);
    }

    public RawAndPecentage getLatestLeftVelStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getLeftWheelVelocity();
        int max = MAX_WHEEL_VEL;
        int min = MIN_WHEEL_VEL;

        return new RawAndPecentage(rawVal, max, min);
    }

    public RawAndPecentage getLatestRightVelStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getRightWheelVelocity();
        int max = MAX_WHEEL_VEL;
        int min = MIN_WHEEL_VEL;

        return new RawAndPecentage(rawVal, max, min);
    }

    public RawAndPecentage getLatestLeftLightStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getLeftLightIntensity();
        int max = MAX_LIGHT_INTENSITY;
        int min = MIN_LIGHT_INTENSITY;

        return new RawAndPecentage(rawVal, max, min);
    }

    public RawAndPecentage getLatestRightLightStats() throws NullPointerException, ArrayIndexOutOfBoundsException {
        SpeedLightStats stats = getMostRecentStats();
        int rawVal = stats.getRightLightIntensity();
        int max = MAX_LIGHT_INTENSITY;
        int min = MIN_LIGHT_INTENSITY;

        return new RawAndPecentage(rawVal, max, min);
    }

    //Returns a deep copy to ensure statList integrity when used by outside sources.
    public ArrayList<SpeedLightStats> getStats() {
        ArrayList<SpeedLightStats> copy = new ArrayList<>();
        program.getStatList().forEach(s -> {
            copy.add(new SpeedLightStats(
                    s.getLeftLightIntensity(), s.getRightLightIntensity(),
                    s.getLeftWheelVelocity(), s.getRightWheelVelocity(),
                    s.getCurrState(),
                    s.getTimestamp()));
        });
        return copy;
    }
}
