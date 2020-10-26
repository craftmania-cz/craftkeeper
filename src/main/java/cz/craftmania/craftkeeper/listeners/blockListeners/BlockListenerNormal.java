package cz.craftmania.craftkeeper.listeners.blockListeners;

import cz.craftmania.craftkeeper.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListenerNormal implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Main.getAutosellManager().handleBlockBreakEvent(event);
    }
}
