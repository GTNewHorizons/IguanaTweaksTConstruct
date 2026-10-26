package iguanaman.iguanatweakstconstruct.replacing;

import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.ACCESSORY;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.EXTRA;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.HANDLE;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.HEAD;

import java.util.Arrays;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;
import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import iguanaman.iguanatweakstconstruct.util.Log;
import tconstruct.TConstruct;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.ModifyBuilder;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.crafting.ToolRecipe;
import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.tools.DualMaterialToolPart;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.weaponry.IAmmo;
import tconstruct.modifiers.tools.ModAttack;
import tconstruct.modifiers.tools.ModWindup;
import tconstruct.tools.TinkerTools;
import tconstruct.util.config.PHConstruct;
import tconstruct.weaponry.TinkerWeaponry;

public final class ReplacementLogic {

    private ReplacementLogic() {} // non-instantiable

    public static void exchangeToolPart(ToolCore tool, NBTTagCompound tags, PartTypes type, ItemStack partStack,
            ItemStack toolStack) {
        type = getBuildPartType(tool, type);
        ItemStack[] rebuilt = rebuildTool(tool, tags, type, partStack);
        if (rebuilt == null) {
            Log.warn("Couldn't rebuild " + toolStack.getDisplayName() + " to replace its part, nothing was changed.");
            return;
        }
        NBTTagCompound originalTags = rebuilt[0].getTagCompound().getCompoundTag("InfiTool");
        NBTTagCompound newTags = rebuilt[1].getTagCompound().getCompoundTag("InfiTool");

        int partMaterialId = getToolPartMaterial(newTags, type);
        int oldMaterialId = getToolPartMaterial(tags, type);

        // Things that can change from replacing a part:
        // - durability
        // - mining speed
        // - mining level
        // - attack
        // - reinforced
        // - shoddy/stonebound
        // - Material Traits/Abilities that are not reinforced/stonebound (mostly from other mods)
        // - XP

        // save unmodified old required xp for later use
        long oldRequiredXp = LevelingLogic.getRequiredXp(toolStack, tags);
        long oldRequiredBoostXp = LevelingLogic.getRequiredBoostXp(toolStack);

        // can be negative until the mob head refund below. Not clamped, ModifyBuilder rejects the swap if still
        // negative, e.g. when TinkersGregworks' looser check let it through
        tags.setInteger("Modifiers", getResultingModifierCount(tags, originalTags, newTags));

        // update part materials and rendering
        updateTag(newTags, tags, "Head");
        updateTag(newTags, tags, "RenderHead");
        updateTag(newTags, tags, "Handle");
        updateTag(newTags, tags, "RenderHandle");
        updateTag(newTags, tags, "Accessory");
        updateTag(newTags, tags, "RenderAccessory");
        updateTag(newTags, tags, "Extra");
        updateTag(newTags, tags, "RenderExtra");

        // max ammo comes from the durability, check it before updating
        boolean fullAmmo = tool instanceof IAmmo
                && ((IAmmo) tool).getAmmoCount(toolStack) >= ((IAmmo) tool).getMaxAmmo(toolStack);

        // update durability
        int base = newTags.getInteger("BaseDurability");
        int bonus = tags.getInteger("BonusDurability"); // not changed through material, modifier
        float modDur = tags.getFloat("ModDurability"); // not changed through material, modifier

        // long so maxed out durabilities don't wrap around, float multiply like tcon's ModDurability
        int total = Math.max(ToolBuilder.clampDurability((long) (((long) base + bonus) * (modDur + 1f))), 1);
        tags.setInteger("BaseDurability", base);
        tags.setInteger("TotalDurability", total);

        // update damage
        updateTag(newTags, tags, "Attack");
        updateTag(newTags, tags, "BaseAttack");

        // update mining speed, keeping what was added after crafting (redstone, silk touch)
        updateMiningSpeed(tool, tags, originalTags, newTags, "MiningSpeed", true);
        updateMiningSpeed(tool, tags, originalTags, newTags, "MiningSpeed2", true);
        updateMiningSpeed(tool, tags, originalTags, newTags, "MiningSpeedHandle", false);
        updateMiningSpeed(tool, tags, originalTags, newTags, "MiningSpeedExtra", false);

        // update harvest level (boosting/xp will be applied later)
        updateTag(newTags, tags, "HarvestLevel");
        updateTag(newTags, tags, "HarvestLevel2");
        updateTag(newTags, tags, "HarvestLevelHandle");
        updateTag(newTags, tags, "HarvestLevelExtra");

        // ranged weapons have additional tags to consider
        updateTag(newTags, tags, "DrawSpeed");
        updateTag(newTags, tags, "BaseDrawSpeed");
        updateTag(newTags, tags, "FlightSpeed");
        updateTag(newTags, tags, "Mass");
        updateTag(newTags, tags, "BreakChance");
        updateTag(newTags, tags, "Accuracy");

        // stonebound/jagged. Leveling bonuses add to the material value, so keep what was added after crafting
        float shoddy = tags.getFloat("Shoddy") + newTags.getFloat("Shoddy") - originalTags.getFloat("Shoddy");
        // float rounding can leave a tiny value instead of 0, which would block jagged/stonebound level up bonuses
        if (Math.abs(shoddy) < 0.0001f) shoddy = 0f;
        tags.setFloat("Shoddy", shoddy);

        // update ammo, full ammo stays full like an undamaged tool keeps full durability
        if (tool instanceof IAmmo) {
            IAmmo ammo = (IAmmo) tool;
            if (fullAmmo || ammo.getAmmoCount(toolStack) > ammo.getMaxAmmo(toolStack))
                ammo.setAmmo(ammo.getMaxAmmo(toolStack), toolStack);
        }

        // reinforced is kinda complicated, since the actual level you get out of the materials is complicated
        // simply calculate difference between current and newly built tool to know how much has been added afterwards
        int currentReinforced = tags.getInteger("Unbreaking");
        int oldReinforced = originalTags.getInteger("Unbreaking");
        int newReinforced = newTags.getInteger("Unbreaking");
        newReinforced += currentReinforced - oldReinforced;
        tags.setInteger("Unbreaking", newReinforced);
        // reinforced tooltip is handled internally by tcon automagically

        // fiery from blaze arrow shafts, keep what the blaze modifier added after crafting
        int fiery = tags.getInteger("Fiery") + newTags.getInteger("Fiery") - originalTags.getInteger("Fiery");
        if (fiery > 0) tags.setInteger("Fiery", fiery);
        else tags.removeTag("Fiery");

        // material tooltips are handled by tcon internally

        // redstone modifier on bows
        reapplyWindup(tags, toolStack);
        // quartz/attack modifier
        reapplyAttack(tags, toolStack);
        // silktouch
        reapplySilktouch(tags, toolStack);

        boolean mobHeadRemoved = Config.removeMobHeadOnPartReplacement && type == HEAD
                && tags.hasKey("Mining Level Boost");
        if (mobHeadRemoved) removeMobHeadModifier(tags);

        if (tags.hasKey("GemBoost")) {
            // if boosted, remove boost tag
            // you're removing the head that had the reinforced tips or w/e after all
            if (type == HEAD) tags.removeTag("GemBoost");
            // the harvest level only comes from the head, so reapply the boost on top of the rebuilt one
            else tags.setInteger("HarvestLevel", tags.getInteger("HarvestLevel") + tags.getInteger("GemBoost"));
        }

        // if boosted by vanilla diamond modifier, then we have to respect that if vanilla tcon allows harvestlevel
        // change
        // check is either modifier-changed OR harvestlevels, because the changeDiamondModifier is basically false if
        // the pulse isn't loaded
        if (PHConstruct.miningLevelIncrease
                && (!Config.changeDiamondModifier || !TConstruct.pulsar.isPulseLoaded(Reference.PULSE_HARVESTTWEAKS))) {
            int min = 0;
            if (tags.getBoolean("Diamond")) {
                min = 3;
                if (TConstruct.pulsar.isPulseLoaded(Reference.PULSE_HARVESTTWEAKS)) min = HarvestLevels._6_obsidian;
            } else if (tags.getBoolean("Emerald")) {
                min = 2;
                if (TConstruct.pulsar.isPulseLoaded(Reference.PULSE_HARVESTTWEAKS)) min = HarvestLevels._5_diamond;
            }

            if (tags.getInteger("HarvestLevel") < min) tags.setInteger("HarvestLevel", min);
        }

        // handle Leveling/xp (has to be done first before we change the stats so we get the correct old values)
        if (LevelingLogic.hasXp(tags)) {
            float newRequiredXp = LevelingLogic.getRequiredXp(toolStack, tags);
            float xp = LevelingLogic.getXp(tags);

            // apply transition
            xp *= newRequiredXp / (float) oldRequiredXp;

            // xp penality?
            if (Config.partReplacementXpPenality > 0) {
                xp *= (100.0f - Config.partReplacementXpPenality) / 100.0f;
            }

            tags.setLong(LevelingLogic.TAG_EXP, Math.round(xp));
        }

        // handle boost leveling/xp
        if (LevelingLogic.hasBoostXp(tags)) {
            if (Config.levelingPickaxeBoost) {
                float newRequiredBoostXp = LevelingLogic.getRequiredBoostXp(toolStack);
                float xp = LevelingLogic.getBoostXp(tags);

                // apply transition
                xp *= newRequiredBoostXp / (float) oldRequiredBoostXp;

                // xp penality?
                if (Config.partReplacementBoostXpPenality > 0 && type == HEAD) {
                    if (LevelingLogic.isBoosted(tags)) {
                        tags.setBoolean(LevelingLogic.TAG_IS_BOOSTED, false);
                        xp = newRequiredBoostXp;
                    }

                    xp *= (100.0f - Config.partReplacementBoostXpPenality) / 100.0f;
                }

                tags.setLong(LevelingLogic.TAG_BOOST_EXP, Math.round(xp));
            }
            // without boost xp the boost can only come from the mob head, so it goes with it and can be reapplied
            else if (mobHeadRemoved) tags.setBoolean(LevelingLogic.TAG_IS_BOOSTED, false);

            // already full xp, or boosted by a mob head?
            if (LevelingLogic.isBoosted(tags)) tags.setInteger("HarvestLevel", tags.getInteger("HarvestLevel") + 1);
        }
        // The original tool did not have mining XP boost. Check if the new part should have it.
        else if (IguanaTweaksTConstruct.pulsar.isPulseLoaded(Reference.PULSE_LEVELING)) {
            if (LevelingLogic.hasBoostXp(newTags)) {
                tags.setInteger(LevelingLogic.TAG_BOOST_EXP, newTags.getInteger(LevelingLogic.TAG_BOOST_EXP));
                tags.setBoolean(LevelingLogic.TAG_IS_BOOSTED, newTags.getBoolean(LevelingLogic.TAG_IS_BOOSTED));
            }
        }

        // Update the tool name if we replaced the head and it was a automagic name
        if (type == HEAD) {
            String defaultName = ToolBuilder.defaultToolName(TConstructRegistry.getMaterial(oldMaterialId), tool);
            if (toolStack.getDisplayName().endsWith(defaultName)) {
                defaultName = ToolBuilder.defaultToolName(TConstructRegistry.getMaterial(partMaterialId), tool);
                toolStack.setStackDisplayName("\u00a7r" + defaultName);
            }
        }
    }

    // update tag if it already exists.
    // this saves us lots of hasFoo code,since there should be no general situation where a tag doesn't exist on the
    // target
    // tags the rebuilt tool doesn't have are removed, like the DrawSpeed older versions added to non-bows.
    // copying them would store null, which crashes when the tool is copied or saved
    private static void updateTag(NBTTagCompound from, NBTTagCompound to, String tag) {
        if (!to.hasKey(tag)) return;
        if (from.hasKey(tag)) to.setTag(tag, from.getTag(tag));
        else to.removeTag(tag);
    }

    // keeps what was added to a mining speed after crafting
    // redstone's bonus depends on how much was added at once, so it can't be reapplied exactly
    private static void updateMiningSpeed(ToolCore tool, NBTTagCompound tags, NBTTagCompound originalTags,
            NBTTagCompound newTags, String key, boolean silkTouchAffected) {
        if (!tags.hasKey(key)) return;

        int added = tags.getInteger(key) - originalTags.getInteger(key);
        // silk touch can't go below 0, so on slow materials the redstone bonus may be partly hidden.
        // estimate it as if it was added one by one instead
        if (silkTouchAffected && tags.hasKey("Silk Touch") && originalTags.getInteger(key) < 300)
            added = getRedstoneSpeedBonus(tool, tags) - 300;

        tags.setInteger(key, Math.max(newTags.getInteger(key) + added, 0));
    }

    // mining speed added by the redstone modifier if it was added one by one, see ModRedstone
    private static int getRedstoneSpeedBonus(ToolCore tool, NBTTagCompound tags) {
        if (!tags.hasKey("Redstone")) return 0;

        int partBonus = 0;
        if (tool.durabilityTypeHandle() == 2) partBonus += 2;
        if (tool.durabilityTypeAccessory() == 2) partBonus += 2;
        if (tool.durabilityTypeExtra() == 2) partBonus += 2;

        int redstone = tags.getIntArray("Redstone")[0];
        int bonus = 0;
        for (int i = 1; i <= redstone; i++) bonus += 8 + (i - 1) / 50 * 2 + partBonus;
        return bonus;
    }

    // strips the windup (redstone on bows) modifier off a tool and reapplies it (to get the correct draw speed)
    private static void reapplyWindup(NBTTagCompound tags, ItemStack itemStack) {
        // only do if we actually have redstone
        if (!tags.hasKey("Redstone")) return;

        int modifiers = tags.getInteger("Modifiers"); // backup modifiers

        // find the windup modifier, tools share the Redstone key but their speed is kept by updateMiningSpeed
        for (ItemModifier mod : ModifyBuilder.instance.itemModifiers) if (mod instanceof ModWindup) {
            ModWindup modWindup = (ModWindup) mod;
            if (!modWindup.validType((ToolCore) itemStack.getItem())) continue;
            int[] keyPair = tags.getIntArray("Redstone");
            // get amount of redstone applied
            int rLvl = keyPair[0];
            // reset redstone modifier
            tags.removeTag("Redstone");

            // remove the old tooltip
            int tipIndex = keyPair[2];
            tags.removeTag("Tooltip" + tipIndex);
            tags.removeTag("ModifierTip" + tipIndex);

            // reapply redstone
            // tags belong to oldTool
            while (rLvl-- > 0) modWindup.modify(new ItemStack[] { new ItemStack(Items.redstone) }, itemStack);
            break;
        }

        // restore modifiers
        tags.setInteger("Modifiers", modifiers);
    }

    // same as reapplyWindup but for Attack modifier
    private static void reapplyAttack(NBTTagCompound tags, ItemStack itemStack) {
        // only do if we actually have attack modifier
        if (!tags.hasKey("ModAttack")) return;

        int modifiers = tags.getInteger("Modifiers"); // backup modifiers

        ModAttack modAttack = TinkerTools.modAttack;
        // ammo weapons have their own modifier
        if (itemStack.getItem() instanceof ToolCore && TinkerWeaponry.modAttack != null
                && Arrays.asList(((ToolCore) itemStack.getItem()).getTraits()).contains("ammo"))
            modAttack = TinkerWeaponry.modAttack;

        int[] keyPair = tags.getIntArray("ModAttack");
        // get amount of redstone applied
        int qLvl = keyPair[0];
        // reset redstone modifier
        tags.removeTag("ModAttack");

        // remove the old tooltip
        int tipIndex = keyPair[2];
        tags.removeTag("Tooltip" + tipIndex);
        tags.removeTag("ModifierTip" + tipIndex);

        // reapply redstone
        while (qLvl-- > 0) modAttack.modify(new ItemStack[] { new ItemStack(Items.quartz) }, itemStack); // tags belong
                                                                                                         // to oldTool

        // restore modifiers
        tags.setInteger("Modifiers", modifiers);
    }

    // Reapplies the attack change done by the silktouch modifier, mining speed is kept by updateMiningSpeed
    private static void reapplySilktouch(NBTTagCompound tags, ItemStack itemStack) {
        // only do if we actually have attack modifier
        if (!tags.hasKey("Silk Touch")) return;

        int attack = tags.getInteger("Attack");
        attack -= 3;
        if (attack < 0) attack = 0;
        tags.setInteger("Attack", attack);
    }

    // removes the mobhead modifier and rendering
    private static void removeMobHeadModifier(NBTTagCompound tags) {
        // only if we actually are boosted by the modifier
        if (!tags.hasKey("Mining Level Boost")) return;

        // remove the modifier
        tags.removeTag("Mining Level Boost");

        // remove the rendering effect
        int i = 1;
        boolean removed = false;
        while (true) {
            String effect = "Effect" + i;
            i++;

            // we iterated over all effects
            if (!tags.hasKey(effect)) break;

            // if we removed the tag, but there are other effects left, we have to fill the gap
            if (removed) {
                tags.setInteger("Effect" + (i - 2), tags.getInteger(effect));
                tags.removeTag(effect);
                continue;
            }

            int effectId = tags.getInteger(effect);
            // mobheads and the nether star occupy id's 20 to 28. see IguanaToolLeveling.registerBoostModifiers
            if (effectId >= 20 && effectId <= 28) {
                tags.removeTag(effect);
                removed = true;
            }
        }

        // find the tooltip
        i = 1;
        removed = false;
        while (true) {
            String modTip = "ModifierTip" + i;
            String toolTip = "Tooltip" + i;
            i++;

            if (!tags.hasKey(modTip)) break;

            // same deal as above, fill the gap
            if (removed) {
                tags.setString("ModifierTip" + (i - 2), tags.getString(modTip));
                tags.setString("Tooltip" + (i - 2), tags.getString(toolTip));

                tags.removeTag(modTip);
                tags.removeTag(toolTip);
                continue;
            }

            if (tags.getString(modTip).endsWith("Mining Level Boost")) {
                tags.removeTag(modTip);
                tags.removeTag(toolTip);
                removed = true;
            }
        }

        // add a modifier for the removed modifier ;o
        if (Config.mobHeadRequiresModifier) tags.setInteger("Modifiers", tags.getInteger("Modifiers") + 1);
    }

    /**
     * Bolts are built from the dual material bolt core (head) and the fletching (handle).
     */
    public static PartTypes getBuildPartType(ToolCore tool, PartTypes type) {
        if (tool != TinkerWeaponry.boltAmmo) return type;
        return type == ACCESSORY ? HANDLE : HEAD;
    }

    /**
     * Builds the tool from its current parts, and with the given part exchanged. Building fires the regular craft
     * events, so material bonuses (paper, thaumium, magical wood, ...) are the same as on a freshly crafted tool.
     *
     * @param type Where the new part goes, as returned by {@link #getBuildPartType}
     * @return The original and the new tool, or null if either can't be built
     */
    public static ItemStack[] rebuildTool(ToolCore tool, NBTTagCompound tags, PartTypes type, ItemStack partStack) {
        ItemStack headStack = null;
        ItemStack handleStack = null;
        ItemStack accessoryStack = null;
        ItemStack extraStack = null;

        if (getPart(tool, HEAD) != null)
            headStack = new ItemStack(getPart(tool, HEAD), 1, getToolPartMaterial(tags, HEAD));
        if (getPart(tool, HANDLE) != null)
            handleStack = new ItemStack(getPart(tool, HANDLE), 1, getToolPartMaterial(tags, HANDLE));
        if (getPart(tool, ACCESSORY) != null)
            accessoryStack = new ItemStack(getPart(tool, ACCESSORY), 1, getToolPartMaterial(tags, ACCESSORY));
        if (getPart(tool, EXTRA) != null)
            extraStack = new ItemStack(getPart(tool, EXTRA), 1, getToolPartMaterial(tags, EXTRA));

        // extra hack for bolt crafting..
        if (tool == TinkerWeaponry.boltAmmo) {
            headStack = DualMaterialToolPart.createDualMaterial(
                    TinkerWeaponry.partBolt,
                    getToolPartMaterial(tags, HANDLE),
                    getToolPartMaterial(tags, HEAD));
            handleStack = accessoryStack;
            accessoryStack = null;
        }

        ItemStack originalTool = ToolBuilder.instance
                .buildTool(headStack, handleStack, accessoryStack, extraStack, "Original Tool");
        if (originalTool == null) return null;

        if (type == HEAD && headStack != null) headStack = partStack;
        if (type == HANDLE && handleStack != null) handleStack = partStack;
        if (type == ACCESSORY && accessoryStack != null) accessoryStack = partStack;
        if (type == EXTRA && extraStack != null) extraStack = partStack;

        ItemStack newTool = ToolBuilder.instance
                .buildTool(headStack, handleStack, accessoryStack, extraStack, "Modified Tool");
        if (newTool == null) return null;

        return new ItemStack[] { originalTool, newTool };
    }

    /**
     * Returns true if both tools are made of the same materials, e.g. a stick replacing a wooden tool rod.
     */
    public static boolean hasSameParts(NBTTagCompound tags, NBTTagCompound otherTags) {
        for (PartTypes type : PartTypes.values())
            if (getToolPartMaterial(tags, type) != getToolPartMaterial(otherTags, type)) return false;
        return true;
    }

    /**
     * Checks if the material has an extra modifier.
     *
     * @deprecated Only covers paper and thaumium, kept because TinkersGregworks still calls it. Use
     *             {@link #rebuildTool} and {@link #getResultingModifierCount} instead.
     */
    @Deprecated
    public static boolean hasExtraModifier(int materialId) {
        return materialId == TinkerTools.MaterialID.Paper || materialId == TinkerTools.MaterialID.Thaumium;
    }

    /**
     * Keeps the modifiers spent or gained since crafting, and applies the difference in crafting bonuses between the
     * original and the new tool.
     */
    public static int getResultingModifierCount(NBTTagCompound tags, NBTTagCompound originalTags,
            NBTTagCompound newTags) {
        return tags.getInteger("Modifiers") + newTags.getInteger("Modifiers") - originalTags.getInteger("Modifiers");
    }

    /**
     * Returns the type-part of the tool.
     */
    public static Item getPart(ToolCore tool, PartTypes type) {
        switch (type) {
            case HEAD:
                return tool.getHeadItem();
            case HANDLE:
                return tool.getHandleItem();
            case ACCESSORY:
                return tool.getAccessoryItem();
            case EXTRA:
                return tool.getExtraItem();
        }

        return null;
    }

    /**
     * Returns the material id the tool-component is made of.
     * 
     * @param tags InfiTool Tagcompount of the tool
     */
    public static int getToolPartMaterial(NBTTagCompound tags, PartTypes type) {
        String key = null;
        switch (type) {
            case HEAD:
                key = "Head";
                break;
            case HANDLE:
                key = "Handle";
                break;
            case ACCESSORY:
                key = "Accessory";
                break;
            case EXTRA:
                key = "Extra";
                break;
        }

        if (key == null) return 0;

        return tags.getInteger(key);
    }

    /**
     * Check if another part type could be employed. basically slot-id = part-placement.
     * 
     * @param recipe      Recipe that should be checked
     * @param part        The new part that replaces an old one.
     * @param currentType Where the part would currently be replaced. Call multiple times with the previous result to
     *                    check all.
     * @return The new position that's applicable, or the same if nothing new has been detected.
     */
    public static PartTypes detectAdditionalPartType(ToolRecipe recipe, Item part, PartTypes currentType) {
        PartTypes tmpType = currentType;
        if (tmpType == null) {
            if (recipe.validHead(part)) return HEAD;
            tmpType = HEAD;
        }
        if (tmpType == HEAD) {
            if (recipe.validHandle(part)) return HANDLE;
            tmpType = HANDLE;
        }
        if (tmpType == HANDLE) {
            if (recipe.validAccessory(part)) return ACCESSORY;
            tmpType = ACCESSORY;
        }
        if (tmpType == ACCESSORY) {
            if (recipe.validExtra(part)) return EXTRA;
        }

        return currentType;
    }

    public enum PartTypes {
        HEAD,
        HANDLE,
        ACCESSORY,
        EXTRA
    }
}
