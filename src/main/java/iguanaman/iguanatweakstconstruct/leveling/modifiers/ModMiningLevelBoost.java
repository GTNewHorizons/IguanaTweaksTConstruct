package iguanaman.iguanatweakstconstruct.leveling.modifiers;

import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.reference.Config;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import tconstruct.modifiers.tools.ModBoolean;

public class ModMiningLevelBoost extends ModBoolean {
    // the maximum mining level obtainable with this head
    private int maxLvl = 0;

    public ModMiningLevelBoost(ItemStack[] recipe, int effect, int maxLvl) {
        super(recipe, effect, "Mining Level Boost", EnumChatFormatting.GREEN.toString(), recipe[0].getDisplayName());

        this.maxLvl = maxLvl;
    }

    @Override
    protected boolean canModify(ItemStack input, ItemStack[] recipe) {
        NBTTagCompound tags = input.getTagCompound().getCompoundTag("InfiTool");

        // Modifier available?
        if (Config.mobHeadRequiresModifier && tags.getInteger("Modifiers") <= 0) return false;

        // already applied?
        if (tags.getBoolean(key)) return false;

        // got required harvest level?
        int hlvl = tags.getInteger("HarvestLevel");
        if (hlvl < maxLvl && LevelingLogic.canBoostMiningLevel(tags)) return true;

        return false;
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        LevelingLogic.levelUpMiningLevel(tool, null, false);

        // add a modifier if it doesn't require one, because ModBoolean will substract one on modify
        if (!Config.mobHeadRequiresModifier) {
            NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
            tags.setInteger("Modifiers", tags.getInteger("Modifiers") + 1);
        }

        super.modify(input, tool);
    }
}
