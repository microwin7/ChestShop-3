package com.Acrobot.Breeze.Utils;

import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Acrobot
 */
public class InventoryUtil {
    /**
     * Returns the amount of the item inside the inventory
     *
     * @param item      Item to check
     * @param inventory inventory
     * @return amount of the item
     */
    public static int getAmount(ItemStack item, Inventory inventory) {
        if (!inventory.contains(item.getType())) {
            return 0;
        }

        if (inventory.getType() == null) {
            return Integer.MAX_VALUE;
        }

        HashMap<Integer, ? extends ItemStack> items = inventory.all(item.getType());
        int itemAmount = 0;

        for (ItemStack iStack : items.values()) {
            if (MaterialUtil.equals(iStack, item)) itemAmount += iStack.getAmount();
        }

        return itemAmount;
    }

    /**
     * Tells if the inventory is empty
     *
     * @param inventory inventory
     * @return Is the inventory empty?
     */
    public static boolean isEmpty(Inventory inventory) {
        for (ItemStack stack : inventory.getContents()) {
            if (!MaterialUtil.isEmpty(stack)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the inventory has stock of this type
     *
     * @param items     items
     * @param inventory inventory
     * @return Does the inventory contain stock of this type?
     */
    public static boolean hasItems(ItemStack[] items, Inventory inventory) {
        for (ItemStack item : items) {
            if (!inventory.containsAtLeast(item, item.getAmount())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the item fits the inventory
     *
     * @param item      Item to check
     * @param inventory inventory
     * @return Does item fit inside inventory?
     */
    public static boolean fits(ItemStack item, Inventory inventory) {
        int left = item.getAmount();
        int maxInvStackSize = inventory.getMaxStackSize();
        if (maxInvStackSize == Integer.MAX_VALUE) return true;

        // TODO gamerforEA code replace, old code:
        // int maxStackSize = item.getMaxStackSize();
        int maxStackSize = Math.min(maxInvStackSize, getMaxStackSize(item));
        // TODO gamerforEA code end

        for (ItemStack iStack : inventory.getContents()) {
            if (left <= 0) return true;
            if (MaterialUtil.isEmpty(iStack)) left -= maxStackSize;
            else if (MaterialUtil.equals(iStack, item)) {
                int amount = iStack.getAmount();

                // TODO gamerforEA code replace, old code:
                // int maxStackSize1 = iStack.getMaxStackSize();
                int maxStackSize1 = Math.min(maxStackSize, getMaxStackSize(iStack));
                if (maxStackSize1 < amount) continue;
                // TODO gamerforEA code end

                left -= maxStackSize1 - amount;
            }
        }

        return left <= 0;
    }

    /**
     * Adds an item to the inventory with given maximum stack size
     * (it currently uses a custom method of adding items, because Bukkit hasn't fixed it for a year now - not even kidding)
     *
     * @param item         Item to add
     * @param inventory    Inventory
     * @param maxStackSize Maximum item's stack size
     * @return Number of leftover items
     */
    public static int add(ItemStack item, Inventory inventory, int maxStackSize) {
        int amountLeft = item.getAmount();
        if (amountLeft < 1) return 0;

        // TODO gamerforEA code start
        maxStackSize = Math.min(maxStackSize, Math.min(inventory.getMaxStackSize(), getMaxStackSize(item)));
        if (maxStackSize < 1) return 0;
        // TODO gamerforEA code end

        for (int currentSlot = 0; currentSlot < inventory.getSize() && amountLeft > 0; ++currentSlot) {
            ItemStack currentItem = inventory.getItem(currentSlot);
            ItemStack duplicate = item.clone();

            if (MaterialUtil.isEmpty(currentItem)) {
                duplicate.setAmount(Math.min(amountLeft, maxStackSize));
                duplicate.addUnsafeEnchantments(item.getEnchantments());

                amountLeft -= duplicate.getAmount();

                inventory.setItem(currentSlot, duplicate);
            } else if (currentItem.getAmount() < maxStackSize && MaterialUtil.equals(currentItem, item)) {
                int currentAmount = currentItem.getAmount();
                int neededToAdd = Math.min(maxStackSize - currentAmount, amountLeft);

                duplicate.setAmount(currentAmount + neededToAdd);
                duplicate.addUnsafeEnchantments(item.getEnchantments());

                amountLeft -= neededToAdd;

                inventory.setItem(currentSlot, duplicate);
            }
        }

        return amountLeft;
    }

    /**
     * Adds an item to the inventor
     *
     * @param item      Item to add
     * @param inventory Inventory
     * @return Number of leftover items
     */
    public static int add(ItemStack item, Inventory inventory) {
        // TODO gamerforEA code replace, old code:
        // return add(item, inventory, item.getMaxStackSize());
        return add(item, inventory, getMaxStackSize(item));
        // TODO gamerforEA code end
    }

    // TODO gamerforEA code start
    private static int getMaxStackSize(ItemStack stack) {
        net.minecraft.server.v1_7_R4.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        return nmsStack != null ? nmsStack.getMaxStackSize() : stack.getMaxStackSize();
    }
    // TODO gamerforEA code end

    /**
     * Removes an item from the inventory
     *
     * @param item      Item to remove
     * @param inventory Inventory
     * @return Number of items that couldn't be removed
     */
    public static int remove(ItemStack item, Inventory inventory) {
        Map<Integer, ItemStack> leftovers = inventory.removeItem(item);

        return countItems(leftovers);
    }

    /**
     * If items in arguments are similar, this function merges them into stacks of the same type
     *
     * @param items Items to merge
     * @return Merged stack array
     */
    public static ItemStack[] mergeSimilarStacks(ItemStack... items) {
        if (items.length <= 1) return items;
        List<ItemStack> itemList = new LinkedList();

        for (ItemStack item : items) {
            Iterator iterator = itemList.iterator();

            while (true) {
                if (!iterator.hasNext()) {
                    itemList.add(item);
                    break;
                }

                ItemStack iStack = (ItemStack) iterator.next();
                if (MaterialUtil.equals(item, iStack)) {
                    iStack.setAmount(iStack.getAmount() + item.getAmount());
                    break;
                }
            }
        }

        return itemList.toArray(new ItemStack[0]);
    }

    /**
     * Counts the amount of items in ItemStacks
     *
     * @param items ItemStacks of items to count
     * @return How many items are there?
     */
    public static int countItems(ItemStack... items) {
        int count = 0;

        for (ItemStack item : items) {
            count += item.getAmount();
        }

        return count;
    }

    /**
     * Counts leftovers from a map
     *
     * @param items Leftovers
     * @return Number of leftovers
     */
    public static int countItems(Map<Integer, ?> items) {
        int totalLeft = 0;

        int left;
        for (Iterator var2 = items.keySet().iterator(); var2.hasNext(); totalLeft += left) {
            left = (Integer) var2.next();
        }

        return totalLeft;
    }
}
