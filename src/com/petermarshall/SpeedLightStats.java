package com.petermarshall;

public class SpeedLightStats {
    private final long timestamp;
    private final int[] lightIntensity;
    private final int[] wheelVelocity;
    private final FinchState currState;

    public SpeedLightStats(int leftLight, int rightLight, int leftVel, int rightVel, FinchState state) {
        //TODO: can add max vals to velocities. so we can just add 510 to our distance if we go from -255 to 255.
        this.timestamp = System.nanoTime();
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
