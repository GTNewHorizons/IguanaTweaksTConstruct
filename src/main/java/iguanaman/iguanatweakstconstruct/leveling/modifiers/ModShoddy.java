package iguanaman.iguanatweakstconstruct.leveling.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import tconstruct.library.modifier.ItemModifier;

// Shoddy handles Stonebound and Jagged. Jagged simply is negative Stonebound.
public class ModShoddy extends ItemModifier {
    public static ModShoddy ModJagged = new ModShoddy(
            "Jagged",
            EnumChatFormatting.RED.toString(),
            StatCollector.translateToLocal("materialtraits.jagged"),
            -0.7f);
    public static ModShoddy ModStonebound = new ModShoddy(
            "Stonebound",
            EnumChatFormatting.AQUA.toString(),
            StatCollector.translateToLocal("materialtraits.stonebound"),
            0.7f);

    protected float change;
    protected String color;
    protected String tooltipName;

    public ModShoddy(String tag, String c, String tip, float change) {
        super(new ItemStack[0], 0, tag);

        this.tooltipName = tip;
        this.color = c;
        this.change = change;
    }

    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");

        // cannot apply jagged if we have stonebound
        if (tags.getFloat("Shoddy") > 0.0f && this.key.equals(ModJagged.key)) return false;

        // cannot apply stonebound if jagged
        if (tags.getFloat("Shoddy") < 0.0f && this.key.equals(ModStonebound.key)) return false;

        return true;
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");

        if (!tags.getBoolean(key)) {
            tags.setBoolean(key, true);
            addToolTip(tool, color + tooltipName, color + key);
        }

        // go make stonebound/jagged...y
        tags.setFloat("Shoddy", tags.getFloat("Shoddy") + change);
    }

    // no visual effect :(
    @Override
    public void addMatchingEffect(ItemStack input) {}
}
