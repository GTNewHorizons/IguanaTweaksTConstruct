package iguanaman.iguanatweakstconstruct.tweaks.modifiers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.GameRegistry;
import iguanaman.iguanatweakstconstruct.util.Log;
import tconstruct.modifiers.tools.ModExtraModifier;

/**
 * One time use modifier that adds a configurable amount of modifier slots, see Config.bonusModifierSlots.
 */
public class ModBonusModifierSlots extends ModExtraModifier {

    private final int slots;

    public ModBonusModifierSlots(ItemStack[] items, String dataKey, int slots) {
        super(items, dataKey);
        this.slots = slots;
    }

    @Override
    public void modify(ItemStack[] recipe, ItemStack input) {
        NBTTagCompound tags = this.getModifierTag(input);
        tags.setBoolean(key, true);
        tags.setInteger("Modifiers", tags.getInteger("Modifiers") + slots);
    }

    /**
     * Parses "key;slots;item;item...", items being "modid:name" or "modid:name:meta" (meta * for any).
     *
     * @return the modifier, or null if the entry is invalid
     */
    public static ModBonusModifierSlots fromConfig(String entry) {
        String[] parts = entry.trim().split(";");
        if (parts.length < 3) {
            Log.error("Invalid bonus modifier slot entry, expected key;slots;item...: " + entry);
            return null;
        }

        int slots;
        try {
            slots = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            Log.error("Invalid slot amount in bonus modifier slot entry: " + entry);
            return null;
        }
        if (slots <= 0) {
            Log.error("Slot amount has to be positive in bonus modifier slot entry: " + entry);
            return null;
        }

        List<ItemStack> items = new ArrayList<ItemStack>();
        for (int i = 2; i < parts.length; i++) {
            ItemStack stack = parseItem(parts[i].trim());
            if (stack == null) {
                Log.error("Unknown item " + parts[i].trim() + " in bonus modifier slot entry: " + entry);
                return null;
            }
            items.add(stack);
        }

        return new ModBonusModifierSlots(items.toArray(new ItemStack[0]), parts[0].trim(), slots);
    }

    private static ItemStack parseItem(String id) {
        String[] parts = id.split(":");
        if (parts.length < 2 || parts.length > 3) return null;

        Item item = GameRegistry.findItem(parts[0], parts[1]);
        if (item == null) return null;

        int meta = 0;
        if (parts.length == 3) {
            if (parts[2].equals("*")) meta = OreDictionary.WILDCARD_VALUE;
            else {
                try {
                    meta = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return new ItemStack(item, 1, meta);
    }
}
