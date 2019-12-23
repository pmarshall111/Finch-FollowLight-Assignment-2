package com.petermarshall;

public enum FinchState {
    FOLLOWING,
    KEEP_DISTANCE,
    SEARCH;

    public static FinchState swapFollowAndKeepDist(FinchState state) {
        if (state.equals(FinchState.FOLLOWING)) {
            return FinchState.KEEP_DISTANCE;
        } else if (state.equals(FinchState.KEEP_DISTANCE)) {
            return FinchState.FOLLOWING;
        } else {
            return state;
        }
    }
}
