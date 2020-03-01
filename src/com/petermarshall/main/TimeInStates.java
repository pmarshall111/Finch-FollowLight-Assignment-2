package com.petermarshall.main;

//Class is used to count time based on the state the Finch is in.
public class TimeInStates {
    private long followingTime;
    private long searchingTime;
    private long waitingTime;

    public void addTime(long duration, FinchState state) {
        switch(state) {
            case FOLLOWING:
                followingTime += duration;
                break;
            case SEARCH:
                searchingTime += duration;
                break;
            case WAITING_TO_BE_LEVEL:
                waitingTime += duration;

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

    public long getTotalRecordedTime() {
        return followingTime + searchingTime + waitingTime;
    }
}
