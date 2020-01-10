package com.petermarshall;

public class RawAndPecentage {
    private double percentage;
    private int raw;

    RawAndPecentage(int raw, int max, int min) {
        this.percentage = convertRawToPerc(raw, max, min);
        this.raw = raw;
    }

    RawAndPecentage(double percentage, int raw) {
        this.percentage = percentage;
        this.raw = raw;
    }

    public double getPercentage() {
        return percentage;
    }

    public int getRaw() {
        return raw;
    }

    //Only deals with situations where min and raw are < 0
    private static double convertRawToPerc(int raw, int max, int min) {
        if (min < 0) {
            raw += Math.abs(min);
            min = 0;
            max += Math.abs(min);
        }

        return 100*raw / ((double)(min+max));
    }
}
