package com.petermarshall.main;

//Class is used to package light and wheel values from the Finch as percentages and raw values. 2 different types of percentages
//included, percentage is the percentage of the way from the MIN val to the MAX val. posNegPercentage is only different from percentage
//if the range from MIN to MAX crosses 0 i.e. -255 to 255. In this case, posNegPercentage will give the percentage from 0 to the MIN/MAX
//value, with a raw val less than 0 giving a negative percentage.
//class will correct errors in passing in MIN and MAX values to constructor in the wrong order, and will limit any raw value that is bigger
//or lower than the MAX/MIN value to the MAX/MIN value.
public class RawAndPecentage {
    private int raw;
    private double percentage;
    private double posNegPercentage;

    public RawAndPecentage(int raw, int max, int min) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        if (raw > max) {
            raw = max;
        } else if (raw < min) {
            raw = min;
        }

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
