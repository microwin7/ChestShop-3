package com.Acrobot.ChestShop.Listeners.Modules;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.PreShopCreationEvent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

/**
 * @author Acrobot
 */
public class PriceRestrictionModule implements Listener {
    private YamlConfiguration configuration;
    private static final double INVALID_PATH = Double.MIN_VALUE;
    // TODO gamerforEA code start
    private YamlConfiguration amountCfg;

    public PriceRestrictionModule() {
		/* TODO gamerforEA code replace, old code:
		File file = new File(ChestShop.getFolder(), "priceLimits.yml");
		this.configuration = YamlConfiguration.loadConfiguration(file);
		this.configuration.options().header("In this file you can configure maximum and minimum prices for items (when creating a shop).");
		if (!file.exists())
		{
			this.configuration.addDefault("max.buy_price.itemID", 5.53D);
			this.configuration.addDefault("max.buy_price.988", 3.51D);
			this.configuration.addDefault("max.sell_price.978", 3.52D);
			this.configuration.addDefault("min.buy_price.979", 1.03D);
			this.configuration.addDefault("min.sell_price.989", 0.51D);

			try
			{
				this.configuration.options().copyDefaults(true);
				this.configuration.save(ChestShop.loadFile("priceLimits.yml"));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		} */
        this.reload();
        // TODO gamerforEA code end
    }

    public void reload() {
        this.loadPriceCfg();
        this.loadAmountCfg();
    }

    private void loadAmountCfg() {
        File file = new File(ChestShop.getFolder(), "amountLimits.yml");
        this.amountCfg = YamlConfiguration.loadConfiguration(file);
        this.amountCfg.options().header("In this file you can configure maximum and minimum amounts for items (when creating a shop).");
        if (!file.exists()) {
            this.amountCfg.addDefault("max.buy_amount.itemID", 10);
            this.amountCfg.addDefault("max.buy_amount.988", 1);
            this.amountCfg.addDefault("max.sell_amount.978", 1);
            this.amountCfg.addDefault("min.buy_amount.979", 3);
            this.amountCfg.addDefault("min.sell_amount.989", 4);

            try {
                this.amountCfg.options().copyDefaults(true);
                this.amountCfg.save(ChestShop.loadFile("amountLimits.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // TODO gamerforEA code end

    private void loadPriceCfg() {
        File file = new File(ChestShop.getFolder(), "priceLimits.yml");
        this.configuration = YamlConfiguration.loadConfiguration(file);
        this.configuration.options().header("In this file you can configure maximum and minimum prices for items (when creating a shop).");
        if (!file.exists()) {
            this.configuration.addDefault("max.buy_price.itemID", 5.53D);
            this.configuration.addDefault("max.buy_price.988", 3.51D);
            this.configuration.addDefault("max.sell_price.978", 3.52D);
            this.configuration.addDefault("min.buy_price.979", 1.03D);
            this.configuration.addDefault("min.sell_price.989", 0.51D);

            try {
                this.configuration.options().copyDefaults(true);
                this.configuration.save(ChestShop.loadFile("priceLimits.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPreShopCreation(PreShopCreationEvent event) {
        ItemStack material = MaterialUtil.getItem(event.getSignLine((byte) 3));
        if (material != null) {
            int itemID = material.getTypeId();

			/* TODO gamerforEA code replace, old code:
			int amount = material.getAmount();
			if (PriceUtil.hasBuyPrice(event.getSignLine((byte) 2)))
			{
				double buyPrice = PriceUtil.getBuyPrice(event.getSignLine((byte) 2));

				if (this.isValid("min.buy_price." + itemID) && buyPrice < this.configuration.getDouble("min.buy_price." + itemID) / (double) amount)
					event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_PRICE);

				if (this.isValid("max.buy_price." + itemID) && buyPrice > this.configuration.getDouble("max.buy_price." + itemID) / (double) amount)
					event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_PRICE);
			}

			if (PriceUtil.hasSellPrice(event.getSignLine((byte) 2)))
			{
				double sellPrice = PriceUtil.getSellPrice(event.getSignLine((byte) 2));
				if (this.isValid("min.sell_price." + itemID) && sellPrice < this.configuration.getDouble("min.sell_price." + itemID) / (double) amount)
					event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_PRICE);

				if (this.isValid("max.sell_price." + itemID) && sellPrice > this.configuration.getDouble("max.sell_price." + itemID) / (double) amount)
					event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_PRICE);
			} */
            int meta = material.getDurability();
            int amount = material.getAmount();

            try {
                String quantity = event.getSignLine((byte) 1);
                int customAmount = Integer.parseInt(quantity);
                amount = Math.max(amount, customAmount);
            } catch (NumberFormatException ignored) {
            }

            for (String id : new String[]{itemID + ":" + meta, String.valueOf(itemID)}) {
                if (PriceUtil.hasBuyPrice(event.getSignLine((byte) 2))) {
                    double buyPrice = PriceUtil.getBuyPrice(event.getSignLine((byte) 2));
                    double minPrice = this.configuration.getDouble("min.buy_price." + id) * amount;
                    double maxPrice = this.configuration.getDouble("max.buy_price." + id) * amount;
                    int minAmount = this.amountCfg.getInt("min.buy_amount." + id);
                    int maxAmount = this.amountCfg.getInt("max.buy_amount." + id);
                    if (this.isValid("min.buy_price." + id) && buyPrice < minPrice)
                        event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_PRICE);
                    else if (this.isValid("max.buy_price." + id) && buyPrice > maxPrice)
                        event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_PRICE);
                    else if (minAmount > 0 && amount < minAmount)
                        event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_QUANTITY);
                    else if (maxAmount > 0 && amount > maxAmount)
                        event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_QUANTITY);
                }
                if (PriceUtil.hasSellPrice(event.getSignLine((byte) 2))) {
                    double sellPrice = PriceUtil.getSellPrice(event.getSignLine((byte) 2));
                    double minPrice = this.configuration.getDouble("min.sell_price." + id) * amount;
                    double maxPrice = this.configuration.getDouble("max.sell_price." + id) * amount;
                    int minAmount = this.amountCfg.getInt("min.sell_amount." + id);
                    int maxAmount = this.amountCfg.getInt("max.sell_amount." + id);
                    if (this.isValid("min.sell_price." + id) && sellPrice < minPrice)
                        event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_PRICE);
                    else if (this.isValid("max.sell_price." + id) && sellPrice > maxPrice)
                        event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_PRICE);
                    else if (minAmount > 0 && amount < minAmount)
                        event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_QUANTITY);
                    else if (maxAmount > 0 && amount > maxAmount)
                        event.setOutcome(PreShopCreationEvent.CreationOutcome.INVALID_QUANTITY);
                }
            }
            // TODO gamerforEA code end
        }
    }

    private boolean isValid(String path) {
        return configuration.getDouble(path, INVALID_PATH) != INVALID_PATH;
    }
}
