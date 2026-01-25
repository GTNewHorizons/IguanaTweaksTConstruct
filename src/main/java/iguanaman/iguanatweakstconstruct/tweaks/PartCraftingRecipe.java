package iguanaman.iguanatweakstconstruct.tweaks;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;

import iguanaman.iguanatweakstconstruct.reference.Reference;
import tconstruct.library.crafting.PatternBuilder;
import tconstruct.library.util.IPattern;

public class PartCraftingRecipe implements IRecipe {

    static {
        // register the recipe with the recipesorter
        RecipeSorter
                .register(Reference.MOD_ID + ":part", RepairCraftingRecipe.class, RecipeSorter.Category.SHAPELESS, "");
    }

    private ItemStack outputPart;

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        outputPart = null;
        ItemStack pattern = null;
        ItemStack input = null;

        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack slot = inventoryCrafting.getStackInSlot(i);
            // empty slot
            if (slot == null) continue;

            // is it the tool?
            if (slot.getItem() instanceof IPattern) {
                // only one pattern
                if (pattern != null) return false;

                pattern = slot;
                // otherwise.. input material
            } else {
                if (input != null) return false;

                input = slot;
            }
        }

        if (pattern == null || input == null) return false;

        // make sure it's a valid pattern
        if (!PatternBuilder.instance.toolPatterns.contains(pattern.getItem())) return false;

        ItemStack[] output = PatternBuilder.instance.getToolPart(input, pattern, null);
        if (output == null) return false;

        for (ItemStack stack : output) if (stack != null) {
            outputPart = stack;
            return true;
        }

        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        return outputPart;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return outputPart;
    }
}
