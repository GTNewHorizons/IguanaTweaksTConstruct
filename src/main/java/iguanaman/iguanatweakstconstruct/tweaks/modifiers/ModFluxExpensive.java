package iguanaman.iguanatweakstconstruct.tweaks.modifiers;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import tconstruct.modifiers.tools.ModFlux;

public class ModFluxExpensive extends ModFlux {

    public ModFluxExpensive(ArrayList<ItemStack> batteries) {
        this.batteries = batteries;
    }

    @Override
    public boolean matches(ItemStack[] input, ItemStack tool) {
        return super.matches(input, tool) && canModify(tool, input);
    }

    // needs 2 modifiers
    @Override
    protected boolean canModify(ItemStack tool, ItemStack[] input) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        return tags.getInteger("Modifiers") > 1 && !tags.getBoolean(key); // Will fail if the modifier is false or the
                                                                          // tag doesn't exist
    }

    @Override
    public void modify(ItemStack[] input, ItemStack tool) {
        super.modify(input, tool);

        // substract additional modifier
        NBTTagCompound tags = tool.getTagCompound();
        int modifiers = tags.getCompoundTag("InfiTool").getInteger("Modifiers");
        modifiers -= 1;
        tags.getCompoundTag("InfiTool").setInteger("Modifiers", modifiers);
    }
}
