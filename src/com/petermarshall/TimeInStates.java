package com.petermarshall;

public class TimeInStates {
    private long followingTime;
    private long searchingTime;
    private long waitingTime;

    public void addTime(long t, FinchState state) {
        switch(state) {
            case FOLLOWING:
                followingTime += t;
                break;
            case SEARCH:
                searchingTime += t;
                break;
            case WAITING_TO_BE_LEVEL:
                waitingTime += t;

        }
    }

    public long getFollowingTime() {
        return followingTime;
    }

    public long getSearchingTime() {
        return searchingTime;
    }

    public long getWaitingTime() {
        return waitingTime;
    }
}
