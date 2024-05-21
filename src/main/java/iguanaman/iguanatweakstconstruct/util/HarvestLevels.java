package iguanaman.iguanatweakstconstruct.util;

import static net.minecraft.util.EnumChatFormatting.BLUE;
import static net.minecraft.util.EnumChatFormatting.DARK_AQUA;
import static net.minecraft.util.EnumChatFormatting.DARK_GRAY;
import static net.minecraft.util.EnumChatFormatting.DARK_PURPLE;
import static net.minecraft.util.EnumChatFormatting.DARK_RED;
import static net.minecraft.util.EnumChatFormatting.GOLD;
import static net.minecraft.util.EnumChatFormatting.GRAY;
import static net.minecraft.util.EnumChatFormatting.LIGHT_PURPLE;
import static net.minecraft.util.EnumChatFormatting.RED;
import static net.minecraft.util.EnumChatFormatting.WHITE;

import java.util.Map;

import net.minecraft.util.StatCollector;

import tconstruct.library.tools.ToolMaterial;

// strength of the tool-material. stone == strength of a stone pick etc.
public final class HarvestLevels {

    private HarvestLevels() {} // non-instantiable

    public static int _0_stone = 0;
    public static int _1_flint = 1;
    public static int _2_copper = 2;
    public static int _3_iron = 3;
    public static int _4_bronze = 4;
    public static int _5_diamond = 5;
    public static int _6_obsidian = 6;
    public static int _7_ardite = 7;
    public static int _8_cobalt = 8;
    public static int _9_manyullym = 9;

    public static int max = 9;

    private static boolean vanilla = false;

    // needed if HarvestLevels module is deactivated to achieve vanilla mining levels
    public static void adjustToVanillaLevels() {
        _1_flint = 1;
        _2_copper = 1;
        _3_iron = 2;
        _4_bronze = 2;
        _5_diamond = 2;
        _6_obsidian = 3;
        _7_ardite = 4;
        _8_cobalt = 4;
        _9_manyullym = 5;

        max = 5;

        vanilla = true;
    }

    public static void updateHarvestLevelNames() {
        Map<Integer, String> names = tconstruct.library.util.HarvestLevels.harvestLevelNames;

        if (vanilla) {
            names.put(0, GRAY + StatCollector.translateToLocal("mininglevel.stone"));
            names.put(1, DARK_RED + StatCollector.translateToLocal("mininglevel.iron"));
            names.put(2, RED + StatCollector.translateToLocal("mininglevel.redstone"));
            names.put(3, LIGHT_PURPLE + StatCollector.translateToLocal("mininglevel.obsidian"));
            names.put(4, BLUE + StatCollector.translateToLocal("mininglevel.cobalt"));
            names.put(5, DARK_PURPLE + StatCollector.translateToLocal("mininglevel.manyullyn"));
            names.put(6, DARK_PURPLE + StatCollector.translateToLocal("mininglevel.manyullyn") + LIGHT_PURPLE + "+");
        } else {
            names.put(0, GRAY + StatCollector.translateToLocal("mininglevel.stone"));
            names.put(1, GOLD + StatCollector.translateToLocal("mininglevel.copper"));
            names.put(2, DARK_GRAY + StatCollector.translateToLocal("mininglevel.iron"));
            names.put(3, WHITE + StatCollector.translateToLocal("mininglevel.tin"));
            names.put(4, RED + StatCollector.translateToLocal("mininglevel.redstone"));
            names.put(5, LIGHT_PURPLE + StatCollector.translateToLocal("mininglevel.obsidian"));
            names.put(6, DARK_RED + StatCollector.translateToLocal("mininglevel.ardite"));
            names.put(7, DARK_AQUA + StatCollector.translateToLocal("mininglevel.cobalt"));
            names.put(8, DARK_PURPLE + StatCollector.translateToLocal("mininglevel.manyullyn"));
            names.put(9, DARK_PURPLE + StatCollector.translateToLocal("mininglevel.manyullyn") + LIGHT_PURPLE + "+");
        }
    }

    public static String getHarvestLevelName(int num) {
        return tconstruct.library.util.HarvestLevels.getHarvestLevelName(num);
    }

    public static void setCustomHarvestLevelNames(Map<Integer, ToolMaterial> mats) {
        for (Map.Entry<Integer, ToolMaterial> mat : mats.entrySet())
            tconstruct.library.util.HarvestLevels.harvestLevelNames
                    .put(mat.getKey(), mat.getValue().style() + mat.getValue().localizedName());
    }
}
