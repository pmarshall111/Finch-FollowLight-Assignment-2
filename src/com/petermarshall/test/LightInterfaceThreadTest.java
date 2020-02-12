package com.petermarshall.test;

import com.petermarshall.main.FinchState;
import com.petermarshall.main.LightInterfaceThread;
import com.petermarshall.main.TimeHelper;
import com.petermarshall.main.TimeInStates;
import edu.cmu.ri.createlab.terk.robot.finch.Finch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.IntSummaryStatistics;

import static org.junit.Assert.*;

public class LightInterfaceThreadTest {
    private Finch finch;
    private LightInterfaceThread interfaceThread;

    @Before
    public void setup() {
        finch = new Finch();
        interfaceThread = new LightInterfaceThread(finch);
    }

    @After
    public void tearDown() {
        finch.quit();
    }

    @Test
    public void canStartProgram() {
        assertFalse(interfaceThread.isRunning());
        interfaceThread.start();
        assertTrue(interfaceThread.isRunning());
    }

    @Test
    public void canStopProgram() {
        interfaceThread.start();
        interfaceThread.stopProgram();
        assertFalse(interfaceThread.isRunning());
    }

    @Test
    public void timeElapsedStartsAtZero() {
        assertEquals(0, interfaceThread.getTimeElapsedInNS());
    }

    //TODO: add a bunch of tests such that if called before it's started, the result is 0.

    @Test
    public void canGetTimeElapsed() {
        long startTime = System.nanoTime();
        interfaceThread.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        long errorInNs = 500;
        assertEquals(endTime-startTime, interfaceThread.getTimeElapsedInNS(), errorInNs);
    }

    @Test
    public void canGetLeftLightAtStart() {
        interfaceThread.start();
        int finchLeftLight = finch.getLeftLightSensor();
        assertEquals(finchLeftLight, interfaceThread.getLeftLightAtStart(), 1);
    }

    @Test
    public void canGetRightLightAtStart() {
        interfaceThread.start();
        int finchRightLight = finch.getRightLightSensor();
        assertEquals(finchRightLight, interfaceThread.getRightLightAtStart(), 1);
    }

    @Test
    public void canGetHighestLeftReading() {
        interfaceThread.start();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        while (!TimeHelper.xSecondsPassed(System.nanoTime(), 10)) {
            stats.accept(finch.getLeftLightSensor());
        }
        assertEquals(stats.getMax(), interfaceThread.getHighestLeftLightReading(), 1);
    }

    @Test
    public void canGetHighestRightReading() {
        interfaceThread.start();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        while (!TimeHelper.xSecondsPassed(System.nanoTime(), 10)) {
            stats.accept(finch.getRightLightSensor());
        }
        assertEquals(stats.getMax(), interfaceThread.getHighestRightLightReading(), 1);
    }

    @Test
    public void canGetLowestLeftReading() {
        interfaceThread.start();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        while (!TimeHelper.xSecondsPassed(System.nanoTime(), 10)) {
            stats.accept(finch.getLeftLightSensor());
        }
        assertEquals(stats.getMin(), interfaceThread.getLowestLeftLightReading(), 1);
    }

    @Test
    public void canGetLowestRightReading() {
        interfaceThread.start();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        while (!TimeHelper.xSecondsPassed(System.nanoTime(), 10)) {
            stats.accept(finch.getRightLightSensor());
        }
        assertEquals(stats.getMin(), interfaceThread.getLowestRightLightReading(), 1);
    }

    @Test
    public void canGetAvgLeftReading() {
        interfaceThread.start();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        while (!TimeHelper.xSecondsPassed(System.nanoTime(), 10)) {
            stats.accept(finch.getLeftLightSensor());
        }
        assertEquals(stats.getAverage(), interfaceThread.getAverageLeftLightSensorReading(), 1);
    }

    @Test
    public void canGetAvgRightReading() {
        interfaceThread.start();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        while (!TimeHelper.xSecondsPassed(System.nanoTime(), 10)) {
            stats.accept(finch.getRightLightSensor());
        }
        assertEquals(stats.getAverage(), interfaceThread.getAverageRightLightSensorReading(), 1);
    }

    @Test
    public void canGetAvgOvrReading() {
        interfaceThread.start();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        while (!TimeHelper.xSecondsPassed(System.nanoTime(), 10)) {
            stats.accept(finch.getLeftLightSensor());
            stats.accept(finch.getRightLightSensor());
        }
        assertEquals(stats.getAverage(), interfaceThread.getAverageLightSensorReading(), 1);
    }

    //TODO: manual test needed for can get number of detections. need to go and shine light and count.

    //currentFinchState don't know how to test. manual test needed?

    //probably needs a manual intervention such that a light is found. or alternatively a longer test period and hope that it does find a light.
    @Test
    public void canGetTimeInEachState() {
        interfaceThread.start();
        TimeInStates time = new TimeInStates();
        long prevTime = System.nanoTime();
        FinchState currState = interfaceThread.getCurrentFinchState();
        while (!TimeHelper.xSecondsPassed(System.nanoTime(), 10)) {
            time.addTime(System.nanoTime() - prevTime, currState);
            currState = interfaceThread.getCurrentFinchState();
            prevTime = System.nanoTime();
        }
        TimeInStates interfaceTime = interfaceThread.getTimeInEachState();

        assertEquals(time.getFollowingTime(), interfaceTime.getFollowingTime(), 500);
        assertEquals(time.getSearchingTime(), interfaceTime.getSearchingTime(), 500);
        assertEquals(time.getWaitingTime(), interfaceTime.getWaitingTime(), 500);
        assertEquals(time.getTotalRecordedTime(), interfaceTime.getTotalRecordedTime(), 1500);
    }


    //final tests are to get latest stats for each bit and also to get stats array in general
}