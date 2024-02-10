package com.Acrobot.Breeze.Utils;

import com.Acrobot.ChestShop.ChestShop;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.bukkit.CoalType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Acrobot
 */
public class MaterialUtil {
    public static final Pattern DURABILITY = Pattern.compile(":(\\d)*");
    public static final Pattern METADATA = Pattern.compile("#([0-9a-zA-Z])*");
    public static final boolean LONG_NAME = true;
    public static final boolean SHORT_NAME = false;
    private static final Map<String, Material> MATERIAL_CACHE = new HashMap();

    /**
     * Checks if the itemStack is empty or null
     *
     * @param item Item to check
     * @return Is the itemStack empty?
     */
    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * Checks if the itemStacks are equal, ignoring their amount
     *
     * @param one first itemStack
     * @param two second itemStack
     * @return Are they equal?
     */
    public static boolean equals(ItemStack one, ItemStack two) {
        // TODO gamerforEA code replace, old code:
        // return one.isSimilar(two);
        CraftItemStack craftOne = one instanceof CraftItemStack ? (CraftItemStack) one : CraftItemStack.asCraftCopy(one);
        CraftItemStack craftTwo = two instanceof CraftItemStack ? (CraftItemStack) two : CraftItemStack.asCraftCopy(two);
        return craftOne.isSimilar(craftTwo);
        // TODO gamerforEA code end
    }

    /**
     * Gives you a Material from a String (doesn't have to be fully typed in)
     *
     * @param name Name of the material
     * @return Material found
     */
    public static Material getMaterial(String name) {
        String formatted = name.replaceAll(" |_", "").toUpperCase();
        if (MATERIAL_CACHE.containsKey(formatted)) return MATERIAL_CACHE.get(formatted);
        Material material = Material.matchMaterial(name);

        if (material != null) {
            MATERIAL_CACHE.put(formatted, material);
            return material;
        }
        short length = 32767;

        for (Material currentMaterial : Material.values()) {
            String matName = currentMaterial.name();

            if (matName.length() < length && matName.replace("_", "").startsWith(formatted)) {
                length = (short) matName.length();
                material = currentMaterial;
            }
        }

        MATERIAL_CACHE.put(formatted, material);

        return material;
    }

    /**
     * Returns item's name
     *
     * @param itemStack ItemStack to name
     * @return ItemStack's name
     */
    public static String getName(ItemStack itemStack) {
        return getName(itemStack, true);
    }

    /**
     * Returns item's name
     *
     * @param itemStack     ItemStack to name
     * @param showDataValue Should we also show the data value?
     * @return ItemStack's name
     */
    public static String getName(ItemStack itemStack, boolean showDataValue) {
        String dataName = MaterialUtil.DataValue.name(itemStack);
        return dataName != null && showDataValue ? StringUtil.capitalizeFirstLetter(dataName + '_' + itemStack.getType(), '_') : StringUtil.capitalizeFirstLetter(itemStack.getType().toString(), '_');
    }

    /**
     * Returns item's name, just like on the sign
     *
     * @param itemStack ItemStack to name
     * @return ItemStack's name
     */
    public static String getSignName(ItemStack itemStack) {
        StringBuilder name = new StringBuilder(15);

        String itemName = itemStack.getType().name();
        itemName = StringUtil.capitalizeFirstLetter(itemName, '_');

        name.append(itemName);
        if (itemStack.getDurability() != 0) name.append(':').append(itemStack.getDurability());

        if (itemStack.hasItemMeta()) name.append('#').append(Metadata.getItemCode(itemStack));

        return name.toString();
    }

    /**
     * Gives you an ItemStack from a String
     *
     * @param itemName Item name
     * @return ItemStack
     */
    public static ItemStack getItem(String itemName) {
        ItemStack itemStack = MaterialUtil.Odd.getFromString(itemName);
        if (itemStack != null) return itemStack;
        String[] split = Iterables.toArray(Splitter.onPattern(":|-|#").trimResults().split(itemName), String.class);
        Material material = getMaterial(split[0]);
        short durability = getDurability(itemName);
        if (material == null) {
            if (!split[0].contains(" ")) return null;

            for (int index = split[0].indexOf(32); index >= 0; index = split[0].indexOf(32, index + 1)) {
                material = getMaterial(split[0].substring(index));
                if (material != null) {
                    if (durability == 0) durability = (short) DataValue.get(split[0].substring(0, index), material);
                    break;
                }
            }

            if (material == null) return null;
        }

        itemStack = new ItemStack(material);
        itemStack.setDurability(durability);
        ItemMeta meta = getMetadata(itemName);
        if (meta != null) itemStack.setItemMeta(meta);

        return itemStack;
    }

    /**
     * Returns the durability from a string
     *
     * @param itemName Item name
     * @return Durability found
     */
    public static short getDurability(String itemName) {
        Matcher m = DURABILITY.matcher(itemName);
        if (!m.find()) return (short) 0;
        String data = m.group();
        if (data != null && !data.isEmpty()) {
            data = data.substring(1);
            return NumberUtil.isShort(data) ? Short.valueOf(data) : 0;
        }
        return (short) 0;
    }

    /**
     * Returns metadata from a string
     *
     * @param itemName Item name
     * @return Metadata found
     */
    public static ItemMeta getMetadata(String itemName) {
        Matcher m = METADATA.matcher(itemName);
        if (!m.find()) return null;
        String group = m.group().substring(1);
        return MaterialUtil.Metadata.getFromCode(group);
    }

    public static class DataValue {
        /**
         * Gets the data value from a string
         *
         * @param type     Data Value string
         * @param material Material
         * @return data value
         */
        public static byte get(String type, Material material) {
            if (material != null && material.getData() != null) {
                type = type.toUpperCase().replace(" ", "_");
                MaterialData materialData = material.getNewData((byte) 0);
                if (materialData instanceof TexturedMaterial) {
                    TexturedMaterial texturedMaterial = (TexturedMaterial) materialData;

                    for (Material mat : texturedMaterial.getTextures()) {
                        if (mat.name().startsWith(type) && !mat.equals(material))
                            return (byte) texturedMaterial.getTextures().indexOf(mat);
                    }
                } else {
                    if (materialData instanceof Colorable) {
                        DyeColor color;

                        try {
                            color = DyeColor.valueOf(type);
                        } catch (IllegalArgumentException exception) {
                            return (byte) 0;
                        }

                        if (material == Material.INK_SACK) return color.getDyeData();

                        return color.getWoolData();
                    }

                    if (materialData instanceof Tree) try {
                        return TreeSpecies.valueOf(type).getData();
                    } catch (IllegalArgumentException ex) {
                        return (byte) 0;
                    }

                    if (materialData instanceof SpawnEgg) try {
                        EntityType entityType = EntityType.valueOf(type);
                        return (byte) entityType.getTypeId();
                    } catch (IllegalArgumentException ex) {
                        return (byte) 0;
                    }

                    if (materialData instanceof Coal) try {
                        return CoalType.valueOf(type).getData();
                    } catch (IllegalArgumentException ex) {
                        return (byte) 0;
                    }
                }

                return (byte) 0;
            }
            return (byte) 0;
        }

        public static String name(ItemStack itemStack) {
            MaterialData data = itemStack.getData();
            if (data == null) return null;
            if (data instanceof TexturedMaterial) return ((TexturedMaterial) data).getMaterial().name();
            if (data instanceof Colorable) {
                DyeColor color = ((Colorable) data).getColor();
                return color != null ? color.name() : null;
            }
            if (data instanceof Tree) {
                TreeSpecies specie = ((Tree) data).getSpecies();
                return specie != null && specie != TreeSpecies.GENERIC ? specie.name() : null;
            }
            if (data instanceof SpawnEgg) {
                EntityType type = ((SpawnEgg) data).getSpawnedType();
                return type != null ? type.name() : null;
            }
            if (!(data instanceof Coal)) return null;
            CoalType coal = ((Coal) data).getType();
            return coal != null && coal != CoalType.COAL ? coal.name() : null;
        }
    }

    public static class Metadata {
        /**
         * Returns the ItemStack represented by this code
         *
         * @param code Code representing the item
         * @return Item represented by code
         */
        public static ItemMeta getFromCode(String code) {
            ItemStack item = ChestShop.getItemDatabase().getFromCode(code);
            return item == null ? null : item.getItemMeta();
        }

        /**
         * Returns the code for this item
         *
         * @param item Item being represented
         * @return Code representing the item
         */
        public static String getItemCode(ItemStack item) {
            return ChestShop.getItemDatabase().getItemCode(item);
        }
    }

    public static class Odd {
        private static boolean isInitialized = false;

        /**
         * Returns the item stack from OddItem plugin
         *
         * @param itemName Item name to parse
         * @return itemStack that was parsed
         */
        public static ItemStack getFromString(String itemName) {
			/* TODO gamerforEA code replace, old code:
			if (!isInitialized)
				return null;
			else
			{
				String name = itemName.replace(':', ';');

				try
				{
					return OddItem.getItemStack(name);
				}
				catch (Exception var3)
				{
					return null;
				}
			} */
            return null;
            // TODO gamerforEA code end
        }

        /**
         * Lets the class know that it's safe to use the OddItem methods now
         */
        public static void initialize() {
            isInitialized = true;
        }
    }
}
