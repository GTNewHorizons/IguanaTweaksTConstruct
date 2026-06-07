package iguanaman.iguanatweakstconstruct.harvestlevels.modifiers;

import static java.lang.Math.min;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
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
        if (tags.getBoolean(key) && !Config.diamondLevelBoostMultiple) return false;

        // don't apply if boost is already maxxed out (prevents the consumption of without applying an effect)
        int maxLevel = HarvestLevels._5_diamond;
        // if it's emerald and it can be applied to any tool, not just bronze
        maxLevel = this.parentTag.equals("Emerald") && !Config.diamondMinMiningLevelRequired ? HarvestLevels._4_bronze
                : HarvestLevels._5_diamond;
        if (curLevel >= maxLevel) return false;

        // can be applied without modifier only if diamond/emerald modifier is already present
        if (tags.getInteger("Modifiers") <= 0 && !tags.getBoolean(parentTag)) return false;

        // diamond (or emerald) level boost can be applied
        return true;
    }

    // what harvest level modification it should do in place of diamond/emerald's harvest level modification
    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        int maxLevel = HarvestLevels._5_diamond;

        // if it's an emerald, max is bronze, not diamond.
        // but only as long as the config to make it applied to only bronze level tools is not true
        // because then we just keep the base iguana tweaks logic,
        // in which both diamond and emerald increased it to diamond mining level
        if (this.parentTag.equals("Emerald") && !Config.diamondMinMiningLevelRequired)
            maxLevel = HarvestLevels._4_bronze;

        // set to new harvest level, clamp to max
        int curLevel = LevelingLogic.getHarvestLevel(tags);
        if (curLevel < maxLevel) {
            int modifiedLevel = curLevel + Config.diamondLevelAddition;
            // just in case it's more than 1 level per diamond/emerald
            modifiedLevel = min(modifiedLevel, maxLevel);
            tags.setInteger("HarvestLevel", modifiedLevel);
        }

        // no need to remove a modifier, since we either already have a diamond modifier or get it added together with
        // this modifier
        // but we have to add the key
        tags.setBoolean(key, true);
    }

    @Override
    public void addMatchingEffect(ItemStack input) {
        // we don't add an effect, because the diamond/emerald modifier that'll be applied with this will
    }
}
