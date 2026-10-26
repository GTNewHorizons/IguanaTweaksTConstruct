package iguanaman.iguanatweakstconstruct.replacing;

import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.ACCESSORY;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.EXTRA;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.HANDLE;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.HEAD;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.detectAdditionalPartType;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.exchangeToolPart;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.getBuildPartType;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.getResultingModifierCount;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.getToolPartMaterial;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.hasSameParts;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.rebuildTool;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.reference.Config;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.crafting.ToolRecipe;
import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.tools.DualMaterialToolPart;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.util.IToolPart;
import tconstruct.library.weaponry.AmmoItem;
import tconstruct.tools.TinkerTools;
import tconstruct.weaponry.TinkerWeaponry;

public class ModPartReplacement extends ItemModifier {

    public ModPartReplacement() {
        // applicable for all items. canModify decides
        super(new ItemStack[0], 0, "");
    }

    @Override
    public boolean matches(ItemStack[] recipe, ItemStack input) {
        return canModify(input, recipe);
    }

    @Override
    protected boolean canModify(ItemStack itemStack, ItemStack[] parts) {
        if (!(itemStack.getItem() instanceof ToolCore)) return false;

        ToolCore tool = (ToolCore) itemStack.getItem();
        NBTTagCompound tags = itemStack.getTagCompound().getCompoundTag("InfiTool");

        // special tools can not be replaced
        if (tags.getBoolean("Special")) return false;

        if (tags.getInteger("Damage") > 0) return false;
        // Durability is written on "Ammo" tag instead of "Damage" tag for AmmoWeapons
        if (tool instanceof AmmoItem && ((AmmoItem) tool).getMaxAmmo(tags) != ((AmmoItem) tool).getAmmoCount(itemStack))
            return false;

        // check if any of the tools parts contain stone. we have to prevent exchanging that with disabled stone tools
        // because otherwise the replacement-logic would not be able to obtain necessary information and crash.
        if (Config.disableStoneTools) {
            if (tool.getHeadItem() != null && getToolPartMaterial(tags, HEAD) == 1) return false;
            if (tool.getHandleItem() != null && getToolPartMaterial(tags, HANDLE) == 1
                    && tool.getHandleItem() != TinkerWeaponry.bowstring
                    && tool.getHandleItem() != TinkerWeaponry.partArrowShaft) {
                return false;
            }
            if (tool.getAccessoryItem() != null && getToolPartMaterial(tags, ACCESSORY) == 1
                    && tool.getAccessoryItem() != TinkerWeaponry.bowstring
                    && tool.getAccessoryItem() != TinkerWeaponry.fletching) {
                return false;
            }
            if (tool.getExtraItem() != null && getToolPartMaterial(tags, EXTRA) == 1) return false;
        }

        // get the recipe of the tool
        ToolRecipe recipe = findRecipe(tool);
        if (recipe == null) return false;

        // check if all parts are actually parts and compatible with the allowed parts
        Item replacementPartItem = null;
        int partIndex = -1;
        PartTypes partType = null;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) continue;
            Item item = parts[i].getItem();

            if (tool == TinkerWeaponry.arrowAmmo)
                if (item == Items.reeds || item == Items.blaze_rod || item == Items.bone || item == Items.stick)
                    item = TinkerWeaponry.partArrowShaft;

            if (item == Items.bone || item == Items.stick) item = TinkerTools.toolRod;

            // is it a toolpart?
            if (!(item instanceof IToolPart)) return false;

            // we only allow single part replacement. sorry i'm lazy. ;/
            if (replacementPartItem != null) return false;

            // which part is it, and is it a valid part?
            if (recipe.validHead(item)) partType = HEAD;
            else if (recipe.validHandle(item)) partType = HANDLE;
            else if (recipe.validAccessory(item)) partType = ACCESSORY;
            else if (recipe.validExtra(item)) partType = EXTRA;
            else return false;

            if (partType == HEAD && tool == TinkerWeaponry.boltAmmo && item instanceof DualMaterialToolPart) {
                partType = HANDLE;
            }

            replacementPartItem = item;
            partIndex = i;
        }

        // no usable part present? :(
        if (replacementPartItem == null) return false;
        if (partIndex == -1) return false;

        // determine materials
        int newMatId = ToolBuilder.instance.getMaterialID(parts[partIndex]);
        int oldMatId = getToolPartMaterial(tags, partType);

        // detect possible secondary position for part (e.g. hammer has 2 plates, etc.)
        // index 0 = handle
        // index 1 = accessory
        // index 2 = extra
        // index >2 = crafting station
        // todo: special behaviour that left side of crafting station is left hammer/battleaxe part and right is right
        // component ;)
        for (int i = partIndex; i > 0; i--) {
            partType = detectAdditionalPartType(recipe, replacementPartItem, partType);
            oldMatId = getToolPartMaterial(tags, partType);
        }

        // if it's a head, we don't allow downgrading to a head with no xp, if we have xp
        if (partType == HEAD)
            if (TConstructRegistry.getMaterial(newMatId).harvestLevel == 0 && LevelingLogic.hasBoostXp(tags))
                return false;

        boolean sameMaterial = newMatId == oldMatId;

        // Special case for bolts, which have two materials.
        if (tool == TinkerWeaponry.boltAmmo && replacementPartItem instanceof DualMaterialToolPart) {
            int newHeadId = ((DualMaterialToolPart) replacementPartItem).getMaterialID2(parts[partIndex]);
            int oldHeadId = getToolPartMaterial(tags, HEAD);

            sameMaterial = sameMaterial && newHeadId == oldHeadId;
        }
        if (sameMaterial) return false;

        // can the tool be built with the new part, and do we have enough modifiers left if we exchange it?
        PartTypes buildPartType = getBuildPartType(tool, partType);
        ItemStack[] rebuilt = rebuildTool(tool, tags, buildPartType, parts[partIndex]);
        if (rebuilt == null) return false;

        NBTTagCompound originalTags = rebuilt[0].getTagCompound().getCompoundTag("InfiTool");
        NBTTagCompound newTags = rebuilt[1].getTagCompound().getCompoundTag("InfiTool");
        // vanilla items like sticks only turn into their material once built
        if (hasSameParts(originalTags, newTags)) return false;

        int modifiers = getResultingModifierCount(tags, originalTags, newTags);

        // replacing the head gives back the modifier used by the mob head boost
        if (buildPartType == HEAD && Config.removeMobHeadOnPartReplacement
                && Config.mobHeadRequiresModifier
                && tags.hasKey("Mining Level Boost"))
            modifiers++;

        return modifiers >= 0;
    }

    @Override
    public void modify(ItemStack[] parts, ItemStack itemStack) {
        // do all the stuff that we did in canModify again to obtain the necessary information :(
        ToolCore tool = (ToolCore) itemStack.getItem();
        ToolRecipe recipe = findRecipe(tool);
        NBTTagCompound tags = itemStack.getTagCompound().getCompoundTag("InfiTool");

        // get item
        Item replacementPartItem = null;
        int partIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) continue;
            replacementPartItem = parts[i].getItem();
            partIndex = i;
        }

        if (tool == TinkerWeaponry.arrowAmmo)
            if (replacementPartItem == Items.reeds || replacementPartItem == Items.blaze_rod
                    || replacementPartItem == Items.bone
                    || replacementPartItem == Items.stick)
                replacementPartItem = TinkerWeaponry.partArrowShaft;

        if (replacementPartItem == Items.bone || replacementPartItem == Items.stick)
            replacementPartItem = TinkerTools.toolRod;

        // detect which part to replace
        PartTypes partType = detectAdditionalPartType(recipe, replacementPartItem, null);
        for (int i = partIndex; i > 0; i--) partType = detectAdditionalPartType(recipe, replacementPartItem, partType);

        // actually do the exchange
        exchangeToolPart(tool, tags, partType, parts[partIndex], itemStack);
    }

    private ToolRecipe findRecipe(ToolCore tool) {
        ToolRecipe recipe;
        // easy way
        recipe = ToolBuilder.instance.recipeList.get(tool.getToolName());
        if (recipe == null) {
            // do a more elaborate check
            for (ToolRecipe r : ToolBuilder.instance.combos) if (r.getType().getClass().equals(tool.getClass())) {
                recipe = r;
                break;
            }
        }

        return recipe;
    }

    @Override
    public void addMatchingEffect(ItemStack input) {}
}
