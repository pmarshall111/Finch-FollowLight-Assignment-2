package com.petermarshall.test;

import com.petermarshall.main.*;
import edu.cmu.ri.createlab.terk.robot.finch.Finch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
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
        interfaceThread.stopProgram();
        finch.quit();
    }

    @Test
    public void canStartProgram() {
        startInterfaceThread();
        sleep(500);
        assertTrue(interfaceThread.isRunning());
    }

    @Test
    public void canStopProgram() {
        startInterfaceThread();
        sleep(500);
        interfaceThread.stopProgram();
        sleep(500);
        assertFalse(interfaceThread.isRunning());
    }

    @Test
    public void canGetTimeElapsed() {
        long startTime = System.nanoTime();
        startInterfaceThread();
        sleep(2000);
        long endTime = System.nanoTime();
        long errorInNs = 50000000; //50ms
        assertEquals(endTime-startTime, interfaceThread.getTimeElapsedInNS(), errorInNs);
    }

    @Test
    public void canGetLeftLightAtStart() {
        startInterfaceThread();
        int finchLeftLight = finch.getLeftLightSensor();
        assertEquals(finchLeftLight, interfaceThread.getLeftLightAtStart(), 5);
    }

    @Test
    public void canGetRightLightAtStart() {
        startInterfaceThread();
        int finchRightLight = finch.getRightLightSensor();
        assertEquals(finchRightLight, interfaceThread.getRightLightAtStart(), 5);
    }

    @Test
    public void canGetHighestLeftReading() {
        startInterfaceThread();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        long prevTime = System.nanoTime();
        while (!TimeHelper.xSecondsPassed(prevTime, 10)) {
            stats.accept(finch.getLeftLightSensor());
        }
        assertEquals(stats.getMax(), interfaceThread.getHighestLeftLightReading(), 5);
    }

    @Test
    public void canGetHighestRightReading() {
        startInterfaceThread();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        long prevTime = System.nanoTime();
        while (!TimeHelper.xSecondsPassed(prevTime, 10)) {
            stats.accept(finch.getRightLightSensor());
        }
        assertEquals(stats.getMax(), interfaceThread.getHighestRightLightReading(), 5);
    }

    @Test
    public void canGetLowestLeftReading() {
        startInterfaceThread();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        long prevTime = System.nanoTime();
        while (!TimeHelper.xSecondsPassed(prevTime, 10)) {
            stats.accept(finch.getLeftLightSensor());
        }
        assertEquals(stats.getMin(), interfaceThread.getLowestLeftLightReading(), 5);
    }

    @Test
    public void canGetLowestRightReading() {
        startInterfaceThread();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        long prevTime = System.nanoTime();
        while (!TimeHelper.xSecondsPassed(prevTime, 10)) {
            stats.accept(finch.getRightLightSensor());
        }
        assertEquals(stats.getMin(), interfaceThread.getLowestRightLightReading(), 5);
    }

    @Test
    public void canGetAvgLeftReading() {
        startInterfaceThread();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        long prevTime = System.nanoTime();
        while (!TimeHelper.xSecondsPassed(prevTime, 10)) {
            stats.accept(finch.getLeftLightSensor());
        }
        assertEquals(stats.getAverage(), interfaceThread.getAverageLeftLightSensorReading(), 5);
    }

    @Test
    public void canGetAvgRightReading() {
        startInterfaceThread();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        long prevTime = System.nanoTime();
        while (!TimeHelper.xSecondsPassed(prevTime, 10)) {
            stats.accept(finch.getRightLightSensor());
        }
        assertEquals(stats.getAverage(), interfaceThread.getAverageRightLightSensorReading(), 5);
    }

    @Test
    public void canGetAvgOvrReading() {
        startInterfaceThread();
        IntSummaryStatistics stats = new IntSummaryStatistics();
        long prevTime = System.nanoTime();
        while (!TimeHelper.xSecondsPassed(prevTime, 10)) {
            stats.accept(finch.getLeftLightSensor());
            stats.accept(finch.getRightLightSensor());
        }
        assertEquals(stats.getAverage(), interfaceThread.getAverageLightSensorReading(), 5);
    }

    @Test
    public void canGetListOfStats() {
        startInterfaceThread();
        long prevTime = System.nanoTime();
        while (!TimeHelper.xSecondsPassed(prevTime, 10)) {
            //do nothing
        }
        ArrayList<SpeedLightStats> stats = interfaceThread.getStats();
        SpeedLightStats firstStat = stats.get(0), lastStat = stats.get(stats.size()-1);
        long timeElapsed = lastStat.getTimestamp() - firstStat.getTimestamp();
        double timeInSecs = timeElapsed/Math.pow(10,9);
        assertNotNull(stats);
        assertTrue(stats.size() > 100);
        assertEquals(10d, timeInSecs, 0.5); //comparison in secs
    }

    //sleep used to give Java chance to set up and start new thread before continuing steps of test.
    private void startInterfaceThread() {
        interfaceThread.start();
        sleep(200);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}