package iguanaman.iguanatweakstconstruct.leveling;

import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.ACCESSORY;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.EXTRA;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.HANDLE;
import static iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic.PartTypes.HEAD;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import iguanaman.iguanatweakstconstruct.override.XPAdjustmentMap;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.reference.Reference;
import iguanaman.iguanatweakstconstruct.replacing.ReplacementLogic;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import tconstruct.items.tools.Cleaver;
import tconstruct.items.tools.Excavator;
import tconstruct.items.tools.Hammer;
import tconstruct.items.tools.Hatchet;
import tconstruct.items.tools.LumberAxe;
import tconstruct.items.tools.Mattock;
import tconstruct.items.tools.Pickaxe;
import tconstruct.items.tools.Scythe;
import tconstruct.items.tools.Shovel;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolCore;

/**
 * Utility class that takes care of all the Tool XP related things. Basically how leveling works: - You get XP for doing
 * stuff with the tool - On levelup, you gain additional modifiers (according to configuration) - On levelup, you gain
 * random bonus modifiers (according to configuration) - If pick-boosting is enabled, all the xp you gain also fills a
 * secondary xp-bar, the mining-boost-xp - When your mining-boost-xp is full, your mining level is increased by 1. Only
 * works once per pick.
 */
public final class LevelingLogic {

    public static final String TAG_EXP = "ToolEXP";
    public static final String TAG_LEVEL = "ToolLevel";
    public static final String TAG_BOOST_EXP = "HeadEXP"; // HeadEXP for downwards compatibility
    public static final String TAG_IS_BOOSTED = "HarvestLevelModified";

    private LevelingLogic() {} // non-instantiable

    public static int getLevel(NBTTagCompound tags) {
        return tags.getInteger(TAG_LEVEL);
    }

    public static int getHarvestLevel(NBTTagCompound tags) {
        return tags.hasKey("HarvestLevel") ? tags.getInteger("HarvestLevel") : -1;
    }

    public static long getXp(NBTTagCompound tags) {
        return tags.getLong(TAG_EXP);
    }

    public static long getBoostXp(NBTTagCompound tags) {
        return tags.getLong(TAG_BOOST_EXP);
    }

    public static boolean hasLevel(NBTTagCompound tags) {
        return tags.hasKey(TAG_LEVEL);
    }

    public static boolean hasXp(NBTTagCompound tags) {
        return tags.hasKey(TAG_EXP);
    }

    public static boolean hasBoostXp(NBTTagCompound tags) {
        return tags.hasKey(TAG_BOOST_EXP);
    }

    public static boolean isBoosted(NBTTagCompound tags) {
        return tags.getBoolean(TAG_IS_BOOSTED);
    }

    public static boolean isMaxLevel(NBTTagCompound tags) {
        return getLevel(tags) >= Config.maxToolLevel;
    }

    /**
     * can only be boosted if: - tool was created while pick boosting was active - tool hasn't been boosted yet - tool
     * doesn't have max mining level already
     */
    public static boolean canBoostMiningLevel(NBTTagCompound tags) {
        return tags.hasKey(TAG_IS_BOOSTED) && !isBoosted(tags) && getHarvestLevel(tags) < HarvestLevels.max;
    }

    /**
     * Add the leveling specific NBT.
     * 
     * @param tag The tag that should recieve the data. Usually InfiTool Tag.
     */
    public static void addLevelingTags(NBTTagCompound tag, ToolCore tool) {
        // we start with level 1
        tag.setInteger(TAG_LEVEL, 1);
        // and no xp :(
        tag.setLong(TAG_EXP, 0);

        // mining level boost
        addBoostTags(tag, tool);
    }

    public static void addBoostTags(NBTTagCompound tag, ToolCore tool) {
        int hlvl = tag.getInteger("HarvestLevel");
        if (!Config.pickaxeBoostRequired) return;
        if (hlvl == 0 || !(tool instanceof Pickaxe || tool instanceof Hammer)) return;

        tag.setLong(TAG_BOOST_EXP, 0);
        tag.setBoolean(TAG_IS_BOOSTED, false);

        // reduce harvestlevel by 1
        tag.setInteger("HarvestLevel", hlvl - 1);
    }

    /**
     * Updates the tool information with the given tool and boost xp. This SETS the xp!
     * 
     * @param player  Required for awesome *ding* sound
     * @param toolXP  Value the tool XP shall be set to. -1 for no change.
     * @param boostXP Value the mining-boost XP shall be set to. -1 for no change.
     */
    public static void updateXP(ItemStack tool, EntityPlayer player, long toolXP, long boostXP) {
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        if (!hasLevel(tags)) return;

        int level = getLevel(tags);

        boolean leveled = false;
        boolean pickLeveled = false;

        // Update Tool XP
        if (toolXP >= 0 && hasXp(tags) && level > 0 && !isMaxLevel(tags) && Config.toolLeveling) {
            // set new xp value
            tags.setLong(TAG_EXP, toolXP);

            // check for levelup
            if (toolXP >= getRequiredXp(tool, tags)) {
                // anti cheater check!
                int cxp = tags.getInteger("CheatyXP");
                if (cxp * 2 >= getRequiredXp(tool, tags)) {
                    // you just got rubber chicken'd
                    if (player != null && !player.worldObj.isRemote) {
                        String text = StatCollector.translateToLocal("message.levelup.chicken");
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_RED + text));
                        player.worldObj.playSoundAtEntity(player, Reference.RESOURCE + ":chicken", 0.9f, 1.0f);
                    }

                    tags.setString("Original", Item.itemRegistry.getNameForObject(tool.getItem()));
                    // rubber chicken yaaaaay
                    tool.func_150996_a(IguanaToolLeveling.rubberChicken);
                    tags.setLong(TAG_EXP, 0L);
                    tags.setInteger("CheatyXP", 0);

                    if (player != null && player.worldObj.isRemote) {
                        for (int i = 0; i < 1337; i++) {
                            player.worldObj.spawnEntityInWorld(
                                    new EntityXPOrb(player.worldObj, player.posX, player.posY, player.posZ, 1));
                        }
                    }

                    return;
                }

                levelUpTool(tool, player);
                leveled = true;
            }
        }

        // handle mining boost XP
        if (Config.levelingPickaxeBoost) {
            // we can only if we have a proper material (>stone) and are not max mining level already
            if (canBoostMiningLevel(tags)) {
                tags.setLong(TAG_BOOST_EXP, boostXP);

                // check for mining boost levelup!
                if (boostXP >= getRequiredBoostXp(tool)) {
                    levelUpMiningLevel(tool, player, leveled);

                    pickLeveled = true;
                }
            }
        }

        // if we got a levelup, play a sound!
        if ((leveled || pickLeveled) && !player.worldObj.isRemote)
            player.worldObj.playSoundAtEntity(player, Reference.RESOURCE + ":chime", 0.9f, 1.0f);
    }

    public static void addXP(ItemStack tool, EntityPlayer player, long xp) {
        if (player.capabilities.isCreativeMode) return;
        if (tool == null || !tool.hasTagCompound()) return;

        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");

        // only if we have a level or xp
        if (!hasLevel(tags) || !hasXp(tags)) return;

        // tool EXP
        Long toolXp = -1L;
        if (hasXp(tags)) toolXp = getXp(tags) + xp;

        // mininglevel boost EXP
        Long boostXp = -1L;
        if (hasBoostXp(tags)) boostXp = getBoostXp(tags) + xp;

        // update the tool information
        updateXP(tool, player, toolXp, boostXp);
    }

    public static int getRequiredBoostXp(ItemStack tool) {
        return getRequiredXp(tool, null, true);
    }

    public static int getRequiredXp(ItemStack tool, NBTTagCompound tags) {
        return getRequiredXp(tool, tags, false);
    }

    protected static int getRequiredXp(ItemStack tool, NBTTagCompound tags, boolean miningBoost) {
        if (tags == null) tags = tool.getTagCompound().getCompoundTag("InfiTool");

        float base = 100f;

        boolean harvest, weapon, bow;
        harvest = weapon = bow = false;
        if (tool.getItem() instanceof ToolCore) for (String trait : ((ToolCore) tool.getItem()).getTraits()) {
            if ("bow".equals(trait)) bow = true;
            if ("weapon".equals(trait)) weapon = true;
            if ("harvest".equals(trait)) harvest = true;
        }

        if (bow) {
            base = 200f;
            base *= tags.getFloat("FlightSpeed") * 0.7f;

            base *= Config.xpRequiredWeaponsPercentage / 100f;
        } else if (weapon && !(tool.getItem() instanceof Hammer)) {
            base = 140f;
            base *= ((ToolCore) tool.getItem()).getDamageModifier();
            base *= Math.max(1, tags.getInteger("Attack")) * 1.2f;

            if (tool.getItem() instanceof Scythe) base *= 1.5f;
            base *= Config.xpRequiredWeaponsPercentage / 100f;
        } else {
            if (tags.hasKey("HarvestLevel") && LevelingLogic.getHarvestLevel(tags) < 1) base -= 20;
            if (tags.hasKey("HarvestLevel") && LevelingLogic.getHarvestLevel(tags) < 2) base -= 15;

            // main-head mining speed
            int baseMiningSpeed = TConstructRegistry.getMaterial(tags.getInteger("Head")).toolSpeed();
            int miningSpeed = tags.getInteger("MiningSpeed");
            // and mining speeds of additional heads.
            float divider = 2.4f;
            if (tags.hasKey("MiningSpeed2")) {
                baseMiningSpeed += TConstructRegistry.getMaterial(tags.getInteger("Accessory")).toolSpeed();
                miningSpeed += tags.getInteger("MiningSpeed2");
                divider += 1;
            }
            if (tags.hasKey("MiningSpeedHandle")) {
                baseMiningSpeed += TConstructRegistry.getMaterial(tags.getInteger("Handle")).toolSpeed();
                miningSpeed += tags.getInteger("MiningSpeedHandle");
                divider += 1;
            }
            if (tags.hasKey("MiningSpeedExtra")) {
                baseMiningSpeed += TConstructRegistry.getMaterial(tags.getInteger("Extra")).toolSpeed();
                miningSpeed += tags.getInteger("MiningSpeedExtra");
                divider += 1;
            }

            base += ((float) baseMiningSpeed + (float) (miningSpeed - baseMiningSpeed) / 5f) / divider;

            // shovels need a bit more xp because their blocks break much faster

            if (tool.getItem() instanceof Hammer) base *= 5.1f;
            if (tool.getItem() instanceof Excavator) base *= 6.2f;
            if (tool.getItem() instanceof LumberAxe) base *= 1.38f;
            if (tool.getItem() instanceof Shovel) base *= 1.2f; // shovels break their blocks faster than picks
            if (tool.getItem() instanceof Hatchet) base *= 0.66f; // not much wood to chop, but usable as weapon

            base *= Config.xpRequiredToolsPercentage / 100f;
        }

        if (miningBoost) {
            int harvestLevelCopper = HarvestLevels._2_copper;
            int harvestLevel = getHarvestLevel(tags);
            if (harvestLevel >= harvestLevelCopper)
                base *= Math.pow(Config.xpPerBoostLevelMultiplier, harvestLevel - harvestLevelCopper);
            if (harvestLevel == 0) base /= Config.xpPerBoostLevelMultiplier * Config.xpPerBoostLevelMultiplier;

            base *= Config.levelingPickaxeBoostXpPercentage / 100f;
        } else {
            int level = tags.getInteger("ToolLevel");
            if (level >= 1) base *= Math.pow(Config.xpPerLevelMultiplier, level - 1);
            if (tags.hasKey("HarvestLevel") && LevelingLogic.getHarvestLevel(tags) == 0)
                base /= Config.xpPerLevelMultiplier * Config.xpPerLevelMultiplier;

            // XP Multiplier applies to all tools, but not to "mining boost" XP.
            float xpMultiplier = getXPMultiplier(tool, tags);

            base *= xpMultiplier;
        }

        return Math.round(base);
    }

    private static float getXPMultiplier(ItemStack tool, NBTTagCompound tags) {
        boolean nonHeadsCount = !Config.onlyHeadsChangeXPRequirement;
        ToolCore core = (ToolCore) tool.getItem();

        boolean extraIsHead = (core instanceof Hammer);

        boolean accessoryIsHead = (extraIsHead || core instanceof Excavator
                || core instanceof Cleaver
                || core instanceof LumberAxe
                || core instanceof Mattock);

        double numberOfParts = 0;
        double xpModSoFar = 1;

        if (ReplacementLogic.getPart(core, HEAD) != null) {
            numberOfParts++;
            int toolMaterialHead = ReplacementLogic.getToolPartMaterial(tags, HEAD);
            String matName = TConstructRegistry.getMaterial(toolMaterialHead).name();
            xpModSoFar *= XPAdjustmentMap.get(matName);
        }
        if (ReplacementLogic.getPart(core, HANDLE) != null && nonHeadsCount) {
            numberOfParts++;
            int toolMaterialHandle = ReplacementLogic.getToolPartMaterial(tags, HANDLE);
            String matName = TConstructRegistry.getMaterial(toolMaterialHandle).name();
            xpModSoFar *= XPAdjustmentMap.get(matName);
        }
        if (ReplacementLogic.getPart(core, ACCESSORY) != null && (accessoryIsHead || nonHeadsCount)) {
            numberOfParts++;
            int toolMaterialAccessory = ReplacementLogic.getToolPartMaterial(tags, ACCESSORY);
            String matName = TConstructRegistry.getMaterial(toolMaterialAccessory).name();
            xpModSoFar *= XPAdjustmentMap.get(matName);
        }
        if (ReplacementLogic.getPart(core, EXTRA) != null && (extraIsHead || nonHeadsCount)) {
            numberOfParts++;
            int toolMaterialExtra = ReplacementLogic.getToolPartMaterial(tags, EXTRA);
            String matName = TConstructRegistry.getMaterial(toolMaterialExtra).name();
            xpModSoFar *= XPAdjustmentMap.get(matName);
        }

        // Take the geometric mean
        return (float) Math.pow(xpModSoFar, 1.0 / numberOfParts);
    }

    /**
     * Applies all the logic for increasing the tool level. This is only specific to the *tool* level, and has no
     * relation to the mining-level-boost!
     */
    public static void levelUpTool(ItemStack stack, EntityPlayer player) {
        NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
        World world = player.worldObj;

        // *ding* levelup!
        int level = getLevel(tags);
        level++;

        // tell the player how awesome he is
        if (!world.isRemote) {
            // special message
            if (StatCollector.canTranslate("message.levelup." + level)) player.addChatMessage(
                    new ChatComponentText(
                            EnumChatFormatting.DARK_AQUA + StatCollector.translateToLocalFormatted(
                                    "message.levelup." + level,
                                    stack.getDisplayName() + EnumChatFormatting.DARK_AQUA)));
            // generic message
            else player.addChatMessage(
                    new ChatComponentText(
                            EnumChatFormatting.DARK_AQUA + StatCollector.translateToLocalFormatted(
                                    "message.levelup.generic",
                                    stack.getDisplayName() + EnumChatFormatting.DARK_AQUA,
                                    LevelingTooltips.getLevelString(level))));
        }

        // Add random bonuses on leveling up?
        // this is done first so the extra-chance can be incorporated correctly
        if (Config.toolLevelingRandomBonuses) {
            int bonusesToAdd = 0;
            for (int lvl : Config.randomBonusesAtlevels) if (level == lvl) bonusesToAdd++;

            while (bonusesToAdd-- > 0) RandomBonuses.tryModifying(player, stack);
        }

        // and NOW save the change
        tags.setInteger(TAG_LEVEL, level);

        // reset tool xp to 0, since we're at a new level now
        tags.setLong(TAG_EXP, 0L);

        int currentModifiers = tags.getInteger("Modifiers");

        // Add Modifier for leveling up?
        int modifiersToAdd = 0;
        // check if we are supposed to add a modifier at this levelup
        for (int lvl : Config.toolModifiersAtLevels) if (level == lvl) modifiersToAdd++;
        // yes, no break. this means if a level is in the list multiple times, you get multiple modifiers

        if (modifiersToAdd > 0) {
            currentModifiers += modifiersToAdd;
            tags.setInteger("Modifiers", currentModifiers);

            // fancy message on clientside
            if (!world.isRemote) {
                if (world.rand.nextInt(10) < modifiersToAdd) player.addChatMessage(
                        new ChatComponentText(
                                LevelingTooltips.getInfoString(
                                        StatCollector.translateToLocal("message.levelup.newmodifier.2"),
                                        EnumChatFormatting.DARK_AQUA,
                                        String.format(
                                                "+%d %s",
                                                modifiersToAdd,
                                                StatCollector.translateToLocal("message.levelup.modifier")),
                                        EnumChatFormatting.GOLD)));
                else player.addChatMessage(
                        new ChatComponentText(
                                LevelingTooltips.getInfoString(
                                        StatCollector.translateToLocal("message.levelup.newmodifier.1"),
                                        EnumChatFormatting.DARK_AQUA,
                                        String.format(
                                                "+%d %s",
                                                modifiersToAdd,
                                                StatCollector.translateToLocal("message.levelup.modifier")),
                                        EnumChatFormatting.GOLD)));
            }
        }
    }

    public static void levelUpMiningLevel(ItemStack stack, EntityPlayer player, boolean leveled) {
        NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");

        // we only apply that once
        if (isBoosted(tags)) return;

        // reset miningboost xp to 0
        if (hasBoostXp(tags)) tags.setLong(TAG_BOOST_EXP, 0L);

        // fancy message
        if (player != null) {
            if (!player.worldObj.isRemote) {
                player.addChatMessage(
                        new ChatComponentText(
                                LevelingTooltips.getInfoString(
                                        StatCollector.translateToLocalFormatted(
                                                "message.levelup.miningboost",
                                                stack.getDisplayName()),
                                        EnumChatFormatting.DARK_AQUA,
                                        String.format(
                                                "+%d %s",
                                                1,
                                                StatCollector.translateToLocal("message.levelup.mininglevel")),
                                        EnumChatFormatting.GOLD)));
            }
        }

        tags.setBoolean(TAG_IS_BOOSTED, true);
        // increase harvest level by 1
        tags.setInteger("HarvestLevel", tags.getInteger("HarvestLevel") + 1);
    }

    /*
     * private static boolean tryModify(EntityPlayer player, ItemStack stack, int rnd, boolean isTool) { ItemModifier
     * mod = null; Item prefix = stack.getItem(); ItemStack[] nullItemStack = new ItemStack[] {}; if (rnd < 1) { mod =
     * new ModInteger(nullItemStack, 4, "Moss", Config.mossRepairSpeed, "\u00a72", "Auto-Repair"); if
     * (!player.worldObj.isRemote) player.addChatMessage(new
     * ChatComponentText("\u00a79It seems to have accumulated a patch of moss (+1 repair)")); } else if (rnd < 2 &&
     * (!isTool && !(prefix instanceof Shortbow) || isTool && (prefix instanceof Pickaxe || prefix instanceof Hammer)))
     * { mod = new IguanaModLapis(nullItemStack, 10, new int[]{100}); if (((IguanaModLapis)mod).canModify(stack,
     * nullItemStack)) { if (!player.worldObj.isRemote) player.addChatMessage(new
     * ChatComponentText("\u00a79Perhaps holding on to it will bring you luck? (+100 luck)")); } else return false; }
     * else if (rnd < 6 && (isTool || prefix instanceof Shortbow)) { mod = new IguanaModRedstone(nullItemStack, 2, 50);
     * if (((IguanaModRedstone)mod).canModify(stack, nullItemStack, true)) { if (!player.worldObj.isRemote)
     * player.addChatMessage(new ChatComponentText("\u00a79You spin it around with a flourish (+1 haste)")); } else
     * return false; } else if (rnd < 3 && !isTool && !(prefix instanceof Shortbow)) { mod = new
     * IguanaModAttack("Quartz", nullItemStack, 11, 30); if (((IguanaModAttack)mod).canModify(stack, nullItemStack,
     * true)) { if (!player.worldObj.isRemote) player.addChatMessage(new
     * ChatComponentText("\u00a79You take the time to sharpen the dull edges of the blade (+1 attack)")); } else return
     * false; } else if (rnd < 4 && !isTool && !(prefix instanceof Shortbow)) { mod = new ModInteger(nullItemStack, 13,
     * "Beheading", 1, "\u00a7d", "Beheading"); if (!player.worldObj.isRemote) player.addChatMessage(new
     * ChatComponentText("\u00a79You could take someones head off with that! (+1 beheading)")); } else if (rnd < 5 &&
     * !isTool && !(prefix instanceof Shortbow)) { mod = new IguanaModBlaze(nullItemStack, 7, new int[]{25}); if
     * (((IguanaModBlaze)mod).canModify(stack, nullItemStack)) { if (!player.worldObj.isRemote)
     * player.addChatMessage(new ChatComponentText("\u00a79It starts to feels more hot to the touch (+1 fire aspect)"));
     * } else return false; } else if (rnd < 6 && !isTool && !(prefix instanceof Shortbow)) { mod = new
     * ModInteger(nullItemStack, 8, "Necrotic", 1, "\u00a78", "Life Steal"); if (!player.worldObj.isRemote)
     * player.addChatMessage(new ChatComponentText("\u00a79It shudders with a strange energy (+1 life steal)")); } else
     * if (rnd < 7 && !isTool && !(prefix instanceof Shortbow)) { mod = new ModSmite("Smite", 14, nullItemStack, new
     * int[]{ 36}); if (!player.worldObj.isRemote) player.addChatMessage(new
     * ChatComponentText("\u00a79It begins to radiate a slight glow (+1 smite)")); } else if (rnd < 8 && !isTool &&
     * !(prefix instanceof Shortbow)) { mod = new ModAntiSpider("Anti-Spider",15, nullItemStack, new int[]{ 4}); if
     * (!player.worldObj.isRemote) player.addChatMessage(new
     * ChatComponentText("\u00a79A strange odor emanates from the weapon (+1 bane of arthropods)")); } else if (rnd < 9
     * && !isTool) { mod = new IguanaModPiston(nullItemStack, 3, new int[]{10}); if
     * (((IguanaModPiston)mod).canModify(stack, nullItemStack)) { if (!player.worldObj.isRemote)
     * player.addChatMessage(new
     * ChatComponentText("\u00a79Feeling more confident, you can more easily keep your assailants at bay (+1 knockback)"
     * )); } else return false; } else if (rnd < 10) { mod = new ModReinforced(nullItemStack, 16, 1); if
     * (!player.worldObj.isRemote) player.addChatMessage(new
     * ChatComponentText("\u00a79Fixing up the wear and tear should make it last a little longer (+1 reinforced)")); }
     * if (mod == null) return false; mod.addMatchingEffect(stack); mod.modify(nullItemStack, stack); return true; }
     */
}
