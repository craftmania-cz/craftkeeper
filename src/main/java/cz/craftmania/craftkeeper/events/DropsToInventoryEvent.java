package cz.craftmania.craftkeeper.events;

import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DropsToInventoryEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private @Getter KeeperPlayer keeperPlayer;
    private @Getter List<ItemStack> drops;
    private @Getter Block block;

    public DropsToInventoryEvent(KeeperPlayer keeperPlayer, List<ItemStack> drops, Block block) {
        this.keeperPlayer = keeperPlayer;
        this.drops = drops;
        this.block = block;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
