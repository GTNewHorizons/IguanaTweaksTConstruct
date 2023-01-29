package iguanaman.iguanatweakstconstruct.tweaks;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;

import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.tools.ToolCore;
import tconstruct.modifiers.tools.ModToolRepair;
import iguanaman.iguanatweakstconstruct.reference.Reference;

public class RepairCraftingRecipe implements IRecipe {

    static {
        // register the recipe with the recipesorter
        RecipeSorter.register(
                Reference.MOD_ID + ":repair",
                RepairCraftingRecipe.class,
                RecipeSorter.Category.SHAPELESS,
                "");
    }

    private ModToolRepair modifier = null;
    private ItemStack modifiedTool = null;

    public RepairCraftingRecipe() {
        for (ItemModifier mod : ModifyBuilder.instance.itemModifiers) if (mod instanceof ModToolRepair) {
            modifier = (ModToolRepair) mod;
            break;
        }
    }

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        ItemStack tool = null;
        ItemStack[] input = new ItemStack[inventoryCrafting.getSizeInventory()];

        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack slot = inventoryCrafting.getStackInSlot(i);
            // empty slot
            if (slot == null) continue;

            // is it the tool?
            if (slot.getItem() instanceof ToolCore) tool = slot;
            // otherwise.. input material
            else input[i] = slot;
        }
        // no tool found?
        if (tool == null) return false;

        // check if applicable, and save result for later
        if (modifier.matches(input, tool)) {
            modifiedTool = tool.copy();
            modifier.modify(input, modifiedTool);
            return true;
        } else modifiedTool = null;

        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        // we call the matches-function again to sync up the issue in case it got called between
        // the matches-call and the getCraftingResult call
        // we can pass null for the world since it is never accessed in the function
        matches(inventoryCrafting, null);
        return modifiedTool;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return modifiedTool;
    }
}
