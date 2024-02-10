package com.Acrobot.ChestShop.Listeners.PostTransaction;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.ChestShop.Events.TransactionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Acrobot
 */
public class ItemManager implements Listener {
    @EventHandler
    public static void shopItemRemover(TransactionEvent event) {
        if (event.getTransactionType() == TransactionEvent.TransactionType.BUY) {
            removeItems(event.getOwnerInventory(), event.getStock());
            addItems(event.getClientInventory(), event.getStock());
            event.getClient().updateInventory();
        }
    }

    @EventHandler
    public static void inventoryItemRemover(TransactionEvent event) {
        if (event.getTransactionType() == TransactionEvent.TransactionType.SELL) {
            removeItems(event.getClientInventory(), event.getStock());
            addItems(event.getOwnerInventory(), event.getStock());
            event.getClient().updateInventory();
        }
    }

    private static void removeItems(Inventory inventory, ItemStack[] items) {
        for (ItemStack item : items) {
            InventoryUtil.remove(item, inventory);
        }
    }

    private static void addItems(Inventory inventory, ItemStack[] items) {
		/* TODO gamerforEA code clear:
		if (Properties.STACK_TO_64)
			for (ItemStack item : items)
			{
				InventoryUtil.add(item, inventory, 64);
			}
		else */
        for (ItemStack item : items) {
            InventoryUtil.add(item, inventory);
        }
    }
}
