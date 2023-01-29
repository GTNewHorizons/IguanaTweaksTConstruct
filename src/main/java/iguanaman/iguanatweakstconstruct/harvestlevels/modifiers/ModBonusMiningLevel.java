package iguanaman.iguanatweakstconstruct.harvestlevels.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.library.modifier.ItemModifier;
import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;

public class ModBonusMiningLevel extends ItemModifier {

    public final String parentTag;

    public ModBonusMiningLevel(ItemStack[] recipe, String parentTag) {
        super(recipe, 0, "GemBoost");

        this.parentTag = parentTag;
    }

    @Override
    protected boolean canModify(ItemStack input, ItemStack[] recipe) {
        NBTTagCompound tags = input.getTagCompound().getCompoundTag("InfiTool");

        // only on bronze harvest level
        if (LevelingLogic.getHarvestLevel(tags) != HarvestLevels._4_bronze) return false;

        // already applied? (actually impossible, but maybe we'll change something in the future
        if (tags.getBoolean(key)) return false;

        // can be applied without modifier if diamond/emerald modifier is already present
        if (tags.getInteger("Modifiers") <= 0 && !tags.getBoolean(parentTag)) return false;

        // only if harvestlevel is bronze and can NOT be boosted anymore
        return !LevelingLogic.canBoostMiningLevel(tags);
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        // set harvestlevel to diamond
        tags.setInteger("HarvestLevel", HarvestLevels._5_diamond);

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
