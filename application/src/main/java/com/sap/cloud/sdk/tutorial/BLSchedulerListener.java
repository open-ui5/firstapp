package com.sap.cloud.sdk.tutorial;


import org.quartz.Trigger;
import org.quartz.listeners.SchedulerListenerSupport;

public class BLSchedulerListener extends SchedulerListenerSupport {

    @Override
    public void schedulerStarted() {
        // do something with the event
    }

    @Override
    public void schedulerShutdown() {
        // do something with the event
    }

    @Override
    public void jobScheduled(Trigger trigger) {
        // do something with the event
    }

}