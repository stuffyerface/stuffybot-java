package me.stuffy.stuffybot.events;

import me.stuffy.stuffybot.utils.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseEvent {
    private String name;
    private long interval;
    private TimeUnit timeUnit;
    private ScheduledExecutorService scheduler;

    public BaseEvent(String name, long interval, TimeUnit timeUnit) {
        this.name = name;
        this.interval = interval;
        this.timeUnit = timeUnit;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        Logger.log("<Startup> Scheduled " + name + " event to run every " + interval + " " + timeUnit.toString().toLowerCase());
    }

    public void startFixedRateEvent() {
        scheduler.scheduleAtFixedRate(this::execute, interval, interval, timeUnit);
    }

    protected abstract void execute();

    public void stopEvent() {
        scheduler.shutdown();
    }

}
