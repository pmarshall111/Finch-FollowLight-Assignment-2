package com.petermarshall;

import java.math.BigDecimal;

public class RawAndPecentage {
    private int raw;
    private double percentage;
    private double posNegPercentage;

    RawAndPecentage(int raw, int max, int min) {
        this.raw = raw;
        this.percentage = convertRawToPerc(raw, max, min);
        if (max > 0 && min < 0) {
            this.posNegPercentage = convertRawToPosNegPerc(raw, max, min);
        } else {
            this.posNegPercentage = this.percentage;
        }
    }

    public int getPercentage() {
        return (int) percentage;
    }

    public int getRaw() {
        return raw;
    }

    public int getPosNegPercentage() {
        return (int) posNegPercentage;
    }

    //does not deal with situations where min is > 0
    private static double convertRawToPerc(int raw, int max, int min) {
        if (min < 0) {
            raw += Math.abs(min);
            max += Math.abs(min);
            min = 0;
        }

        return 100d*raw / (min+max);
    }

    private static double convertRawToPosNegPerc(int raw, int max, int min) {
        if (raw >= 0) {
            return 100d*raw / max;
        } else {
            return -100d*raw / min;
        }
    }
}
