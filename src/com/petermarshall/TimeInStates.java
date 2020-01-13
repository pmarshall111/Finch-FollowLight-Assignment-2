package com.petermarshall;

public class TimeInStates {
    private long followingTime;
    private long searchingTime;

    public void addTime(long t, FinchState state) {
        switch(state) {
            case FOLLOWING:
                followingTime += t;
                break;
            case SEARCH:
                searchingTime += t;
                break;
        }
    }

    public long getFollowingTime() {
        return followingTime;
    }

    public long getSearchingTime() {
        return searchingTime;
    }
}
