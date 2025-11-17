package iguanaman.iguanatweakstconstruct.reference;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import iguanaman.iguanatweakstconstruct.leveling.RandomBonuses;

public class Config {

    private Configuration configfile;

    // leveling
    public static int maxToolLevel;
    public static int xpRequiredToolsPercentage;
    public static int xpRequiredWeaponsPercentage;
    public static float xpPerLevelMultiplier;
    public static boolean showTooltipXP;
    public static boolean showMinimalTooltipXP;
    public static boolean detailedXpTooltip;
    public static boolean toolLeveling;
    public static int toolLevelingExtraModifiers;
    public static int[] toolModifiersAtLevels;
    public static boolean toolLevelingRandomBonuses;
    public static int[] randomBonusesAtlevels;
    public static boolean randomBonusesAreUseful;
    public static boolean randomBonusesAreRandom;
    public static boolean onlyHeadsChangeXPRequirement;
    public static boolean allowFakePlayerLeveling;

    // random bonuses deactivation
    public static Set<RandomBonuses.Modifier> deactivatedModifiers = new HashSet<RandomBonuses.Modifier>();

    // pick boost
    public static boolean pickaxeBoostRequired;
    public static boolean mobHeadPickaxeBoost;
    public static boolean mobHeadRequiresModifier;
    public static boolean levelingPickaxeBoost;
    public static int levelingPickaxeBoostXpPercentage;
    public static float xpPerBoostLevelMultiplier;

    // Harvest Leveling
    public static boolean changeDiamondModifier;
    public static int durabilityPercentage;
    public static int miningSpeedPercentage;

    // part replacement
    public static boolean removeMobHeadOnPartReplacement;
    public static int partReplacementXpPenality;
    public static int partReplacementBoostXpPenality;

    // heads
    public static int baseHeadDropChance;
    public static int beheadingHeadDropChance;
    public static boolean headsFromFakePlayerKills;

    // tweaks
    public static boolean nerfVanillaTools;
    public static boolean nerfVanillaHoes;
    public static boolean nerfVanillaSwords;
    public static boolean nerfVanillaBows;
    public static boolean removeFlintDrop;
    public static boolean addFlintRecipe;
    public static int recipeGravelPerFlint;
    public static boolean disableStoneTools;
    public static boolean disableBonusMods;
    public static boolean castsBurnMaterial;
    public static boolean allowStringBinding;
    public static boolean removeObsidianAlloy;

    public static boolean easyToolRepair;
    public static boolean easyPartCrafting;
    public static boolean easyToolBuilding;
    public static boolean easyAdvancedToolBuilding;

    public static boolean allowStencilReuse;
    public static boolean allowPartReuse;
    public static boolean removeStoneTorchRecipe;
    public static boolean moreExpensiveSilkyCloth;
    public static boolean moreExpensiveSilkyJewel;
    public static boolean moreModifiersForFlux;
    public static int maxToolRepairs;
    // public static float repairAmountMultiplier;

    // allowed tools that should not be nerfed
    public static boolean excludedToolsIsWhitelist;
    public static Set<String> excludedTools = new HashSet<String>();
    public static Set<String> excludedModTools = new HashSet<String>();

    // debug
    public static boolean showDebugXP;
    public static boolean logHarvestLevelChanges;
    public static boolean logMiningLevelChanges;
    public static boolean logToolMaterialChanges;
    public static boolean logBonusExtraChance;
    public static boolean logOverrideChanges;

    public void init(File file) {
        configfile = new Configuration(file);
        configfile.load();

        sync();
    }

    public void sync() {
        final String CATEGORY_Leveling = "ToolLeveling";
        final String CATEGORY_Bonuses = "RandomBonuses";
        final String CATEGORY_PickLeveling = "PickLeveling";
        final String CATEGORY_HarvestLevels = "HarvestLevelTweaks";
        final String CATEGORY_PartReplacement = "PartReplacement";
        final String CATEGORY_Heads = "MobHeads";
        final String CATEGORY_Tweaks = "Tweaks";
        final String CATEGORY_AllowedTools = "AllowedTools";
        final String CATEGORY_Debug = "Debug";

        /** Leveling **/
        configfile.setCategoryComment(CATEGORY_Leveling, "Leveling Module: Setup the leveling system how you like it");

        // modifying leveling behaviour
        xpRequiredToolsPercentage = configfile.getInt(
                "xpRequiredToolsPercentage",
                CATEGORY_Leveling,
                100,
                1,
                999,
                "Change the XP required to level up tools in % (higher = more xp needed)");
        xpRequiredWeaponsPercentage = configfile.getInt(
                "xpRequiredWeaponsPercentage",
                CATEGORY_Leveling,
                100,
                1,
                999,
                "Change the XP required to level up weapons in % (higher = more xp needed)");
        xpPerLevelMultiplier = configfile.getFloat(
                "xpPerLevelMultiplier",
                CATEGORY_Leveling,
                1.15f,
                1.0f,
                9.99f,
                "Exponential multiplier for required xp per level");
        onlyHeadsChangeXPRequirement = configfile.getBoolean(
                "onlyHeadsChangeXPRequirement",
                CATEGORY_Leveling,
                true,
                "If true, only the heads of tools are examined when determining how much XP it takes to level up. (This only matters if you manually specify that some material types level faster than others using the override module)");
        allowFakePlayerLeveling = configfile.getBoolean(
                "allowFakePlayerLeveling",
                CATEGORY_Leveling,
                true,
                "Allow tool leveling through fake players");

        // tooltip things
        showTooltipXP = configfile
                .getBoolean("showTooltipXP", CATEGORY_Leveling, true, "Current XP is shown when hovering over a tool");
        detailedXpTooltip = configfile.getBoolean(
                "detailedXpTooltip",
                CATEGORY_Leveling,
                true,
                "XP tooltip shows numbers, in addition to percentage");
        showMinimalTooltipXP = configfile
                .getBoolean("showMinimalTooltipXP", CATEGORY_Leveling, false, "Current XP% is shown after the level");

        // levelup behaviour
        maxToolLevel = configfile.getInt("maxToolLevel", CATEGORY_Leveling, 6, 1, 99, "");
        toolLeveling = configfile.getBoolean(
                "toolLeveling",
                CATEGORY_Leveling,
                true,
                "Can your skill with tools 'level up' as you use them?");
        toolLevelingExtraModifiers = configfile
                .getInt("ExtraModifiers", CATEGORY_Leveling, 0, 0, 9, "The amount of modifiers new tools have.");
        toolModifiersAtLevels = configfile.get(
                CATEGORY_Leveling,
                "ModifiersAtLevels",
                new int[] { 2, 4, 6 },
                "Adds an extra modifier on these levelups if 'ExtraModifiers' is enabled").getIntList();
        toolLevelingRandomBonuses = configfile.getBoolean(
                "RandomBonuses",
                CATEGORY_Leveling,
                true,
                "Gives a random bonus every level, if false and levelling is on modifiers are given at levels 2 and 4 (requires 'toolLeveling=true')");
        randomBonusesAtlevels = configfile.get(
                CATEGORY_Leveling,
                "BonusesAtLevels",
                new int[] { 2, 3, 4, 5, 6 },
                "Adds a random bonus on these levelups if 'RandomBonuses' is enabled").getIntList();
        randomBonusesAreUseful = configfile.getBoolean(
                "UsefulBonuses",
                CATEGORY_Leveling,
                true,
                "Disables less-useful modifiers on levelups. Like a sword with silktouch, or a pickaxe with beheading.");
        randomBonusesAreRandom = configfile.getBoolean(
                "CompletelyRandomBonuses",
                CATEGORY_Leveling,
                false,
                "Each modifier is equally likely on levelup. Disables useful bonuses.");

        /** Random Bonuses **/
        configfile.setCategoryComment(
                CATEGORY_Bonuses,
                "Leveling Module: Allows to completely deactivate specific modifiers on levelup.");

        for (RandomBonuses.Modifier mod : RandomBonuses.Modifier.values()) {
            // we use this way of obtaining the values because it doesn't create the empty lines and comments
            // otherwise this blows up the config. a lot.
            Property allowed = configfile.get(CATEGORY_Bonuses, String.format("allow%s", mod.toString()), true);
            if (!allowed.getBoolean()) deactivatedModifiers.add(mod);
        }

        /** Pickaxe Boosting **/
        configfile.setCategoryComment(
                CATEGORY_PickLeveling,
                "Leveling Module: Allows pickaxes to gain a mining level with enough XP. Should be used with the HarvestLevel Module.");

        // pick boosting behaviour
        pickaxeBoostRequired = configfile.getBoolean(
                "pickaxeBoostRequired",
                CATEGORY_PickLeveling,
                true,
                "Every Pickaxes Mining Level is reduced by 1 and needs a mining levelup (separate from tool level) or, if enabled, a mob head modifier to advance");
        levelingPickaxeBoost = configfile.getBoolean(
                "allowLevelingBoost",
                CATEGORY_PickLeveling,
                true,
                "Pickaxes gain Mining Xp by using the pickaxe.");
        mobHeadPickaxeBoost = configfile.getBoolean(
                "addMobHeadBoost",
                CATEGORY_PickLeveling,
                true,
                "Mob heads can be used to boost a pickaxe's mining xp.");
        mobHeadRequiresModifier = configfile.getBoolean(
                "mobHeadBoostNeedsModifier",
                CATEGORY_PickLeveling,
                false,
                "Mob head boosting requires a free modifier");

        levelingPickaxeBoostXpPercentage = configfile.getInt(
                "xpRequiredPickBoostPercentage",
                CATEGORY_PickLeveling,
                100,
                1,
                999,
                "Change the percentage of XP required to boost a pick (i.e. 200 means 2x normal boost xp required)");
        xpPerBoostLevelMultiplier = configfile.getFloat(
                "xpPerBoostLevelMultiplier",
                CATEGORY_Leveling,
                1.12f,
                1.0f,
                9.99f,
                "Exponential multiplier for required boost xp per level");

        /** HarvestLevel Module **/
        configfile.setCategoryComment(
                CATEGORY_HarvestLevels,
                "Harvest Level Tweak Module: Introduces a slower mining level progression.");

        // changed diamond/emerald modifier
        changeDiamondModifier = configfile.getBoolean(
                "diamondRequired",
                CATEGORY_HarvestLevels,
                true,
                "Changes the Diamond and Emerald modifier: Apply it to a bronze level tool to obtain diamond level. Required unless you have steel or similar.");

        // Tool durability/speed changes
        durabilityPercentage = configfile.getInt(
                "durabilityPercentage",
                CATEGORY_HarvestLevels,
                80,
                1,
                999,
                "Change durability of all tool materials (in percent)");
        miningSpeedPercentage = configfile.getInt(
                "miningSpeedPercentage",
                CATEGORY_HarvestLevels,
                100,
                1,
                999,
                "Change mining speed of all tool materials (in percent)");

        /** PartReplacement Module **/
        removeMobHeadOnPartReplacement = configfile.getBoolean(
                "removeMobHead",
                CATEGORY_PartReplacement,
                true,
                "Removes the Mob Head Modifier on Tool-Head replacement, allowing it to be reapplied. Should be used with PickBoostXpPenality.");
        partReplacementXpPenality = configfile.getInt(
                "XpPenality",
                CATEGORY_PartReplacement,
                0,
                0,
                100,
                "How much of the current XP% shall be removed when replacing parts (So if you had 50%, and penality is 10% it'll remove 5% xp, resulting in 45%). Does not remove Skill Levels.");
        partReplacementBoostXpPenality = configfile.getInt(
                "PickBoostXpPenality",
                CATEGORY_PartReplacement,
                5,
                0,
                100,
                "How much of the current XP% to the next mining level shall be removed when replacing the pickaxe head. Useful to remove the mining level boost on part replacement.");

        /** MobHeads **/
        configfile.setCategoryComment(CATEGORY_Heads, "Mob Head Module: Adds additional Mob heads and drops");

        // drop behaviour
        baseHeadDropChance = configfile
                .getInt("baseDropChange", CATEGORY_Heads, 5, 0, 100, "Base percentage for a head to drop");
        beheadingHeadDropChance = configfile.getInt(
                "beheadingDropChange",
                CATEGORY_Heads,
                2,
                1,
                100,
                "Percentage added to base percentage per level of Beheading modifier");

        headsFromFakePlayerKills = configfile.getBoolean(
                "headsFromFakePlayerKills",
                CATEGORY_Heads,
                false,
                "Drop heads when mobs are killed by a fake player");

        /** Vanilla/TConstruct Tweaks **/
        configfile.setCategoryComment(
                CATEGORY_Tweaks,
                "Tweak Module: Tweaks to vanilla Minecraft and Tinker's Construct");

        nerfVanillaTools = configfile.getBoolean(
                "disableRegularTools",
                CATEGORY_Tweaks,
                true,
                "Makes all non-TConstruct tools mine nothing");
        nerfVanillaHoes = configfile.getBoolean(
                "disableRegularHoes",
                CATEGORY_Tweaks,
                false,
                "Makes all non-TConstruct hoes to not be able to hoe ground. Use the Mattock.");
        nerfVanillaSwords = configfile.getBoolean(
                "disableRegularSwords",
                CATEGORY_Tweaks,
                false,
                "Makes all non-TConstruct swords useless. Like whacking enemies with a stick.");
        nerfVanillaBows = configfile.getBoolean(
                "disableRegularBows",
                CATEGORY_Tweaks,
                false,
                "Makes all non-TConstruct bows useless. You suddenly forgot how to use a bow.");

        // gravel/flint tweaks
        removeFlintDrop = configfile.getBoolean(
                "removeFlintDrop",
                CATEGORY_Tweaks,
                true,
                "Removes the random chance of getting flint from gravel");
        addFlintRecipe = configfile.getBoolean(
                "addFlintRecipe",
                CATEGORY_Tweaks,
                true,
                "Adds a shapeless recipe to get flint from gravel");
        recipeGravelPerFlint = configfile
                .getInt("gravelPerFlint", CATEGORY_Tweaks, 3, 1, 9, "How many gravel are required to craft one Flint");

        removeObsidianAlloy = configfile.getBoolean(
                "removeObsidianAlloy",
                CATEGORY_Tweaks,
                true,
                "Removes the creation of Obsidian in the Smeltery. This is normally used to prevent the early pre-diamond creation of obsidian tools.");

        // ticon tweaks
        disableStoneTools = configfile.getBoolean(
                "disableStoneTools",
                CATEGORY_Tweaks,
                true,
                "Stone Tools can only be used to create casts, but no tools");
        castsBurnMaterial = configfile.getBoolean(
                "castingBurnsMaterial",
                CATEGORY_Tweaks,
                true,
                "Creating a metal cast burns up the material that was used to create it");
        allowStencilReuse = configfile.getBoolean(
                "allowStencilReuse",
                CATEGORY_Tweaks,
                false,
                "Allows to use stencils as blank patterns in the stencil table");
        allowPartReuse = configfile.getBoolean(
                "allowPartReuse",
                CATEGORY_Tweaks,
                true,
                "Allows toolparts to be used as material in the Part Builder. Like, turn a Pick head into a Shovel head.!");
        allowStringBinding = configfile.getBoolean(
                "allowStringBinding",
                CATEGORY_Tweaks,
                true,
                "Allows you to use a piece of string as a binding");
        disableBonusMods = configfile.getBoolean(
                "disableBonusModifierModifiers",
                CATEGORY_Tweaks,
                false,
                "Removes the ability to add modifiers with Gold, Diamond, Netherstars etc.");

        // easy crafting
        easyToolRepair = configfile.getBoolean(
                "easyToolRepair",
                CATEGORY_Tweaks,
                true,
                "Allows to repair your tool in a crafting grid, without tool station");
        easyPartCrafting = configfile.getBoolean(
                "easyPartCrafting",
                CATEGORY_Tweaks,
                false,
                "Allows to craft tool parts with a pattern and the material in any crafting grid.");
        easyToolBuilding = configfile.getBoolean(
                "easyToolBuilding",
                CATEGORY_Tweaks,
                false,
                "Allows to create Tool Station Tools (2-3 Parts) in any crafting grid");
        easyAdvancedToolBuilding = configfile.getBoolean(
                "easyToolBuildingForge",
                CATEGORY_Tweaks,
                false,
                "Allows to also create Tool Forge Tools (4 Parts) in any crafting grid");

        // stuff
        removeStoneTorchRecipe = configfile.getBoolean(
                "removeStoneTorchRecipe",
                CATEGORY_Tweaks,
                false,
                "Removes the recipe for Tinker's Construct's stone torch");
        moreExpensiveSilkyCloth = configfile.getBoolean(
                "moreExpensiveSilkyCloth",
                CATEGORY_Tweaks,
                true,
                "Silky Cloth needs gold ingots, instead of nuggets");
        moreExpensiveSilkyJewel = configfile.getBoolean(
                "moreExpensiveSilkyJewel",
                CATEGORY_Tweaks,
                false,
                "Silky Jewel needs an emerald block, instead of one emerald");
        moreModifiersForFlux = configfile.getBoolean(
                "moreModifiersForFlux",
                CATEGORY_Tweaks,
                true,
                "Flux modifier requires 2 Modifiers. Because that stuff is broken.");

        // repair
        maxToolRepairs = configfile.getInt(
                "repairsLimit",
                CATEGORY_Tweaks,
                -1,
                -1,
                999,
                "Limits the amount how often a tool can be repaired. -1 means unlimited repairs, like normally.");
        // repairAmountMultiplier = configfile.getFloat("repairAmountMultiplier", CATEGORY_Tweaks, 1.0f, 0.01f, 9.99f,
        // "A factor that is multiplied onto the amount a tool is repaired. (0.5 = half durability restored per repair,
        // 2.0 = twice as much durability restored per repair)");

        /** Allowed tools for nerfed vanilla tools **/
        configfile.setCategoryComment(
                CATEGORY_AllowedTools,
                "Tweak Module: This category allows you to specify which tools ARE NOT USABLE or alternatively ARE STILL USABLE if the option to disable non-TConstsruct tools is enabled.\nTo make this easier a /dumpTools command is provided, that dumps the names of all applicable items in your world. Copy'n'Paste away!");
        {
            String type = configfile.getString(
                    "exclusionType",
                    CATEGORY_AllowedTools,
                    "blacklist",
                    "Change the type of the exclusion.\n'blacklist' means the listed tools are made unusable.\n'whitelist' means ALL tools except the listed ones are unusable.",
                    new String[] { "whitelist", "blacklist" });
            excludedToolsIsWhitelist = "whitelist".equals(type);

            String[] tools = configfile.getStringList(
                    "tools",
                    CATEGORY_AllowedTools,
                    defaultExcludedTools,
                    "Tools that are excluded if the option to nerf non-tinkers tools is enabled.");
            String[] swords = configfile.getStringList(
                    "swords",
                    CATEGORY_AllowedTools,
                    defaultExcludedSwords,
                    "Swords that are excluded if the option to nerf non-tinkers swords is enabled.");
            String[] bows = configfile.getStringList(
                    "bows",
                    CATEGORY_AllowedTools,
                    defaultExcludedBows,
                    "Bows that are excluded if the option to nerf non-tinkers bows is enabled.");
            String[] hoes = configfile.getStringList(
                    "hoes",
                    CATEGORY_AllowedTools,
                    defaultExcludedHoes,
                    "Hoes that are excluded if the option to nerf non-tinkers hoes is enabled.");

            excludedModTools.addAll(
                    Arrays.asList(
                            configfile.getStringList(
                                    "mods",
                                    CATEGORY_AllowedTools,
                                    defaultAllowMod,
                                    "Here you can exclude entire mods by adding their mod-id (the first part of the string).")));

            if (nerfVanillaTools) excludedTools.addAll(Arrays.asList(tools));
            if (nerfVanillaSwords) excludedTools.addAll(Arrays.asList(swords));
            if (nerfVanillaBows) excludedTools.addAll(Arrays.asList(bows));
            if (nerfVanillaHoes) excludedTools.addAll(Arrays.asList(hoes));
        }

        /** Debug **/
        configfile.setCategoryComment(CATEGORY_Debug, "Stuff to give you/me more information");

        showDebugXP = configfile
                .getBoolean("showDebugXP", CATEGORY_Debug, false, "Current Tool/Pick XP is shown as debug (F3) text");

        logHarvestLevelChanges = configfile.getBoolean(
                "logBlockHarvestLevelChange",
                CATEGORY_Debug,
                true,
                "Logs when the harvest level of a block is changed");
        logMiningLevelChanges = configfile.getBoolean(
                "logToolMiningLevelChange",
                CATEGORY_Debug,
                true,
                "Logs when the mining level of a (non-tinker) tool is changed");
        logToolMaterialChanges = configfile.getBoolean(
                "logTinkerMaterialChange",
                CATEGORY_Debug,
                true,
                "Logs when the mining level of a tinkers tool material is changed");
        logBonusExtraChance = configfile.getBoolean(
                "logBonusExtraChance",
                CATEGORY_Debug,
                true,
                "Logs how much the extra-chance from doing stuff you had when getting a random bonus on levelup.");
        logOverrideChanges = configfile.getBoolean(
                "logExcessiveOverrideChanges",
                CATEGORY_Debug,
                false,
                "Logs every single thing done by the Override module. Use at your own risk. ;)");

        configfile.save();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(Reference.MOD_ID)) sync();
    }

    private static String[] defaultExcludedTools = new String[] {
            // botania
            "Botania:manasteelAxe", "Botania:manasteelPick", "Botania:manasteelShovel",
            // Flaxbeards Steam Power
            "Steamcraft:axeGildedGold", "Steamcraft:pickGildedGold", "Steamcraft:shovelGildedGold",
            "Steamcraft:axeBrass", "Steamcraft:pickBrass", "Steamcraft:shovelBrass",
            // IC2
            "IC2:itemToolBronzeAxe", "IC2:itemToolBronzePickaxe", "IC2:itemToolBronzeSpade",
            // Railcraft
            "Railcraft:tool.steel.axe", "Railcraft:tool.steel.pickaxe", "Railcraft:tool.steel.shovel" };
    private static String[] defaultExcludedHoes = new String[] { "Steamcraft:hoeGildedGold", "Steamcraft:hoeBrass",
            "IC2:itemToolBronzeHoe", "Railcraft:tool.steel.hoe" };
    private static String[] defaultExcludedSwords = new String[] { "Botania:manasteelSword",
            "Steamcraft:swordGildedGold", "Steamcraft:swordBrass", "ThermalExpansion:tool.battleWrenchInvar",
            "IC2:itemToolBronzeSword", "Railcraft:tool.steel.sword" };
    private static String[] defaultExcludedBows = new String[] {};

    private static String[] defaultAllowMod = new String[] { "minecraft", "Metallurgy", "Natura", "BiomesOPlenty",
            "ProjRed|Exploration", "appliedenergistics2", "MekanismTool", "ThermalFoundation" };
}
