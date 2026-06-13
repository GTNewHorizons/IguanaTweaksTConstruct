package iguanaman.iguanatweakstconstruct.harvestlevels.modifiers;

import static java.lang.Math.max;
import static java.lang.Math.min;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.modifier.ItemModifier;

public class ModBonusMiningLevel extends ItemModifier {

    public final String parentTag;

    // Adds a modifier with the same 'recipe' as diamond or emerald
    // this new modifier is the one modifying harvest level, and is not modifying the base modifier
    public ModBonusMiningLevel(ItemStack[] recipe, String parentTag) {
        super(recipe, 0, "GemBoost");

        this.parentTag = parentTag;
    }

    // when can it be applied
    @Override
    protected boolean canModify(ItemStack input, ItemStack[] recipe) {
        NBTTagCompound tags = input.getTagCompound().getCompoundTag("InfiTool");

        int curLevel = LevelingLogic.getHarvestLevel(tags);

        // only on bronze harvest level, if the config is true
        if (curLevel != HarvestLevels._4_bronze && Config.diamondMinMiningLevelRequired) return false;

        // already applied? Only apply again if config is true
        if (tags.getInteger(key) > 0 && !Config.diamondLevelBoostMultiple) return false;

        // don't apply if boost is already maxxed out (prevents the consumption of without applying an effect)
        if (curLevel >= this.maxHarvestLvl(tags)) return false;

        // can be applied without modifier only if diamond/emerald modifier is already present
        if (tags.getInteger("Modifiers") <= 0 && !tags.getBoolean(parentTag)) return false;

        // diamond (or emerald) level boost can be applied
        return true;
    }

    // what harvest level modification it should do in place of diamond/emerald's harvest level modification
    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");

        int maxLevel = this.maxHarvestLvl(tags);
        int actualBoost = 0;

        // set to new harvest level, clamp to max
        int curLevel = LevelingLogic.getHarvestLevel(tags);
        if (curLevel < maxLevel) {
            // get how many levels are being added, if less than config amount
            actualBoost = Math.min(maxLevel - curLevel, Config.diamondLevelAddition);
            // use the actual boost, so we don't go over the max
            int modifiedLevel = curLevel + actualBoost;
            tags.setInteger("HarvestLevel", modifiedLevel);
        }

        // no need to remove a modifier, since we either already have a diamond modifier or get it added together with
        // this modifier
        // but we have to add the key, and how many levels it has added
        if (tags.hasKey(key)) {
            tags.setInteger(key, tags.getInteger(key) + actualBoost);
        } else tags.setInteger(key, Config.diamondLevelAddition);
    }

    public int maxHarvestLvl(NBTTagCompound tags) {
        /*
         * if it's an emerald, max is bronze, not diamond. but only as long as the config to make it applied to only
         * bronze level tools is not true because then we just keep the base iguana tweaks logic, in which both diamond
         * and emerald increased it to diamond
         */
        int maxLvl = this.parentTag.equals("Emerald") && !Config.diamondMinMiningLevelRequired ? HarvestLevels._4_bronze
                : HarvestLevels._5_diamond;

        /*
         * if boosting is needed, and the pick isn't boosted yet, lower max by 1. Prevents it from getting an extra
         * mining level when it shouldn't. I.e., you boost it to diamond with a bunch of diamonds, then boost it with
         * boostXP or a head, it'll have obsidian harvest level (mines ardite)
         */
        if (LevelingLogic.isBoosted(tags) && Config.pickaxeBoostRequired) {
            maxLvl -= 1;
        }

        return maxLvl;
    }

    public static int gemBoostedLevel(NBTTagCompound tags) {
        int headLvl = TConstructRegistry.getMaterial(tags.getInteger("Head")).harvestLevel();
        boolean isBoosted = LevelingLogic.isBoosted(tags) && Config.pickaxeBoostRequired;

        if (!isBoosted) {
            headLvl -= 1;
        }

        if (tags.hasKey("GemBoost")) {
            int boostedLvl = headLvl + tags.getInteger("GemBoost");

            int max = 0;
            if (tags.getBoolean("Diamond")) max = HarvestLevels._5_diamond;
            else if (tags.getBoolean("Emerald"))
                max = !Config.diamondMinMiningLevelRequired ? HarvestLevels._4_bronze : HarvestLevels._5_diamond;

            if (!isBoosted) {
                max--;
            }

            // return head level if it's the highest
            // otherwise, clamp it to the max between head + boost and the max boost
            return max(headLvl, min(boostedLvl, max));
        }

        return headLvl;
    }

    @Override
    public void addMatchingEffect(ItemStack input) {
        // we don't add an effect, because the diamond/emerald modifier that'll be applied with this will
    }
}
