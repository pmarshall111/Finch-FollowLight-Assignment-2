package com.petermarshall;

public class SpeedLightStats {
    private final long timestamp;
    private final int[] lightIntensity;
    private final int[] wheelVelocity;

    public SpeedLightStats(int leftLight, int rightLight, int leftVel, int rightVel) {
        //TODO: can add max vals to velocities. so we can just add 510 to our distance if we go from -255 to 255.
        this.timestamp = System.nanoTime();
        this.lightIntensity = new int[]{leftLight, rightLight};
        this.wheelVelocity = new int[]{leftVel, rightVel};
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
}
