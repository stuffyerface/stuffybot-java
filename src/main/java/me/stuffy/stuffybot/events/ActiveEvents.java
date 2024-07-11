package me.stuffy.stuffybot.events;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActiveEvents extends BaseEvent{
    public ActiveEvents() {
        super("ActiveEvents", 1, TimeUnit.MINUTES);
    }

    @Override
    protected void execute() {
        // Blitz Hour (Every 4 hours)
        // Pit Day (Feb 26 5:00 AM GMT - Feb 27 5:00 AM GMT)
        // Dream Mode (Friday 12:00 AM EST, breaks in October)
        // Pit Map (

        List<String> pitMaps = List.of("Castle", "Corals", "Genesis", "Four Seasons", "Elements");
        List<String> dreamModes = List.of("Rush", "Ultimate", "Castle", "Voidless", "Armed", "Lucky Block", "Swappage");

        long currentTimestamp = System.currentTimeMillis();
        String day = new java.text.SimpleDateFormat("dd").format(new java.util.Date(currentTimestamp));
    }
}
