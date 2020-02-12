package com.petermarshall.main;

public class SpeedLightStats {
    private final long timestamp;
    private final int[] lightIntensity;
    private final int[] wheelVelocity;
    private final FinchState currState;

    public SpeedLightStats(int leftLight, int rightLight, int leftVel, int rightVel, FinchState state, long timestamp) {
        this.timestamp = timestamp;
        this.lightIntensity = new int[]{leftLight, rightLight};
        this.wheelVelocity = new int[]{leftVel, rightVel};
        this.currState = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getLeftLightIntensity() {
        return lightIntensity[0];
    }

    public int getRightLightIntensity() {
        return lightIntensity[1];
    }

    public int getLeftWheelVelocity() {
        return wheelVelocity[0];
    }

    public int getRightWheelVelocity() {
        return wheelVelocity[1];
    }

    public FinchState getCurrState() {
        return currState;
    }
}
