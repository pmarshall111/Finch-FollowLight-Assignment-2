package com.petermarshall.test;

import com.petermarshall.main.FinchState;
import com.petermarshall.main.TimeInStates;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeInStatesTest {
    private TimeInStates time;

    @Before
    public void setup() {
        time = new TimeInStates();
    }

    @Test
    public void timeStartsAtZero() {
        assertEquals(0, time.getFollowingTime());
        assertEquals(0, time.getSearchingTime());
        assertEquals(0, time.getWaitingTime());
        assertEquals(0, time.getTotalRecordedTime());
    }

    @Test
    public void getFollowingTime() {
        long toAdd = 100;
        time.addTime(toAdd, FinchState.FOLLOWING);
        assertEquals(toAdd, time.getFollowingTime());
        assertEquals(0, time.getSearchingTime());
        assertEquals(0, time.getWaitingTime());
        assertEquals(toAdd, time.getTotalRecordedTime());

        long toAdd2 = 299;
        time.addTime(toAdd2, FinchState.FOLLOWING);
        assertEquals(toAdd + toAdd2, time.getFollowingTime());
        assertEquals(0, time.getSearchingTime());
        assertEquals(0, time.getWaitingTime());
        assertEquals(toAdd + toAdd2, time.getTotalRecordedTime());
    }

    @Test
    public void getSearchingTime() {
        long toAdd = 100;
        time.addTime(toAdd, FinchState.SEARCH);
        assertEquals(0, time.getFollowingTime());
        assertEquals(toAdd, time.getSearchingTime());
        assertEquals(0, time.getWaitingTime());
        assertEquals(toAdd, time.getTotalRecordedTime());

        long toAdd2 = 299;
        time.addTime(toAdd2, FinchState.SEARCH);
        assertEquals(0, time.getFollowingTime());
        assertEquals(toAdd + toAdd2, time.getSearchingTime());
        assertEquals(0, time.getWaitingTime());
        assertEquals(toAdd + toAdd2, time.getTotalRecordedTime());
    }

    @Test
    public void getWaitingTime() {
        long toAdd = 100;
        time.addTime(toAdd, FinchState.WAITING_TO_BE_LEVEL);
        assertEquals(0, time.getFollowingTime());
        assertEquals(0, time.getSearchingTime());
        assertEquals(toAdd, time.getWaitingTime());
        assertEquals(toAdd, time.getTotalRecordedTime());

        long toAdd2 = 299;
        time.addTime(toAdd2, FinchState.WAITING_TO_BE_LEVEL);
        assertEquals(0, time.getFollowingTime());
        assertEquals(0, time.getSearchingTime());
        assertEquals(toAdd + toAdd2, time.getWaitingTime());
        assertEquals(toAdd + toAdd2, time.getTotalRecordedTime());
    }

    @Test
    public void getTotalRecordedTime() {
        long toAddFoll = 100, toAddSearch = 162, toAddWait = 8;
        time.addTime(toAddFoll, FinchState.FOLLOWING);
        time.addTime(toAddSearch, FinchState.SEARCH);
        time.addTime(toAddWait, FinchState.WAITING_TO_BE_LEVEL);
        assertEquals(toAddFoll + toAddSearch + toAddWait, time.getTotalRecordedTime());
    }
}
