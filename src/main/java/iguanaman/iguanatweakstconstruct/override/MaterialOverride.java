package iguanaman.iguanatweakstconstruct.override;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ToolMaterial;
import iguanaman.iguanatweakstconstruct.reference.Config;
import iguanaman.iguanatweakstconstruct.util.HarvestLevels;
import iguanaman.iguanatweakstconstruct.util.Log;

public class MaterialOverride implements IOverride {

    @Override
    public void createDefault(Configuration config) {
        Log.info("Creating Material Default File");
        // cycle through all materials
        HashMap<Integer, ToolMaterial> newMaterials = new HashMap<Integer, ToolMaterial>();
        for (Map.Entry<Integer, ToolMaterial> entry : TConstructRegistry.toolMaterials.entrySet()) {
            ToolMaterial mat = entry.getValue();
            String category = "materials" + Configuration.CATEGORY_SPLITTER + mat.materialName;
            category = category.toLowerCase();
            processMaterial(category, mat, config);
        }
    }

    @Override
    public void processConfig(Configuration config) {
        Log.debug("Loading Material Overrides");
        StringBuilder comment = new StringBuilder();
        comment.append(
                "Copy the desired materials you want to change from the defaults file into this file and adapt the stats.\n\n");

        comment.append("Mining Levels:\n");
        for (int i = 0; i <= HarvestLevels.max; i++)
            comment.append(String.format("\t%d - %s\n", i, HarvestLevels.getHarvestLevelName(i)));

        comment.append("\n");
        comment.append(
                "Stonebound and Jagged are both in the 'shoddy' property. A positive number is stonebound, a negative number jagged.\n");
        comment.append("\n");
        comment.append(
                "XP Amount is a multiplier on the XP needed to make a tool of this materia level up. For example, 2 makes it take\n");
        comment.append(
                "twice as long to level up. .5 makes it take half as long. A tool made of multiple materials averages the growth\n");
        comment.append("rate using the geometric mean. (NOT the arethmatic mean!)\n");
        comment.append("\n");
        comment.append("Possible Colors:\n");
        for (EnumChatFormatting format : EnumChatFormatting.values())
            comment.append(String.format("\t%s\n", format.getFriendlyName()));

        config.setCategoryComment(" Info", comment.toString());

        config.setCategoryComment(" Info", comment.toString());
        Property prop = config.get(
                " Info",
                "fillWithDefault",
                false,
                "Fills this file with the default values, not altering changed values.");
        boolean fillWithDefault = prop.getBoolean();
        prop.set(false);
        prop = config.get(
                " Info",
                "removeDefaultValues",
                false,
                "Removes all entries that correspond to the default values. Will likely fail with some materials because floats are derp.");
        boolean cleanup = prop.getBoolean();
        prop.set(false);

        config.setCategoryComment("materials", "Insert materials to change here");

        // cycle through all materials
        HashMap<Integer, ToolMaterial> newMaterials = new HashMap<Integer, ToolMaterial>();
        HashMap<String, ToolMaterial> newMaterialStrings = new HashMap<String, ToolMaterial>();
        for (Map.Entry<Integer, ToolMaterial> entry : TConstructRegistry.toolMaterials.entrySet()) {
            ToolMaterial mat = entry.getValue();
            String category = "materials" + Configuration.CATEGORY_SPLITTER + mat.materialName;
            category = category.toLowerCase();

            if (!config.hasCategory(category) && !fillWithDefault) {
                // retain the material :S
                newMaterials.put(entry.getKey(), mat);
                newMaterialStrings.put(mat.name(), mat);
                continue;
            }

            ToolMaterial newMat = processMaterial(category, mat, config);
            // we cannot replace the material while cycling through, since it'd cause a ConcurrentModificationException
            newMaterials.put(entry.getKey(), newMat);
            newMaterialStrings.put(newMat.name(), newMat);

            if (Config.logOverrideChanges)
                Log.info(String.format("Material Override: Changed Material %s", mat.materialName));

            if (cleanup && isEqual(mat, newMat)) config.removeCategory(config.getCategory(category));
        }

        TConstructRegistry.toolMaterials = newMaterials;
        TConstructRegistry.toolMaterialStrings = newMaterialStrings;
    }

    private ToolMaterial processMaterial(String category, ToolMaterial mat, Configuration config) {
        // get material stats
        int harvestLevel = config.get(category, "harvestLevel", mat.harvestLevel()).getInt();
        float xpAmount = (float) config.get(category, "xpAmount", XPAdjustmentMap.get(mat.materialName)).getDouble();
        int durability = config.get(category, "durability", mat.durability()).getInt();
        int miningspeed = config.get(category, "miningSpeed", mat.toolSpeed()).getInt();
        int attack = config.get(category, "attack", mat.attack()).getInt();
        float handleModifier = (float) config.get(category, "handleModifier", mat.handleDurability()).getDouble();
        int reinforced = config.get(category, "reinforced", mat.reinforced()).getInt();
        float stonebound = (float) config.get(category, "shoddy", mat.shoddy()).getDouble();
        String tipStyle; // enum
        String oldStyle = stringToEnum(mat.style()).getFriendlyName();
        tipStyle = config.get(category, "color", oldStyle).getString();
        // convert the enum string to the actual color
        EnumChatFormatting newStyle = EnumChatFormatting.getValueByName(tipStyle);
        if (newStyle != null) tipStyle = newStyle.toString();
        else tipStyle = mat.style();

        // save highest harvest level given
        if (harvestLevel > HarvestLevels.max) HarvestLevels.max = harvestLevel;

        // reconstruct the material
        XPAdjustmentMap.put(mat.materialName, xpAmount);
        return new ToolMaterial(
                mat.materialName,
                mat.localizationString,
                harvestLevel,
                durability,
                miningspeed,
                attack,
                handleModifier,
                reinforced,
                stonebound,
                tipStyle,
                mat.primaryColor);
    }

    private EnumChatFormatting stringToEnum(String s) {
        for (EnumChatFormatting color : EnumChatFormatting.values()) if (color.toString().equals(s)) return color;

        return EnumChatFormatting.GRAY;
    }

    private boolean isEqual(ToolMaterial mat1, ToolMaterial mat2) {
        return mat1.harvestLevel() == mat2.harvestLevel() && mat1.style().equals(mat2.style())
                && mat1.ability().equals(mat2.ability())
                && mat1.attack() == mat2.attack()
                && mat1.durability() == mat2.durability()
                && mat1.toolSpeed() == mat2.toolSpeed()
                && Float.compare(mat1.handleDurability(), mat2.handleDurability()) == 0
                && mat1.reinforced() == mat2.reinforced()
                && Float.compare(XPAdjustmentMap.get(mat1.name()), XPAdjustmentMap.get(mat2.name())) == 0
                && Float.compare(mat1.shoddy(), mat2.shoddy()) == 0;
    }
}
