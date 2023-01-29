package iguanaman.iguanatweakstconstruct.tweaks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;

import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.util.IToolPart;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.reference.Reference;

public class ToolCraftingRecipe implements IRecipe {

    static {
        // register the recipe with the recipesorter
        RecipeSorter
                .register(Reference.MOD_ID + ":tool", RepairCraftingRecipe.class, RecipeSorter.Category.SHAPELESS, "");
    }

    private ItemStack outputTool;

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        outputTool = null;

        List<ItemStack> input = new LinkedList<ItemStack>();

        boolean hasToolPart = false;

        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack slot = inventoryCrafting.getStackInSlot(i);
            // empty slot
            if (slot == null) continue;

            // is it a toolpart?
            if (slot.getItem() instanceof IToolPart) hasToolPart = true;

            // save it
            input.add(slot);
        }

        // at least 1 of the used parts has to be a toolpart
        // this is only a performance optimization. It allows to use sticks as handles while
        // not checking all possible permutations for recipes that don't even contain toolparts
        if (!hasToolPart) return false;

        // check correct size
        if (input.size() < 2 || (input.size() > 3 && !Config.easyAdvancedToolBuilding)) return false;

        // ok. we have to cycle through all permutations. urgh.
        Set<List<ItemStack>> permutations = new HashSet<List<ItemStack>>();
        permutate(new LinkedList<ItemStack>(), input, permutations);

        for (List<ItemStack> comb : permutations) {
            ItemStack tool = null;
            ItemStack accessory = null, extra = null;
            if (comb.size() > 2) accessory = comb.get(2);
            if (comb.size() > 3) extra = comb.get(3);

            // try this combination xX
            tool = ToolBuilder.instance.buildTool(comb.get(0), comb.get(1), accessory, extra, "");

            if (tool != null) {
                outputTool = tool;
                return true;
            }
        }

        return false;
    }

    private void permutate(List<ItemStack> cur, List<ItemStack> rest, Set<List<ItemStack>> output) {
        // if we reached the last element, add the combination to the output
        if (rest.size() == 1) {
            List<ItemStack> out = new LinkedList<ItemStack>(cur);
            out.add(rest.get(0));
            output.add(out);
        }
        // otherwise recursively cycle through all other variations
        else {
            for (ItemStack foo : rest) {
                List<ItemStack> newCur = new LinkedList<ItemStack>(cur);
                List<ItemStack> newRest = new LinkedList<ItemStack>(rest);
                newCur.add(foo);
                newRest.remove(foo);
                permutate(newCur, newRest, output);
            }
        }
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting p_77572_1_) {
        return outputTool;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return outputTool;
    }
}
