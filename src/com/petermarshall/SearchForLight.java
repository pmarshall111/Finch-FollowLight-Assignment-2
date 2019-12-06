package com.petermarshall;

import com.github.freva.asciitable.AsciiTable;
import edu.cmu.ri.createlab.terk.robot.finch.Finch;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.concurrent.TimeUnit;

public class SearchForLight {

    private static Finch finch;
    private static boolean finchFollowing;

    private static ArrayList<Integer> lightReadings;
    private static long scriptStartTime;
    private static int numbDetections;

    private static final int MIN_LIGHT_INTENSITY = 80;


    public static void start(Finch sharedFinch) {
        finch = sharedFinch;

        waitForFinchToBeLevel();
        init();
        detectLight();
    }

    private static void waitForFinchToBeLevel() {
        //TODO: could probably use a timer in here so that the finch has to be level for a period of time before starting. Otherwise could errantly start.
        while (finchIsntLevel()) {
            //do nothing
        }
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

        finch.setWheelVelocities(50,50); //max values +/-255
        finch.setLED(Color.YELLOW);
        finchFollowing = false;
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
                //if we can't find any light and 4 seconds haven't passed, we keep looking. no action required.
            }

            recordLightReadings();
        }

        showStats();
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
    }

    private static void finchFollow() {
        if (!finchFollowing) {
            finchFollowing = true;
            numbDetections++;
            finch.setLED(Color.RED);
        }

        if (finch.getLeftLightSensor() > finch.getRightLightSensor()) {
            finch.setWheelVelocities(50,100);
        } else if (finch.getLeftLightSensor() < finch.getRightLightSensor()) {
            finch.setWheelVelocities(100,50);
        } else {
            finch.setWheelVelocities(50,50);
        }
    }

    private static void finchSearch() {
        finchFollowing = false;
        finch.setLED(Color.YELLOW);
        finch.setWheelVelocities(0,0);
        sleep(500);

        int multiplier = (int) Math.round(Math.random());
        finch.setWheelVelocities(multiplier * 50, Math.abs(multiplier-1) * 50, 3000); //values to be played around with to get roughly 90degs.
        finch.setWheelVelocities(50,50);
    }

    private static boolean fourSecondsElapsed(long prevTime) {
        long currTime = System.nanoTime();
        long fourNanoSecs = (long) (4*Math.pow(10,9));
        return currTime - prevTime > fourNanoSecs;
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
                {"Script duration", String.format("%1d %2ds", minutes, seconds)},
                {"Numb detections", numbDetections+""}
        };

        return AsciiTable.getTable(rows);
    }
}
