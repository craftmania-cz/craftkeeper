package cz.craftmania.craftkeeper.events;

import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerSellallEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private @Getter KeeperPlayer keeperPlayer;
    private @Getter double amount;

    public PlayerSellallEvent(KeeperPlayer keeperPlayer, double amount) {
        this.keeperPlayer = keeperPlayer;
        this.amount = amount;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
