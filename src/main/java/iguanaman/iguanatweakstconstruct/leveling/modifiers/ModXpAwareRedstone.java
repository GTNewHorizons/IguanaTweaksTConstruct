package iguanaman.iguanatweakstconstruct.leveling.modifiers;

import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tconstruct.modifiers.tools.ModRedstone;

/**
 * The same as the redstone modifier, but translates current XP on modifying.
 * Basically: You get XP when applying it, because the required XP for the next level also increases. XP% stays the same.
 */
public class ModXpAwareRedstone extends ModRedstone {
    public final ModRedstone originalModifier;

    public ModXpAwareRedstone(ModRedstone modifier) {
        super(modifier.effectIndex, ListStackToStackArray(modifier.stacks), ListIntToIntArray(modifier.increase));
        originalModifier = modifier;
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        // get max xp before modifying
        long oldXP = LevelingLogic.getRequiredXp(tool, tags);
        long oldBoostXP = LevelingLogic.getRequiredBoostXp(tool);
        // modify
        super.modify(input, tool);

        // update regular xp
        if (LevelingLogic.hasXp(tags)) {
            long newXP = LevelingLogic.getRequiredXp(tool, tags);
            float xp = LevelingLogic.getXp(tags);
            xp *= (float) newXP / (float) oldXP;
            tags.setLong(LevelingLogic.TAG_EXP, Math.round(xp));
        }

        // update boost xp
        if (LevelingLogic.hasBoostXp(tags)) {
            long newBoostXP = LevelingLogic.getRequiredBoostXp(tool);
            float xp = LevelingLogic.getBoostXp(tags);
            xp *= (float) newBoostXP / (float) oldBoostXP;
            tags.setLong(LevelingLogic.TAG_BOOST_EXP, Math.round(xp));
        }
    }

    // we need this because the constructor expects an int[] array, although it uses Integer internally, and Integer[]
    // can't be casted to int[]...
    static int[] ListIntToIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);

        return arr;
    }

    static ItemStack[] ListStackToStackArray(List<ItemStack> list) {
        ItemStack[] arr = new ItemStack[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);

        return arr;
    }
}
