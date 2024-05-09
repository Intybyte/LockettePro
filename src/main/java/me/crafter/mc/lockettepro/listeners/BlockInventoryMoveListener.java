package me.crafter.mc.lockettepro.listeners;

import me.crafter.mc.lockettepro.Config;
import me.crafter.mc.lockettepro.LocketteProAPI;
import me.crafter.mc.lockettepro.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Hopper;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BlockInventoryMoveListener implements Listener {
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        inventoryChecks(event);

        if (Config.isItemTransferInBlocked()) {
            if (isInventoryLocked(event.getDestination())) {
                event.setCancelled(true);
            }
        }
    }

    public void inventoryChecks(InventoryMoveItemEvent event) {
        byte hopperminecartaction = Config.getHopperMinecartAction();
        if (!Config.isItemTransferOutBlocked() && hopperminecartaction == (byte) 0) {
            return;
        }

        if (!isInventoryLocked(event.getSource())) {
            return;
        }

        if (Config.isItemTransferOutBlocked()) {
            event.setCancelled(true);
        }

        InventoryHolder destinationHolder = event.getDestination().getHolder();
        InventoryHolder sourceHolder = event.getDestination().getHolder();

        // Additional Hopper Minecart Check
        if (destinationHolder instanceof HopperMinecart) {
            switch (hopperminecartaction) {
                // case 0 - Impossible
                case (byte) 1: // Cancel only, it is not called if !Config.isItemTransferOutBlocked()
                    event.setCancelled(true);
                    break;
                case (byte) 2: // Extra action - HopperMinecart removal
                    event.setCancelled(true);
                    ((HopperMinecart) destinationHolder).remove();
                    break;
            }
            return;
        }

        if (!(destinationHolder instanceof Hopper) && !(sourceHolder instanceof Hopper)) {
            return;
        }

        if (!isInventoryLocked(event.getDestination())) {
            return;
        }

        Block sourceInventoryBlock = event.getSource().getLocation().getBlock();
        Block destinationInventoryBlock = event.getDestination().getLocation().getBlock();

        if (sourceInventoryBlock == null || destinationInventoryBlock == null) {
            return;
        }

        String sourceOwner = LocketteProAPI.getOwner(sourceInventoryBlock);
        String destOwner = LocketteProAPI.getOwner(destinationInventoryBlock);

        if (destOwner == null || sourceOwner == null) {
            return;
        }

        if (sourceOwner.equalsIgnoreCase(destOwner)) {
            event.setCancelled(false);
        }
    }
    
    public boolean isInventoryLocked(Inventory inventory){
        InventoryHolder inventoryholder = inventory.getHolder();
        if (inventoryholder instanceof DoubleChest){
            inventoryholder = ((DoubleChest)inventoryholder).getLeftSide();
        }
        if (!(inventoryholder instanceof BlockState)) {
            return false;
        }

        Block block = ((BlockState)inventoryholder).getBlock();
        if (!Config.isCacheEnabled()) { // Cache is disabled
            return LocketteProAPI.isLocked(block);
        }
        // Cache is enabled
        if (Utils.hasValidCache(block)){
            return Utils.getAccess(block);
        }

        boolean isLocked = LocketteProAPI.isLocked(block);
        Utils.setCache(block, isLocked);
        return isLocked;
    }
    
}
