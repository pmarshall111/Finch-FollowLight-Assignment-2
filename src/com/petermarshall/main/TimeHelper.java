package com.petermarshall.main;

public class TimeHelper {
    public static boolean fourSecondsElapsed(long prevTime) {
        return xSecondsPassed(prevTime, 4);
    }

    public static boolean xSecondsPassed(long prevTime, double xSeconds) {
        long currTime = System.nanoTime();
        long nanoSecs = (long) (xSeconds*Math.pow(10,9));
        return currTime - prevTime > nanoSecs;
    }
}
