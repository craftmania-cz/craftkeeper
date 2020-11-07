package cz.craftmania.craftkeeper.events;

import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerAutosellStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private @Getter KeeperPlayer keeperPlayer;

    public PlayerAutosellStartEvent(KeeperPlayer keeperPlayer) {
        this.keeperPlayer = keeperPlayer;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
