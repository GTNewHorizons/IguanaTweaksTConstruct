package iguanaman.iguanatweakstconstruct.leveling.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import tconstruct.modifiers.tools.ModBoolean;

public class ModCritical extends ModBoolean {
    public static ModCritical modCritical = new ModCritical(
            "Critical Chance",
            EnumChatFormatting.WHITE.toString(),
            StatCollector.translateToLocal("materialtraits.critical"));

    public ModCritical(String tag, String c, String tip) {
        super(new ItemStack[0], 0, tag, c, tip);
    }

    @Override
    public void addMatchingEffect(ItemStack input) {}
}
