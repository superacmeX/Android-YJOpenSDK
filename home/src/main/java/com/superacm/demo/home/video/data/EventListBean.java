package com.superacm.demo.home.video.data;

import java.util.Arrays;

public class EventListBean {
    private String nextToken;
    private EventDetailBean[] eventQueryRespDTOs;

    public String getNextToken() {
        return nextToken;
    }

    public void setNextToken(String nextToken) {
        this.nextToken = nextToken;
    }

    public EventDetailBean[] getEventQueryRespDTOs() {
        return eventQueryRespDTOs;
    }

    public void setEventQueryRespDTOs(EventDetailBean[] eventQueryRespDTOs) {
        this.eventQueryRespDTOs = eventQueryRespDTOs;
    }

    @Override
    public String toString() {
        return "EventListBean{" +
                "nextToken='" + nextToken + '\'' +
                ", eventQueryRespDTOs=" + Arrays.toString(eventQueryRespDTOs) +
                '}';
    }
}
